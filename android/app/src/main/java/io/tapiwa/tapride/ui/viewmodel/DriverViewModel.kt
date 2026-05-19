package io.tapiwa.tapride.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import io.appwrite.models.RealtimeSubscription
import io.tapiwa.tapride.constants.AppwriteConfig
import io.tapiwa.tapride.data.model.DriverLocation
import io.tapiwa.tapride.data.model.Ride
import io.tapiwa.tapride.data.repository.AuthRepository
import io.tapiwa.tapride.data.repository.LocationRepository
import io.tapiwa.tapride.data.repository.ProfileRepository
import io.tapiwa.tapride.data.repository.RideRepository
import io.tapiwa.tapride.services.AppwriteClient
import io.tapiwa.tapride.services.RealtimeService
import io.tapiwa.tapride.util.TapLogger
import io.tapiwa.tapride.util.currentIso8601
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * UI state for the Driver Dashboard.
 *
 * @property isLoading       True while an async call is pending.
 * @property isOnline        Whether the driver is currently accepting rides.
 * @property openRides       List of available (requested) rides nearby.
 * @property activeRide      The ride the driver has accepted and is working on.
 * @property error           Non-null when an error should be displayed.
 */
data class DriverUiState(
    val isLoading: Boolean = false,
    val isOnline: Boolean = false,
    val openRides: List<Ride> = emptyList(),
    val activeRide: Ride? = null,
    val error: String? = null
)

/**
 * ViewModel for the Driver Dashboard.
 *
 * Handles toggling online status, fetching available rides, accepting rides,
 * and sending location pings every 10 seconds while online.
 */
class DriverViewModel : ViewModel() {

    private val rideRepo = RideRepository(AppwriteClient.databases)
    private val authRepo = AuthRepository(AppwriteClient.account)
    private val profileRepo = ProfileRepository(AppwriteClient.databases)
    private val locationRepo = LocationRepository(AppwriteClient.databases)
    private val realtimeService = RealtimeService(AppwriteClient.realtime)

    private val _uiState = MutableStateFlow(DriverUiState())

    /** Observed by the Driver Dashboard screen. */
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    private var currentUserId: String = ""
    private var locationJob: Job? = null
    private var ridesSubscription: RealtimeSubscription? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepo.currentUser().onSuccess { user ->
                currentUserId = user.id
                loadActiveRide()
            }
        }
    }

    /**
     * Toggles the driver's online status.
     * When going online: starts GPS polling, loads open rides, subscribes to realtime.
     * When going offline: stops GPS polling, clears ride list.
     *
     * @param context Android context needed to access FusedLocationProvider.
     */
    fun toggleOnline(context: Context) {
        val nowOnline = !_uiState.value.isOnline
        _uiState.value = _uiState.value.copy(isOnline = nowOnline)
        viewModelScope.launch {
            profileRepo.getProfile(currentUserId).onSuccess { profile ->
                profileRepo.updateProfile(profile.copy(isOnline = nowOnline))
            }
        }
        if (nowOnline) {
            startLocationUpdates(context)
            loadOpenRides()
            subscribeToOpenRides()
        } else {
            stopLocationUpdates()
            unsubscribeFromRides()
            _uiState.value = _uiState.value.copy(openRides = emptyList())
        }
    }

    /**
     * Accepts an open ride request.
     *
     * @param ride The [Ride] to accept.
     */
    fun acceptRide(ride: Ride) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            rideRepo.updateRide(
                ride.copy(
                    driverId = currentUserId,
                    status = "accepted",
                    acceptedAt = currentIso8601()
                )
            ).onSuccess { updated ->
                _uiState.value = _uiState.value.copy(isLoading = false, activeRide = updated)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /**
     * Marks the ride as in-progress (driver has picked up rider).
     */
    fun startRide() {
        val ride = _uiState.value.activeRide ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            rideRepo.updateRide(ride.copy(status = "in_progress"))
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(isLoading = false, activeRide = updated)
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    /**
     * Marks the ride as completed.
     */
    fun completeRide() {
        val ride = _uiState.value.activeRide ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            rideRepo.updateRide(
                ride.copy(status = "completed", completedAt = currentIso8601())
            ).onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, activeRide = null)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun loadOpenRides() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            rideRepo.getOpenRides().onSuccess { rides ->
                _uiState.value = _uiState.value.copy(isLoading = false, openRides = rides)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun loadActiveRide() {
        viewModelScope.launch {
            rideRepo.getRidesForDriver(currentUserId).onSuccess { rides ->
                val active = rides.firstOrNull { it.status in listOf("accepted", "in_progress") }
                _uiState.value = _uiState.value.copy(activeRide = active)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(context: Context) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        locationJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val location: Location? = fusedClient.lastLocation.await()
                    location?.let {
                        locationRepo.upsertLocation(
                            DriverLocation(
                                driverId = currentUserId,
                                latitude = it.latitude,
                                longitude = it.longitude,
                                updatedAt = currentIso8601(),
                                isOnline = true
                            )
                        )
                    }
                } catch (e: Exception) {
                    TapLogger.e("DriverViewModel", "Location update failed", e)
                }
                delay(10_000L)
            }
        }
    }

    private fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
    }

    private fun subscribeToOpenRides() {
        val channel = "databases.${AppwriteConfig.DATABASE_ID}.collections.${AppwriteConfig.COL_RIDES}.documents"
        ridesSubscription = realtimeService.subscribe(channel) { _ ->
            loadOpenRides()
        }
    }

    private fun unsubscribeFromRides() {
        ridesSubscription?.let { realtimeService.unsubscribe(it) }
        ridesSubscription = null
    }

    /** Clears the error message after display. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        unsubscribeFromRides()
    }
}
