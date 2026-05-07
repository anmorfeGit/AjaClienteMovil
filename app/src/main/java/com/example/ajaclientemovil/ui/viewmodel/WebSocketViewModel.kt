package com.example.ajaclientemovil.ui.viewmodel

// WebSocketViewModel.kt
import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ajaclientemovil.data.NotifyStatusDTO
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.network.WebSocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

/**
 * ViewModel para la gestión de la conexión WebSocket.
 * @param application Contexto de la aplicación.
 */
class WebSocketViewModel(application: Application) : AndroidViewModel(application) {


    private val wsManager = WebSocketManager.getInstance(application)

    // Lista observable para Compose
    val typingUsers = mutableStateListOf<NotifyStatusDTO>()

    fun notifyActivity(topicId: Long, topicTitle: String?, isStarting: Boolean) {
        val currentUserId = SessionManager.getUser(getApplication())?.id ?: return
        val currentUsername = SessionManager.getUsername(getApplication()) ?: "Anónimo"

        val dto = NotifyStatusDTO(
            userId = currentUserId,
            username = currentUsername,
            topicId = topicId,
            topicTitle = topicTitle
        )

        viewModelScope.launch(Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
            wsManager.sendStatus(dto, isStarting)
        }
    }

    fun startListening() {
        viewModelScope.launch(Dispatchers.IO) {
            wsManager.connectAndSubscribe { newList ->
                viewModelScope.launch(Dispatchers.Main) {
                    // Actualización atómica de la lista
                    typingUsers.clear()
                    typingUsers.addAll(newList)
                }
            }
        }
    }

    fun stopListening() {
        wsManager.disconnect()
        typingUsers.clear()
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun clearActivity(topicId: Long, topicTitle: String?) {
        viewModelScope.launch(Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
            val user = SessionManager.getUser(getApplication()) ?: return@launch

            val activeTitle = typingUsers.find { it.topicId == topicId && it.userId == user.id }?.topicTitle
                ?: topicTitle
                ?: "Tema #$topicId"

            val dto = NotifyStatusDTO(
                userId = user.id,
                username = user.username,
                topicId = topicId,
                topicTitle = activeTitle
            )
            wsManager.sendStatus(dto, isStarting = false)
        }
    }

}