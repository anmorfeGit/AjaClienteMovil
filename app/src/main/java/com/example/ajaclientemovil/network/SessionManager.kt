package com.example.ajaclientemovil.network

import android.content.Context
import com.example.ajaclientemovil.data.UserEntityDTO

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
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_REGISTER_DATE = "register_date"


    /**
     * Guarda la sesión tras un login exitoso. Usa MODE_PRIVATE para mantener
     * la privacidad de los datos.
     * @param context Contexto de la actividad o aplicación.
     * @param token El JWT recibido en la cabecera Set-Cookie del servidor.
     * @param user El objeto UserEntityDTO que contiene el nombre de usuario y el rol.
     */
    fun saveSession(context: Context, token: String, user: UserEntityDTO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        prefs.putString(KEY_JWT_TOKEN, token)
        prefs.putLong(KEY_USER_ID, user.id ?: -1L)
        prefs.putString(KEY_USERNAME, user.username)
        prefs.putString(KEY_USER_ROLE, user.role)
        prefs.putString(KEY_EMAIL, user.email)
        prefs.putString(KEY_REGISTER_DATE, user.registerDate)
        prefs.apply()
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
     * @param context Contexto de la aplicación.
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
    /**
     * Recupera el nombre de usuario guardado.
     * Devuelve un String vacío si no existe para evitar errores en la UI.
     * @param context Contexto de la aplicación.
     * @return El nombre de usuario o un String vacío si no existe.
     */
    fun getUsername(context: Context): String {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_USERNAME, null) ?: ""
    }

    /**
     * Recupera el email guardado.
     * Devuelve un String vacío si no existe para evitar errores en la UI.
     * @param context Contexto de la aplicación.
     * @return El email o un String vacío si no existe.
     */
    fun getEmail(context: Context): String {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_EMAIL, null) ?: ""
    }
    /**
     * Reconstruye el objeto UserEntityDTO desde las preferencias
     * @param context Contexto de la aplicación.
     * @return El objeto UserEntityDTO o null si no existe.
     */
    fun getUser(context: Context): UserEntityDTO? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getLong(KEY_USER_ID, -1L)
        val username = prefs.getString(KEY_USERNAME, null) ?: return null

        return UserEntityDTO(
            id = id,
            username = username,
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            role = prefs.getString(KEY_USER_ROLE, "USER") ?: "USER",
            isActive = true,
            registerDate = prefs.getString(KEY_REGISTER_DATE, null) // <--- Recuperamos
        )
    }

    /**
     * Recupera la fecha de registro del usuario.
     * @param context Contexto de la aplicación.
     * @return La fecha de registro
     */
    fun getRegisterDate(context: Context): String {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_REGISTER_DATE, null) ?: "No disponible"
    }

}

