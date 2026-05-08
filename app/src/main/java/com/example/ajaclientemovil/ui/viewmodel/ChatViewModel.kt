package com.example.ajaclientemovil.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ajaclientemovil.data.DirectMessageChatEntity
import com.example.ajaclientemovil.data.MessageContentDTO
import com.example.ajaclientemovil.repository.DirectMessageRepository
import com.example.ajaclientemovil.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * Este viewmodel gestiona la lógica de la pantalla de chat.
 * @param dmRepository Repositorio de DirectMessage.
 */

class ChatViewModel(private val dmRepository: DirectMessageRepository) : ViewModel() {

    // Estado para la lista de conversaciones (Bandeja de entrada)
    var conversations by mutableStateOf<List<DirectMessageChatEntity>>(emptyList())

    // Estado para los mensajes de una conversación abierta (Chat individual)
    var activeMessages by mutableStateOf<List<MessageContentDTO>>(emptyList())

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    /**
     * Carga todas las conversaciones (Bandeja de entrada)
     * Se usará al entrar en la pantalla de Bandeja de entrada.
     */
    fun fetchAllConversations() {
        viewModelScope.launch {
            isLoading = true
            dmRepository.fetchAllConversations()
                .onSuccess {
                    conversations = it
                    isLoading = false
                }
                .onFailure {
                    errorMessage = it.message
                    isLoading = false
                }
        }
    }

    /**
     * Carga el historial de mensajes con un usuario específico
     * Se usará al entrar en la pantalla de chat con alguien.
     * @param otherUserId Identificador del usuario con el que queremos hablar.
     */
    fun fetchChatWithUser(otherUserId: Long) {
        viewModelScope.launch {
            isLoading = true
            dmRepository.fetchChatWithUser(otherUserId)
                .onSuccess { chatEntity ->
                    activeMessages = chatEntity.messages
                    isLoading = false
                }
                .onFailure {
                    errorMessage = it.message
                    isLoading = false
                }
        }
    }

    /**
     * Envía un mensaje dentro de una conversación abierta.
     * @param otherUserId Identificador del usuario con el que queremos hablar.
     * @param text Texto del mensaje.
     */
    fun sendMessage(otherUserId: Long, text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            dmRepository.sendMessage(otherUserId, text)
                .onSuccess {
                    // Refrescamos los mensajes para que aparezca el nuevo
                    fetchChatWithUser(otherUserId)
                }
                .onFailure {
                    errorMessage = "No se pudo enviar el mensaje"
                }
        }
    }

    /**
     * Borra una conversación completa
     * @param otherUserId Identificador del usuario con el que queremos borrar la conversación.
     */
    fun deleteChat(otherUserId: Long) {
        viewModelScope.launch {
            dmRepository.deleteConversation(otherUserId)
                .onSuccess {
                    fetchAllConversations()
                    activeMessages = emptyList() // Limpiamos si estábamos dentro del chat
                }
        }
    }

    fun clearError() { errorMessage = null }
    fun clearChat() {
        activeMessages = emptyList()
        errorMessage = null
    }
}

