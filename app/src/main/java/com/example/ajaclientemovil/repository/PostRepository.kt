package com.example.ajaclientemovil.repository

import android.content.Context
import com.example.ajaclientemovil.data.PostEntityDTO
import com.example.ajaclientemovil.data.PostNewDTO
import com.example.ajaclientemovil.network.NetworkManager
import com.example.ajaclientemovil.network.SessionManager

/**
 * Repositorio encargado de gestionar los datos de posts.
 * @param context El contexto de la aplicación necesario para acceder a archivos.
 */
class PostRepository(private val context: Context) {
    private val apiService = NetworkManager.apiService
    private fun getToken() = "JWT_TOKEN=${SessionManager.getToken(context)}"

    /**
     * Obtiene todos los posts de un tema específico.
     * @param topicId Identificador del tema del que se quieren obtener los posts.
     * @return [Result] con la lista de posts
     */
    suspend fun getPostsByTopic(topicId: Long): Result<List<PostEntityDTO>> {
        return try {
            val token = SessionManager.getToken(context) ?: return Result.failure(Exception("Sin sesión"))
            val response = apiService.getAllPosts("JWT_TOKEN=$token")

            if (response.isSuccessful && response.body() != null) {
                val filtered = response.body()!!.message
                    .filter { it.topic?.id == topicId }
                    .sortedBy { it.messageNumber }
                Result.success(filtered)
            } else Result.failure(Exception("Error al obtener mensajes"))
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Crea un nuevo post (mensaje) dentro de un tema específico.
     * @param text Contenido del mensaje.
     * @param topicId Identificador del tema al que pertenece el mensaje.
     * @return [Result] con el resultado de la operación.
     */
    suspend fun createPost(text: String, topicId: Long): Result<Unit> {
        return try {
            val token = SessionManager.getToken(context) ?: return Result.failure(Exception("Sin sesión"))
            val response = apiService.createPost("JWT_TOKEN=$token", PostNewDTO(text, topicId))
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error al enviar"))
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Elimina un post específico.
     * @param postId Identificador del post a eliminar.
     * @return [Result] con el resultado de la operación.
     */
    suspend fun deletePost(postId: Long): Result<Unit> {
        return try {
            val response = apiService.deletePost(getToken(), postId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al borrar el post"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza un post existente.
     * @param post Objeto [PostEntityDTO] con los datos actualizados del post.
     * @return [Result] con el resultado de la operación.
     */
    suspend fun updatePost(post: PostEntityDTO): Result<Unit> {
        return try {
            val response = apiService.editPost(getToken(), post)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al actualizar el post"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}