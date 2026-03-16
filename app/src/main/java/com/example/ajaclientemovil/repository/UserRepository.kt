package com.example.ajaclientemovil.repository

import android.content.Context
import com.example.ajaclientemovil.data.LoginDTO
import com.example.ajaclientemovil.data.UserEntityDTO
import com.example.ajaclientemovil.network.NetworkManager
import com.example.ajaclientemovil.network.SessionManager

/**
 * Repositorio encargado de gestionar los datos de usuario y la persistencia de sesión.
 * * Esta clase implementa el Patrón Repositorio, actuando como mediador entre
 * el servicio de red (NetworkManager) y el almacenamiento local (SessionManager).
 * @param context El contexto de la aplicación necesario para acceder a archivos.
 */
class UserRepository(private val context: Context) {

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
     * * El proceso sigue una estrategia de "limpieza doble":
     * 1. Invalidación Remota: Envía una petición POST al endpoint /api/auth/logout
     * para que el servidor destruya la sesión activa (JSESSIONID) en el backend.
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
}