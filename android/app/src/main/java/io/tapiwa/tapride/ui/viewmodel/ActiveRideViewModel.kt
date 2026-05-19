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
import io.tapiwa.tapride.util.TapLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Active Ride screen.
 *
 * @property isLoading  True while fetching or updating data.
 * @property ride       The currently active [Ride], or null if not yet loaded.
 * @property error      Non-null when an error message should be shown.
 */
data class ActiveRideUiState(
    val isLoading: Boolean = false,
    val ride: Ride? = null,
    val error: String? = null
)

/**
 * ViewModel for the Active Ride screen.
 *
 * Subscribes to realtime updates on the specific ride document so the
 * rider sees live status changes as the driver updates the ride.
 *
 * @param rideId The Appwrite document ID of the ride to observe.
 */
class ActiveRideViewModel(private val rideId: String) : ViewModel() {

    private val rideRepo = RideRepository(AppwriteClient.databases)
    private val authRepo = AuthRepository(AppwriteClient.account)
    private val realtimeService = RealtimeService(AppwriteClient.realtime)

    private val _uiState = MutableStateFlow(ActiveRideUiState())

    /** Observed by the Active Ride screen. */
    val uiState: StateFlow<ActiveRideUiState> = _uiState.asStateFlow()

    private var subscription: RealtimeSubscription? = null

    init {
        loadRide()
        subscribeToRide()
    }

    /** Fetches the latest state of the ride from Appwrite. */
    fun loadRide() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            rideRepo.getRide(rideId)
                .onSuccess { ride ->
                    _uiState.value = _uiState.value.copy(isLoading = false, ride = ride)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    private fun subscribeToRide() {
        val channel = "databases.${AppwriteConfig.DATABASE_ID}.collections.${AppwriteConfig.COL_RIDES}.documents.$rideId"
        subscription = realtimeService.subscribe(channel) { event ->
            TapLogger.d("ActiveRideViewModel", "Realtime event: $event")
            loadRide()
        }
    }

    /** Clears the error message after display. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.let { realtimeService.unsubscribe(it) }
    }
}
