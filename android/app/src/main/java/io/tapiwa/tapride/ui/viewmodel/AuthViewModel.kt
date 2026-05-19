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
 * UI state for authentication screens.
 *
 * @property isLoading    True while an async operation is in progress.
 * @property profile      Set after successful auth + profile fetch.
 * @property error        Non-null when an error message should be shown.
 * @property isLoggedOut  True after a successful logout.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val profile: Profile? = null,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

/**
 * ViewModel driving login, registration, session check and logout flows.
 */
class AuthViewModel : ViewModel() {

    private val authRepo = AuthRepository(AppwriteClient.account)
    private val profileRepo = ProfileRepository(AppwriteClient.databases)

    private val _uiState = MutableStateFlow(AuthUiState())

    /** Observed by auth screens to react to state changes. */
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Checks whether a session already exists and loads the associated profile.
     * Called from [io.tapiwa.tapride.ui.screen.SplashScreen].
     */
    fun checkSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepo.currentUser()
                .onSuccess { user ->
                    profileRepo.getProfile(user.id)
                        .onSuccess { profile ->
                            _uiState.value = _uiState.value.copy(isLoading = false, profile = profile)
                        }
                        .onFailure {
                            // User exists but no profile yet — send to login.
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    /**
     * Authenticates an existing user and loads their profile.
     *
     * @param email    Registered email.
     * @param password Account password.
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email and password are required.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepo.login(email, password)
                .onSuccess { _ ->
                    authRepo.currentUser().onSuccess { user ->
                        profileRepo.getProfile(user.id)
                            .onSuccess { profile ->
                                _uiState.value = _uiState.value.copy(isLoading = false, profile = profile)
                            }
                            .onFailure { e ->
                                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                            }
                    }
                }
                .onFailure { e ->
                    TapLogger.e("AuthViewModel", "login failed", e)
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Login failed.")
                }
        }
    }

    /**
     * Registers a new user, creates their profile and logs them in.
     *
     * @param email       New account email.
     * @param password    Password (min 8 chars).
     * @param fullName    Display name.
     * @param phone       Phone number.
     * @param role        `"rider"` or `"driver"`.
     * @param vehicleType Vehicle description (required for drivers).
     * @param licensePlate Plate number (required for drivers).
     */
    fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: String,
        vehicleType: String = "",
        licensePlate: String = ""
    ) {
        if (email.isBlank() || password.isBlank() || fullName.isBlank() || phone.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "All fields are required.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepo.register(email, password, fullName)
                .onSuccess { user ->
                    // Immediately log in to establish a session.
                    authRepo.login(email, password).onSuccess {
                        val profile = Profile(
                            userId = user.id,
                            fullName = fullName,
                            phone = phone,
                            role = role,
                            vehicleType = vehicleType,
                            licensePlate = licensePlate
                        )
                        profileRepo.createProfile(profile)
                            .onSuccess { p ->
                                _uiState.value = _uiState.value.copy(isLoading = false, profile = p)
                            }
                            .onFailure { e ->
                                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                            }
                    }.onFailure { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    }
                }
                .onFailure { e ->
                    TapLogger.e("AuthViewModel", "register failed", e)
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Registration failed.")
                }
        }
    }

    /**
     * Logs out the current user and clears the profile.
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepo.logout()
            _uiState.value = AuthUiState(isLoggedOut = true)
        }
    }

    /** Clears the current error message after it has been displayed. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
