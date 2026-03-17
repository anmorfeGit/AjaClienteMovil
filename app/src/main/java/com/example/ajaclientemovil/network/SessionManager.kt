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
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"



    /**
     * Guarda la sesión tras un login exitoso.
     * @param context Contexto de la actividad o aplicación.
     * @param token El JWT recibido en la cabecera Set-Cookie del servidor.
     * @param user El objeto UserEntityDTO que contiene el nombre de usuario y el rol.
     */
    fun saveSession(context: Context, token: String, user: UserEntityDTO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        prefs.putString(KEY_JWT_TOKEN, token)
        prefs.putString(KEY_USERNAME, user.username)
        prefs.putString(KEY_USER_ROLE, user.role)
        prefs.putString(KEY_EMAIL, user.email)
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
     */
    fun getUsername(context: Context): String {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_USERNAME, null) ?: ""
    }

    fun getEmail(context: Context): String {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_EMAIL, null) ?: ""
    }




}