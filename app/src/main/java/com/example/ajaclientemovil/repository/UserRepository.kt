package com.example.ajaclientemovil.repository

import android.content.Context
import com.example.ajaclientemovil.data.LoginDTO
import com.example.ajaclientemovil.data.UserEntityDTO
import com.example.ajaclientemovil.data.UserRegisterDTO
import com.example.ajaclientemovil.network.NetworkManager
import com.example.ajaclientemovil.network.SessionManager

/**
 * Repositorio encargado de gestionar los datos de usuario y la persistencia de sesión.
 * * Esta clase implementa el Patrón Repositorio, actuando como mediador entre
 * el servicio de red (NetworkManager) y el almacenamiento local (SessionManager).
 * @param context El contexto de la aplicación necesario para acceder a archivos.
 */
class UserRepository(private val context: Context) {

    private val apiService = NetworkManager.apiService
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
            // Si el login es exitoso (success: true), persistimos la sesión
            SessionManager.saveSession(context, token, userDto)
            Result.success(userDto)
        } else {
            // Si falla, devolvemos el error (el String que Alex manda en 'message')
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
        // 1. Intentamos invalidar en el servidor (POST /api/auth/logout)
        // Es vital informar al servidor para que la cookie deje de ser válida.
        NetworkManager.logout()

        // 2. Limpiamos localmente pase lo que pase
        // Aunque la red falle, debemos asegurar que el usuario no pueda
        // seguir navegando por la app localmente.
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
            // 1. Recuperamos el token almacenado en SharedPreferences
            val token = SessionManager.getToken(context)

            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Sesión expirada o no válida"))
            }

            // 2. Realizamos la petición enviando el token en el formato que el servidor espera
            val response = apiService.getAllUsers("JWT_TOKEN=$token")

            if (response.isSuccessful && response.body() != null) {
                val userListDto = response.body()!!

                // 3. Verificamos el flag 'success' interno de la API de Alex
                if (userListDto.success) {
                    // Éxito total: devolvemos la lista de usuarios
                    Result.success(userListDto.message)
                } else {
                    // El servidor respondió pero success es false (ej: falta de permisos)
                    Result.failure(Exception("Error del servidor: Operación no permitida"))
                }
            } else {
                // Error HTTP (401 Unauthorized, 403 Forbidden, etc.)
                val code = response.code()
                Result.failure(Exception("Error de red (Código: $code)"))
            }
        } catch (e: Exception) {
            // Error de infraestructura (servidor caído, sin internet)
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
                // Extraemos el mensaje de error del servidor si existe
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

            // Construimos el objeto exacto que Alex espera
            val userToSend = currentUser.copy(
                username = newUsername,    // Ahora sí permitimos cambiar el nombre
                email = newEmail,          // Y el email
                password = currentPassword, // Enviamos la actual para verificar
                registerDate = currentUser.registerDate // Mantenemos la original
            )

            // Llamada al endpoint /api/user
            val response = apiService.updateUser("JWT_TOKEN=$token", userToSend)

            if (response.isSuccessful) {
                // Si el servidor responde OK, actualizamos nuestra copia local
                // (Sin guardar la contraseña en el SharedPreferences por seguridad)
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

            // Pasamos los DOS parámetros: el token para autorizar y el ID para identificar
            val response = apiService.deleteUser("JWT_TOKEN=$token", userId)

            if (response.isSuccessful) {
                // Verificamos si el usuario borrado es el actual para cerrar sesión
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
}

