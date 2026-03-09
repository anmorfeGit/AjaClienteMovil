package com.example.ajaclientemovil.data

import android.content.Context

/**
 * Gestor de persistencia de sesión del usuario.
 * Esta clase se encarga de almacenar de forma segura y permanente los datos
 * recibidos del servidor (JSESSIONID y Rol).
 * * Cumple con el requisito del TEA2 de gestionar el estado de Login/Logout.
 */
object SessionManager {
    private const val PREFS_NAME = "aja_session_prefs"
    private const val KEY_SESSION_ID = "jsession_id"
    private const val KEY_ROLE = "user_role"

    /**
     * Guarda la información de sesión en el almacenamiento local (shared preferences).
     *
     * @param context El contexto de la aplicación necesario para acceder a archivos.
     * @param sessionId La id de la sesion (JSESSIONID) recibida del servidor.
     * @param role El tipo de usuario (ADMIN/USER).
     */
    fun saveSession(context: Context, sessionId: String?, role: String) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(KEY_SESSION_ID, sessionId)
            putString(KEY_ROLE, role)
            apply()
        }
    }

    /**
     * Elimina todos los datos de sesión (Logout).
     */
    fun clearSession(context: Context) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }

    /**
     * Recupera el rol del usuario almacenado.
     * @return El rol (ADMIN/USER) o null si no hay sesión activa.
     */
    fun getRole(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_ROLE, null)
    }
}