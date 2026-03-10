package com.example.ajaclientemovil.data.repository

import android.content.Context
import com.example.ajaclientemovil.data.LoginDTO
import com.example.ajaclientemovil.data.Network
import com.example.ajaclientemovil.data.NetworkManager
import com.example.ajaclientemovil.data.SessionManager

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
     * @return [LoginDTO] con los datos del perfil si el login es correcto, o null en caso de fallo.
     */
    suspend fun performLogin(user: String, pass: String): LoginDTO? {
        // Llamamos a la funcion login de NetworkManager.kt y alojamos en una variable el resultado.
        val resultado = NetworkManager.login(user, pass)

        /* Si el resultado no es nulo, significa que el servidor ha devuelto una respuesta.
        Se utiliza la desestructuración de objetos Pair para asignar de forma simultánea tanto el
        objeto de respuesta DTO como el identificador de sesión.
        */
        if (resultado != null) {
            val (loginData, token) = resultado

            if (loginData.success) {
                /* Si loginData.success es true, significa que el login ha sido exitoso.
                Se llama a la función saveSession de SessionManager.kt para guardar los datos
                de sesión en el almacenamiento local.
                 */
                SessionManager.saveSession(
                    context = context,
                    token = token,
                    role = loginData.message.role,
                    username = loginData.message.username
                )
                return loginData
            }
        }
        // Si llegamos aquí, algo ha fallado (red, contraseña mal, etc.).
        return null
    }

    /**
     * Ejecuta el cierre de sesión del usuario.
     * * Borra todos los rastros de la sesión actual en el dispositivo.
     */
    fun logout() {
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