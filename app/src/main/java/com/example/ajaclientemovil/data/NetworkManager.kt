package com.example.ajaclientemovil.data

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Gestor central de comunicaciones de red.
 * Implementa el protocolo de autenticación basado en JWT encapsulado en Cookies,
 * siguiendo la implementación específica del servidor.
 */
object NetworkManager {

    private const val BASE_URL = "https://ajaserver.mel0n.dev"
    private const val TAG = "NetworkManager"

    /**
     * Realiza la autenticación del usuario mediante POST (x-www-form-urlencoded).
     * * Proceso técnico:
     * 1. Envía credenciales codificadas en UTF-8.
     * 2. Captura el token de la cabecera 'Set-Cookie' (formato JWT_TOKEN=...).
     * 3. Deserializa el JSON de respuesta en un objeto [LoginDTO].
     * * @return Un Pair con los datos del usuario y el token, o null si falla.
     */
    suspend fun login(user: String, pass: String): Pair<LoginDTO, String?>? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/auth/login")
            val conn = url.openConnection() as HttpURLConnection

            // Configuración de cabeceras para envío de formulario
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.doOutput = true

            // Codificación de seguridad para caracteres especiales
            val formData = "username=${URLEncoder.encode(user, "UTF-8")}&password=${URLEncoder.encode(pass, "UTF-8")}"

            // Escritura de datos en el cuerpo de la petición
            conn.outputStream.use { it.write(formData.toByteArray()) }

            Log.d(TAG, "Login Response Code: ${conn.responseCode}")

            if (conn.responseCode == 200) {
                // EXTRACCIÓN DEL TOKEN
                // El servidor envía el token dentro de la cabecera "Set-Cookie"
                // en el formato: "JWT_TOKEN=xxx.yyy.zzz; Path=/; ..."
                val cookieHeader = conn.headerFields["Set-Cookie"]
                val token = cookieHeader?.firstOrNull { it.contains("JWT_TOKEN") }
                    ?.split(";")?.get(0) // Obtenemos "JWT_TOKEN=xxx"
                    ?.split("=")?.get(1) // Obtenemos solo el código "xxx"

                // Lectura y traducción del JSON
                val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
                val loginData = Gson().fromJson(responseBody, LoginDTO::class.java)

                return@withContext Pair(loginData, token)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login: ${e.message}")
        }
        null
    }

    /**
     * Realiza peticiones GET autenticadas a recursos protegidos.
     * * @param urlPath Ruta del endpoint (ej: "/api/user").
     * @param token El JWT obtenido en el login.
     * @return El cuerpo de la respuesta como String o null si hay error (403, 401, etc).
     */
    suspend fun getProtectedData(urlPath: String, token: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL$urlPath")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"

            // INYECCIÓN DE CREDENCIALES
            // Cumplimos el requisito del servidor: El token debe viajar como una Cookie
            conn.setRequestProperty("Cookie", "JWT_TOKEN=$token")

            if (conn.responseCode == 200) {
                return@withContext conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                Log.e(TAG, "Acceso denegado o error: ${conn.responseCode}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error de red: ${e.message}")
        }
        null
    }
}