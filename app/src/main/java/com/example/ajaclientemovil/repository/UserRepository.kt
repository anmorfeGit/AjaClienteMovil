package com.example.ajaclientemovil.repository

import android.content.Context
import com.example.ajaclientemovil.data.LoginDTO
import com.example.ajaclientemovil.data.UserEntityDTO
import com.example.ajaclientemovil.data.UserEntityDmDTO
import com.example.ajaclientemovil.data.UserRegisterDTO
import com.example.ajaclientemovil.data.network.AjaApiService
import com.example.ajaclientemovil.network.NetworkManager
import com.example.ajaclientemovil.network.SessionManager

/**
 * Repositorio encargado de gestionar los datos de usuario y la persistencia de sesión.
 * * Esta clase implementa el Patrón Repositorio, actuando como mediador entre
 * el servicio de red (NetworkManager) y el almacenamiento local (SessionManager).
 * @param context El contexto de la aplicación necesario para acceder a archivos.
 */
class UserRepository(
    private val apiService: AjaApiService,
    private val context: Context
){


    /**
     * Gestiona el proceso de inicio de sesión.
     * * Pasos internos:
     * 1. Solicita la autenticación al NetworkManager (hacia el servidor).
     * 2. Si la respuesta es exitosa, extrae el JSESSIONID y el Rol.
     * 3. Persiste estos datos en el SessionManager para futuras consultas.
     * @param user Nombre de usuario introducido.
     * @param pass Contraseña introducida.
     * @return [com.example.ajaclientemovil.data.LoginDTO] con los datos del perfil si el login es correcto, o null en caso de fallo.
     */
    suspend fun performLogin(user: String, pass: String): Result<UserEntityDTO> {
        val (userDto, error, token) = NetworkManager.login(user, pass)

        return if (userDto != null && token != null) {
            SessionManager.saveSession(context, token, userDto)
            Result.success(userDto)
        } else {
            Result.failure(Exception(error ?: "Error desconocido"))
        }
    }

    /**
     * Realiza el cierre de sesión seguro del usuario.
     * 1. Invalidación Remota: Envía una petición POST al endpoint /api/auth/logout
     * para que el servidor destruya la sesión del usuario.
     * 2. Limpieza Local: Elimina los tokens, roles y datos de usuario almacenados
     * en las SharedPreferences a través del SessionManager.
     * * Se utiliza el modificador 'suspend' para asegurar que la llamada de red no
     * bloquee el hilo principal de la interfaz de usuario (UI Thread).
     */
    suspend fun performLogout() {
        NetworkManager.logout()
        SessionManager.clearSession(context)
    }

    /**
     * Recupera el rol del usuario que está actualmente en memoria.
     * * Útil para decidir qué elementos de la UI mostrar (Botones de Admin vs Usuario).
     * @return El rol ("ADMIN" o "USER") o null si no hay nadie logueado.
     */
    fun getCurrentRole(): String? {
        return SessionManager.getRole(context)
    }

    /**
     * Recupera todos los usuarios del sistema consultando la API.
     * @return Result con la lista de usuarios en caso de éxito, o una excepción en caso de error.
     */
    suspend fun getAllUsers(): Result<List<UserEntityDTO>> {
        return try {
            val token = SessionManager.getToken(context)

            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Sesión expirada o no válida"))
            }
            val response = apiService.getAllUsers("JWT_TOKEN=$token")

            if (response.isSuccessful && response.body() != null) {
                val userListDto = response.body()!!
                if (userListDto.success) {
                    Result.success(userListDto.message)
                } else {
                    Result.failure(Exception("Error del servidor: Operación no permitida"))
                }
            } else {
                val code = response.code()
                Result.failure(Exception("Error de red (Código: $code)"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.localizedMessage}"))
        }
    }

    /**
     * Envía los datos de un nuevo usuario al servidor.
     * @param username Nombre de usuario.
     * @param email Email del usuario.
     * @param pass Contraseña del usuario.
     * @return [Result] con el mensaje de éxito o error.
     */
    suspend fun performRegister(username: String, email: String, pass: String): Result<String> {
        return try {
            val registerData = UserRegisterDTO(username, email, pass)
            val response = apiService.register(registerData)

            if (response.isSuccessful) {
                Result.success("Usuari registrat correctament")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error en el registre"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Modifica los datos del usuario actual y actualiza la caché local.
     * @param newUsername Nuevo nombre de usuario.
     * @param newEmail Nuevo email.
     * @param currentPassword Contraseña actual.
     * @return [Result] con el usuario actualizado o error.
     */
    suspend fun updateProfile(newUsername: String, newEmail: String, currentPassword: String): Result<UserEntityDTO> {
        return try {
            val token = SessionManager.getToken(context) ?: throw Exception("Sesión no válida")
            val currentUser = SessionManager.getUser(context) ?: throw Exception("Usuario no encontrado")

            val userToSend = currentUser.copy(
                username = newUsername,
                email = newEmail,
                password = currentPassword,
                registerDate = currentUser.registerDate
            )

            val response = apiService.updateUser("JWT_TOKEN=$token", userToSend)

            if (response.isSuccessful) {
                val updatedUser = userToSend.copy(password = null)
                SessionManager.saveSession(context, token, updatedUser)
                Result.success(updatedUser)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error de validación"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /** Elimina un usuario del sistema.
     * Si el ID coincide con el usuario actual, se limpia la sesión local.
     * @param userId Identificador único del usuario.
     * @return [Result] con el estado de la operación.
     */
    suspend fun deleteUser(userId: Long): Result<Unit> {
        return try {
            val token = SessionManager.getToken(context) ?: throw Exception("Sesión no válida")

            val response = apiService.deleteUser("JWT_TOKEN=$token", userId)

            if (response.isSuccessful) {
                val currentUser = SessionManager.getUser(context)
                if (currentUser?.id == userId) {
                    SessionManager.clearSession(context)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.localizedMessage}"))
        }
    }

    /**
     * Cambia el estado de un usuario (activo/inactivo).
     * @param userId Identificador único del usuario.
     * @param enable Indica si se debe habilitar o deshabilitar el usuario.
     * @return [Result] con el estado de la operación.
     */
    suspend fun toggleUserStatus(userId: Long, enable: Boolean): Result<Unit> {
        return try {
            val token = SessionManager.getToken(context) ?: throw Exception("Sesión no válida")
            val response = if (enable) {
                apiService.enableUser("JWT_TOKEN=$token", userId)
            } else {
                apiService.disableUser("JWT_TOKEN=$token", userId)
            }

            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al cambiar estado: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Cambia el role del usuario al role indicado (ADMIN o USER).
     *
     * El métodó decide qué endpoint llamar según el valor de [toAdmin]:
     * - true  → PUT /user/{id}/roles/admin
     * - false → PUT /user/{id}/roles/user
     *
     * @param userId  Identificador único del usuario cuyo role se modificará.
     * @param toAdmin true para promover a ADMIN, false para degradar a USER.
     * @return [Result] con un mensaje de texto en caso de éxito,
     *         o una excepción descriptiva en caso de error.
     */

    suspend fun updateUserRole(userId: Long, isAdmin: Boolean): Result<String> {
        return try {
            val token = SessionManager.getToken(context) ?: throw Exception("Sesión expirada")
            val cookieHeader = "JWT_TOKEN=$token"

            val response = if (isAdmin) {
                apiService.setRoleAdmin(cookieHeader, userId)
            } else {
                apiService.setRoleUser(cookieHeader, userId)
            }

            if (response.isSuccessful) {
                Result.success("Rol actualizado")
            } else {
                Result.failure(Exception("Error servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene una lista de usuarios disponibles para ser añadidos a una conversación.
     * @return [Result] con la lista de usuarios o una excepción en caso de error.
     */
    suspend fun getUsersForDM(): Result<List<UserEntityDmDTO>> {
        val token = SessionManager.getToken(context) ?: throw Exception("Sesión expirada")
        val cookieHeader = "JWT_TOKEN=$token"
        return try {
            val response = apiService.getUsersForDM(cookieHeader)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body.message)
                } else {
                    Result.failure(Exception("Error en la respuesta del servidor"))
                }
            } else {
                Result.failure(Exception("Error de red: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

