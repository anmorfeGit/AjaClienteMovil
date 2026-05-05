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

    suspend fun sendMessage(idUserTo: Long, text: String): Result<String> {
        return try {
            val cookie = getAuthCookie() ?: return Result.failure(Exception("Sesión expirada"))
            val dto = DirectMessageNewDTO(idUserTo, text)
            val response = apiService.sendDirectMessage(cookie, dto)

            if (response.isSuccessful) {
                // El backend devuelve un Map, extraemos el campo "message"
                val msg = response.body()?.get("message")?.toString() ?: "Enviado"
                Result.success(msg)
            } else {
                Result.failure(Exception("Error al enviar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAllConversations(): Result<List<DirectMessageChatEntity>> {
        return try {
            val cookie = getAuthCookie() ?: return Result.failure(Exception("Sin sesión"))
            val response = apiService.getAllConversations(cookie) // Usa DMListResponse

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: emptyList())
            } else {
                Result.failure(Exception("Error al cargar conversaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchChatWithUser(otherUserId: Long): Result<DirectMessageChatEntity> {
        return try {
            // 1. Obtenemos la cookie de sesión
            val cookie = getAuthCookie() ?: return Result.failure(Exception("Sesión expirada"))

            // 2. Llamada a la API usando el nuevo DTO DMSingleResponse
            val response = apiService.getConversationWithUser(cookie, otherUserId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body.message)
                } else {
                    Result.failure(Exception("Error del servidor: ${body?.success ?: "Sin respuesta"}"))
                }
            } else {
                // Manejo de errores según el código HTTP (404, 500, etc.)
                Result.failure(Exception("Error al obtener el chat: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Captura errores de red (sin internet, timeout, etc.)
            Result.failure(e)
        }
    }

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