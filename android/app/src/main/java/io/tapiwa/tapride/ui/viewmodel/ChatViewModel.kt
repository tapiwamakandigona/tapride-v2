package io.tapiwa.tapride.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.appwrite.models.RealtimeSubscription
import io.tapiwa.tapride.constants.AppwriteConfig
import io.tapiwa.tapride.data.model.Message
import io.tapiwa.tapride.data.repository.AuthRepository
import io.tapiwa.tapride.data.repository.ChatRepository
import io.tapiwa.tapride.services.AppwriteClient
import io.tapiwa.tapride.services.RealtimeService
import io.tapiwa.tapride.util.TapLogger
import io.tapiwa.tapride.util.currentIso8601
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Chat screen.
 *
 * @property isLoading     True while messages are being fetched.
 * @property messages      Ordered list of [Message] objects for the ride.
 * @property currentInput  Current text in the message input field.
 * @property currentUserId ID of the logged-in user, used to style outgoing bubbles.
 * @property error         Non-null when an error message should be shown.
 */
data class ChatUiState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val currentInput: String = "",
    val currentUserId: String = "",
    val error: String? = null
)

/**
 * ViewModel for the in-ride Chat screen.
 *
 * Subscribes to realtime message events so new messages appear instantly.
 *
 * @param rideId The Appwrite document ID of the ride whose chat to show.
 */
class ChatViewModel(private val rideId: String) : ViewModel() {

    private val chatRepo = ChatRepository(AppwriteClient.databases)
    private val authRepo = AuthRepository(AppwriteClient.account)
    private val realtimeService = RealtimeService(AppwriteClient.realtime)

    private val _uiState = MutableStateFlow(ChatUiState())

    /** Observed by the Chat screen Composable. */
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var subscription: RealtimeSubscription? = null

    init {
        loadCurrentUser()
        loadMessages()
        subscribeToMessages()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepo.currentUser().onSuccess { user ->
                _uiState.value = _uiState.value.copy(currentUserId = user.id)
            }
        }
    }

    /** Fetches all messages for this ride from Appwrite. */
    fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            chatRepo.getMessages(rideId)
                .onSuccess { msgs ->
                    _uiState.value = _uiState.value.copy(isLoading = false, messages = msgs)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    /**
     * Updates the message draft as the user types.
     *
     * @param text Current value of the text field.
     */
    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(currentInput = text)
    }

    /**
     * Sends the current draft message and clears the input field.
     * Does nothing if the input is blank.
     */
    fun sendMessage() {
        val text = _uiState.value.currentInput.trim()
        val senderId = _uiState.value.currentUserId
        if (text.isBlank() || senderId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentInput = "")
            chatRepo.sendMessage(
                Message(
                    id = "",
                    rideId = rideId,
                    senderId = senderId,
                    text = text,
                    sentAt = currentIso8601()
                )
            ).onFailure { e ->
                TapLogger.e("ChatViewModel", "sendMessage failed", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun subscribeToMessages() {
        val channel = "databases.${AppwriteConfig.DATABASE_ID}.collections.${AppwriteConfig.COL_MESSAGES}.documents"
        subscription = realtimeService.subscribe(channel) { _ ->
            loadMessages()
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
