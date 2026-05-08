package com.example.ajaclientemovil.repository

import android.content.Context
import com.example.ajaclientemovil.data.DirectMessageChatEntity
import com.example.ajaclientemovil.data.DirectMessageNewDTO
import com.example.ajaclientemovil.data.network.AjaApiService
import com.example.ajaclientemovil.network.SessionManager

class DirectMessageRepository(
    private val apiService: AjaApiService,
    private val context: Context
) {
    /**
     * Recupera el token de sesión formateado para la cabecera Cookie
     * @return El token o null si no existe
     */
    private fun getAuthCookie(): String? {
        val token = SessionManager.getToken(context)
        return if (token != null) "JWT_TOKEN=$token" else null
    }

    /**
     * Envía un mensaje directo a otro usuario.
     * @param idUserTo ID del usuario al que se dirige el mensaje.
     * @param text Texto del mensaje.
     * @return Un Result con el mensaje de éxito o error.
     */
    suspend fun sendMessage(idUserTo: Long, text: String): Result<String> {
        return try {
            val cookie = getAuthCookie() ?: return Result.failure(Exception("Sesión expirada"))
            val dto = DirectMessageNewDTO(idUserTo, text)
            val response = apiService.sendDirectMessage(cookie, dto)

            if (response.isSuccessful) {
                val msg = response.body()?.get("message")?.toString() ?: "Enviado"
                Result.success(msg)
            } else {
                Result.failure(Exception("Error al enviar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene la lista de conversaciones.
     * @return Un Result con la lista de conversaciones o error.
     */
    suspend fun fetchAllConversations(): Result<List<DirectMessageChatEntity>> {
        return try {
            val cookie = getAuthCookie() ?: return Result.failure(Exception("Sin sesión"))
            val response = apiService.getAllConversations(cookie)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: emptyList())
            } else {
                Result.failure(Exception("Error al cargar conversaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el chat con un usuario específico.
     * @param otherUserId ID del usuario con el que se obtendrá el chat.
     * @return Un Result con el chat o error.
     */
    suspend fun fetchChatWithUser(otherUserId: Long): Result<DirectMessageChatEntity> {
        return try {
            val cookie = getAuthCookie() ?: return Result.failure(Exception("Sesión expirada"))

            val response = apiService.getConversationWithUser(cookie, otherUserId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body.message)
                } else {
                    Result.failure(Exception("Error del servidor: ${body?.success ?: "Sin respuesta"}"))
                }
            } else {
                Result.failure(Exception("Error al obtener el chat: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una conversación.
     * @param otherUserId ID del usuario con el que se eliminará la conversación.
     * @return Un Result con el mensaje de éxito o error.
     */
    suspend fun deleteConversation(otherUserId: Long): Result<String> {
        return try {
            val cookie = getAuthCookie() ?: return Result.failure(Exception("Sin sesión"))
            val response = apiService.deleteConversation(cookie, otherUserId)

            if (response.isSuccessful) {
                Result.success("Conversación eliminada")
            } else {
                Result.failure(Exception("No se pudo eliminar el chat"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}