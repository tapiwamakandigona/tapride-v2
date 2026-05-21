package io.tapiwa.tapride.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.appwrite.models.RealtimeSubscription
import io.tapiwa.tapride.constants.AppwriteConfig
import io.tapiwa.tapride.data.model.Ride
import io.tapiwa.tapride.data.repository.AuthRepository
import io.tapiwa.tapride.data.repository.RideRepository
import io.tapiwa.tapride.services.AppwriteClient
import io.tapiwa.tapride.services.RealtimeService
import io.tapiwa.tapride.util.Fare
import io.tapiwa.tapride.util.Geo
import io.tapiwa.tapride.util.TapLogger
import io.tapiwa.tapride.util.currentIso8601
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Rider Dashboard.
 *
 * @property isLoading       True while a network call is pending.
 * @property currentRide     The most recent active ride for this rider, or null.
 * @property rideHistory     Past completed/cancelled rides.
 * @property pickupAddress   Text entered in the pickup field.
 * @property dropAddress     Text entered in the destination field.
 * @property fareEstimate    Calculated fare shown before requesting a ride.
 * @property error           Non-null when an error should be displayed.
 */
data class RiderUiState(
    val isLoading: Boolean = false,
    val currentRide: Ride? = null,
    val rideHistory: List<Ride> = emptyList(),
    val pickupAddress: String = "",
    val dropAddress: String = "",
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val dropLat: Double = 0.0,
    val dropLng: Double = 0.0,
    val fareEstimate: Double = 0.0,
    val error: String? = null
)

/**
 * ViewModel for the Rider Dashboard and ride-request flow.
 */
class RiderViewModel : ViewModel() {

    private val rideRepo = RideRepository(AppwriteClient.databases)
    private val authRepo = AuthRepository(AppwriteClient.account)
    private val realtimeService = RealtimeService(AppwriteClient.realtime)

    private val _uiState = MutableStateFlow(RiderUiState())

    /** Observed by rider UI screens. */
    val uiState: StateFlow<RiderUiState> = _uiState.asStateFlow()

    private var currentUserId: String = ""
    private var rideSubscription: RealtimeSubscription? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepo.currentUser().onSuccess { user ->
                currentUserId = user.id
                loadActiveRide()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = "Session expired. Please log in again.")
            }
        }
    }

    /**
     * Looks for any non-terminal ride belonging to this rider and subscribes
     * to realtime updates for it.
     */
    fun loadActiveRide() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            rideRepo.getRidesForRider(currentUserId).onSuccess { rides ->
                val active = rides.firstOrNull { it.status in listOf("requested", "accepted", "in_progress") }
                _uiState.value = _uiState.value.copy(isLoading = false, currentRide = active)
                active?.let { subscribeToRide(it.id) }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /**
     * Updates the pickup coordinates and recalculates fare.
     *
     * @param lat       Latitude of the selected pickup.
     * @param lng       Longitude of the selected pickup.
     * @param address   Human-readable address string.
     */
    fun setPickup(lat: Double, lng: Double, address: String) {
        _uiState.value = _uiState.value.copy(
            pickupLat = lat, pickupLng = lng, pickupAddress = address
        )
        recalculateFare()
    }

    /**
     * Updates the drop-off coordinates and recalculates fare.
     *
     * @param lat     Latitude of the selected destination.
     * @param lng     Longitude of the selected destination.
     * @param address Human-readable address string.
     */
    fun setDrop(lat: Double, lng: Double, address: String) {
        _uiState.value = _uiState.value.copy(
            dropLat = lat, dropLng = lng, dropAddress = address
        )
        recalculateFare()
    }

    /** Updates only the pickup address text without zeroing coordinates. */
    fun setPickupAddress(address: String) {
        _uiState.value = _uiState.value.copy(pickupAddress = address)
    }

    /** Updates only the drop address text without zeroing coordinates. */
    fun setDropAddress(address: String) {
        _uiState.value = _uiState.value.copy(dropAddress = address)
    }

    private fun recalculateFare() {
        val s = _uiState.value
        if (s.pickupLat == 0.0 || s.dropLat == 0.0) return
        val dist = Geo.haversine(s.pickupLat, s.pickupLng, s.dropLat, s.dropLng)
        _uiState.value = _uiState.value.copy(fareEstimate = Fare.estimate(dist))
    }

    /**
     * Submits a ride request to Appwrite.
     * Requires pickup and drop coordinates to be set first.
     */
    fun requestRide() {
        val s = _uiState.value
        if (s.pickupLat == 0.0 || s.dropLat == 0.0) {
            _uiState.value = _uiState.value.copy(error = "Please set pickup and destination.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val distKm = Geo.haversine(s.pickupLat, s.pickupLng, s.dropLat, s.dropLng)
            val ride = Ride(
                id = "",
                riderId = currentUserId,
                status = "requested",
                pickupLat = s.pickupLat,
                pickupLng = s.pickupLng,
                pickupAddress = s.pickupAddress,
                dropLat = s.dropLat,
                dropLng = s.dropLng,
                dropAddress = s.dropAddress,
                fareEstimate = Fare.estimate(distKm),
                distanceKm = distKm,
                requestedAt = currentIso8601()
            )
            rideRepo.createRide(ride)
                .onSuccess { created ->
                    _uiState.value = _uiState.value.copy(isLoading = false, currentRide = created)
                    subscribeToRide(created.id)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    /**
     * Cancels the active ride.
     *
     * @param reason Optional reason text.
     */
    fun cancelRide(reason: String = "") {
        val ride = _uiState.value.currentRide ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            rideRepo.updateRide(ride.copy(status = "cancelled", cancelReason = reason))
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, currentRide = null)
                    unsubscribeFromRide()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    /** Loads the full ride history for the current rider. */
    fun loadRideHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            rideRepo.getRidesForRider(currentUserId)
                .onSuccess { rides ->
                    _uiState.value = _uiState.value.copy(isLoading = false, rideHistory = rides)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    private fun subscribeToRide(rideId: String) {
        val channel = "databases.${AppwriteConfig.DATABASE_ID}.collections.${AppwriteConfig.COL_RIDES}.documents.$rideId"
        rideSubscription = realtimeService.subscribe(channel) { event ->
            TapLogger.d("RiderViewModel", "Realtime ride event: $event")
            loadActiveRide()
        }
    }

    private fun unsubscribeFromRide() {
        rideSubscription?.let { realtimeService.unsubscribe(it) }
        rideSubscription = null
    }

    /** Clears the error message after display. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeFromRide()
    }
}
