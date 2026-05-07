package com.example.ajaclientemovil.repository

import android.content.Context
import com.example.ajaclientemovil.data.ForumEntityDTO
import com.example.ajaclientemovil.data.TopicEditDTO
import com.example.ajaclientemovil.data.TopicEntityDTO
import com.example.ajaclientemovil.data.TopicNewDTO
import com.example.ajaclientemovil.network.NetworkManager
import com.example.ajaclientemovil.network.SessionManager

/**
 * Repositorio encargado de gestionar los datos de foros y temas.
 * @param context El contexto de la aplicación necesario para acceder a archivos.
 */
class ForumRepository(private val context: Context) {
    private val apiService = NetworkManager.apiService
    private fun getToken() = "JWT_TOKEN=${SessionManager.getToken(context)}"

    /**
     * Obtiene la lista de foros disponibles.
     * @return [Result] con la lista de foros o un error.
     */
    suspend fun getForums(): Result<List<ForumEntityDTO>> {
        return try {
            val token = SessionManager.getToken(context) ?: return Result.failure(Exception("Sin sesión"))
            val response = apiService.getAllForums("JWT_TOKEN=$token")

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception("Error al cargar foros"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Guarda un foro en el servidor.
     * @param forum El foro a guardar.
     * @param isEdit Indica si se trata de una operación de edición.
     * @return [Result] con el resultado de la operación.
     */
    suspend fun saveForum(forum: ForumEntityDTO, isEdit: Boolean): Result<Unit> {
        return try {
            val response = if (isEdit) apiService.editForum(getToken(), forum)
            else apiService.createForum(getToken(), forum)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al guardar el foro"))
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Elimina un foro específico.
     * @param id Identificador del foro a eliminar.
     * @return [Result] con el resultado de la operación.
     */
    suspend fun deleteForum(id: Long): Result<Unit> {
        return try {
            val response = apiService.deleteForum(getToken(), id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al eliminar"))
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Obtiene los temas de un foro específico.
     * @param forumId Identificador del foro del que se quieren obtener los temas.
     * @return [Result] con la lista de temas o un error.
     */
    suspend fun getTopicsByForum(forumId: Long): Result<List<TopicEntityDTO>> {
        return try {
            val token = SessionManager.getToken(context) ?: return Result.failure(Exception("Sin sesión"))
            val response = apiService.getAllTopics("JWT_TOKEN=$token")

            if (response.isSuccessful && response.body() != null) {
                val filteredTopics = response.body()!!.message.filter { it.forum.id == forumId }
                Result.success(filteredTopics)
            } else {
                Result.failure(Exception("Error al cargar temas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crea un nuevo tema en un foro específico.
     * @param title Título del nuevo tema.
     * @param forumId Identificador del foro al que pertenece el tema.
     * @return [Result] con el resultado de la operación.
     */
    suspend fun createTopic(title: String, forumId: Long): Result<String> {
        return try {
            val token = SessionManager.getToken(context) ?: return Result.failure(Exception("Sin sesión"))
            val response = apiService.createTopic("JWT_TOKEN=$token", TopicNewDTO(title, forumId))
            if (response.isSuccessful) Result.success("Tema creado")
            else Result.failure(Exception("Error al crear tema"))
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Elimina un tema específico.
     * @param topicId Identificador del tema a eliminar.
     * @return [Result] con el resultado de la operación.
     */
    suspend fun deleteTopic(topicId: Long): Result<String> {
        return try {
            val token = SessionManager.getToken(context) ?: return Result.failure(Exception("Sin sesión"))
            val response = apiService.deleteTopic("JWT_TOKEN=$token", topicId)
            if (response.isSuccessful) Result.success("Tema eliminado")
            else Result.failure(Exception("No tienes permiso o error del servidor"))
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Edita un tema específico.
     * @param editDTO Datos de edición del tema.
     * @return [Result] con el resultado de la operación.
     */
    suspend fun editTopic(editDTO: TopicEditDTO): Result<Unit> {
        return try {
            val token = SessionManager.getToken(context) ?: return Result.failure(Exception("Sin sesión"))
            val response = apiService.editTopic("JWT_TOKEN=$token", editDTO)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al editar: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}