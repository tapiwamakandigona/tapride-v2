package io.tapiwa.tapride.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.tapiwa.tapride.data.model.Profile
import io.tapiwa.tapride.data.repository.AuthRepository
import io.tapiwa.tapride.data.repository.ProfileRepository
import io.tapiwa.tapride.services.AppwriteClient
import io.tapiwa.tapride.util.TapLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Profile screen.
 *
 * @property isLoading  True while fetching or updating data.
 * @property profile    The current user's [Profile], or null if not yet loaded.
 * @property isSaved    Flips to true momentarily after a successful save.
 * @property error      Non-null when an error message should be shown.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: Profile? = null,
    val isSaved: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Profile screen.
 *
 * Handles loading, editing, and saving the user's [Profile].
 */
class ProfileViewModel : ViewModel() {

    private val authRepo = AuthRepository(AppwriteClient.account)
    private val profileRepo = ProfileRepository(AppwriteClient.databases)

    private val _uiState = MutableStateFlow(ProfileUiState())

    /** Observed by the Profile screen Composable. */
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    /** Fetches the current user's profile from Appwrite. */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepo.currentUser().onSuccess { user ->
                profileRepo.getProfile(user.id)
                    .onSuccess { profile ->
                        _uiState.value = _uiState.value.copy(isLoading = false, profile = profile)
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /**
     * Persists updated profile fields to Appwrite.
     *
     * @param fullName     Updated display name.
     * @param phone        Updated phone number.
     * @param vehicleType  Updated vehicle type (drivers only).
     * @param licensePlate Updated plate (drivers only).
     */
    fun saveProfile(
        fullName: String,
        phone: String,
        vehicleType: String = "",
        licensePlate: String = ""
    ) {
        val existing = _uiState.value.profile ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isSaved = false)
            profileRepo.updateProfile(
                existing.copy(
                    fullName = fullName,
                    phone = phone,
                    vehicleType = vehicleType,
                    licensePlate = licensePlate
                )
            ).onSuccess { updated ->
                _uiState.value = _uiState.value.copy(isLoading = false, profile = updated, isSaved = true)
            }.onFailure { e ->
                TapLogger.e("ProfileViewModel", "saveProfile failed", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /** Resets the [ProfileUiState.isSaved] flag after the UI has reacted to it. */
    fun consumeSavedEvent() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }

    /** Clears the error message after display. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
