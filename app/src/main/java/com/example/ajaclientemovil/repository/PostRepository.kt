package com.example.ajaclientemovil.repository

import android.content.Context
import com.example.ajaclientemovil.data.PostEntityDTO
import com.example.ajaclientemovil.data.PostNewDTO
import com.example.ajaclientemovil.network.NetworkManager
import com.example.ajaclientemovil.network.SessionManager

class PostRepository(private val context: Context) {
    private val apiService = NetworkManager.apiService

    suspend fun getPostsByTopic(topicId: Long): Result<List<PostEntityDTO>> {
        return try {
            val token = SessionManager.getToken(context) ?: return Result.failure(Exception("Sin sesión"))
            val response = apiService.getAllPosts("JWT_TOKEN=$token")

            if (response.isSuccessful && response.body() != null) {
                // Filtramos por el ID del Topic y ordenamos por número de mensaje
                val filtered = response.body()!!.message
                    .filter { it.topic.id == topicId }
                    .sortedBy { it.messageNumber }
                Result.success(filtered)
            } else Result.failure(Exception("Error al obtener mensajes"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun createPost(text: String, topicId: Long): Result<Unit> {
        return try {
            val token = SessionManager.getToken(context) ?: return Result.failure(Exception("Sin sesión"))
            val response = apiService.createPost("JWT_TOKEN=$token", PostNewDTO(text, topicId))
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error al enviar"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // com.example.ajaclientemovil.repository

    class PostRepository(private val context: android.content.Context) {
        private val apiService = com.example.ajaclientemovil.network.NetworkManager.apiService

        /**
         * Crea un nuevo post (mensaje) dentro de un tema específico.
         */
        suspend fun createPost(text: String, topicId: Long): Result<String> {
            return try {
                val token = com.example.ajaclientemovil.network.SessionManager.getToken(context)
                    ?: return Result.failure(Exception("Sesión expirada"))

                val postNewDTO = com.example.ajaclientemovil.data.PostNewDTO(text, topicId)
                val response = apiService.createPost("JWT_TOKEN=$token", postNewDTO)

                if (response.isSuccessful) {
                    // El servidor devuelve un diccionario con "message"
                    val msg = response.body()?.get("message")?.toString() ?: "Post creado con éxito"
                    Result.success(msg)
                } else {
                    Result.failure(Exception("Error al crear el post: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        /**
         * Elimina un post específico mediante su ID.
         * Solo funcionará si el usuario es Admin o el autor del post (validado por el servidor).
         */
        suspend fun deletePost(postId: Long): Result<String> {
            return try {
                val token = com.example.ajaclientemovil.network.SessionManager.getToken(context)
                    ?: return Result.failure(Exception("Sesión expirada"))

                val response = apiService.deletePost("JWT_TOKEN=$token", postId)

                if (response.isSuccessful) {
                    val msg = response.body()?.get("message")?.toString() ?: "Post eliminado"
                    Result.success(msg)
                } else {
                    // Aquí capturamos si el servidor devuelve un 403 (No autorizado)
                    val errorMsg = when(response.code()) {
                        403 -> "No tienes permiso para eliminar este mensaje"
                        404 -> "El mensaje ya no existe"
                        else -> "Error al eliminar: ${response.code()}"
                    }
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        /**
         * Edita el contenido de un post existente.
         */
        suspend fun editPost(postId: Long, newText: String): Result<String> {
            return try {
                val token = com.example.ajaclientemovil.network.SessionManager.getToken(context)
                    ?: return Result.failure(Exception("Sesión expirada"))

                val postEditDTO = com.example.ajaclientemovil.data.PostEditDTO(postId, newText)
                val response = apiService.editPost("JWT_TOKEN=$token", postEditDTO)

                if (response.isSuccessful) {
                    Result.success("Mensaje actualizado correctamente")
                } else {
                    Result.failure(Exception("Error al editar el mensaje"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}