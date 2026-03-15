package com.example.ajaclientemovil.network

import android.content.Context

/**
 * Gestor de persistencia de sesión (JWT).
 * * Esta clase se encarga de almacenar el token de seguridad y el rol del usuario
 * en el almacenamiento privado del dispositivo (SharedPreferences).
 * * Adaptado según la implementación del servidor: el token se identifica como "JWT_TOKEN".
 */
object SessionManager {
    // Nombre del archivo de preferencias
    private const val PREFS_NAME = "aja_session_prefs"

    // Llaves para identificar los datos
    private const val KEY_JWT_TOKEN = "jwt_token"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USERNAME = "username"

    /**
     * Guarda la sesión tras un login exitoso.
     * @param context Contexto de la actividad o aplicación.
     * @param token El JWT recibido en la cabecera Set-Cookie del servidor.
     * @param role El rol del usuario (ADMIN/USER) extraído del LoginDTO.
     */
    fun saveSession(context: Context, token: String?, role: String, username: String) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            // Guardamos el token, el rol y el username en el archivo de preferencias.
            putString(KEY_JWT_TOKEN, token)
            putString(KEY_USER_ROLE, role)
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    /**
     * Recupera el token para incluirlo en las futuras peticiones al servidor.
     * Según el código de Alex, este token debe enviarse en la cabecera "Cookie".
     * @param context Contexto de la aplicación.
     * @return El token o null si no existe.
     */
    fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_JWT_TOKEN, null)
    }

    /**
     * Recupera el rol para gestionar permisos en la interfaz de usuario.
     * @param context Contexto de la aplicación.
     * @return El rol o null si no existe.
     */
    fun getRole(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_USER_ROLE, null)
    }

    /**
     * Elimina los datos de sesión (Logout).
     * * Al borrar el "JWT_TOKEN", las futuras peticiones al servidor de Alex
     * devolverán un error 403 (Prohibido), forzando el re-login.
     */
    fun clearSession(context: Context) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }

    /**
     * Comprueba si existe una sesión activa sin necesidad de llamar a red.
     * @param context Contexto de la aplicación.
     * @return true si existe una sesión activa, false en caso contrario.
     */
    fun isUserLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }
}