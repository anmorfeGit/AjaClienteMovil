package com.example.ajaclientemovil.data

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object NetworkManager {

    private const val BASE_URL = "https://ajaserver.mel0n.dev/api/auth"

    /**
     * Realiza el login contra el servidor.
     * @param user nombre de usuario para hacer la petición de login
     * @param pass contraseña de usuario para hacer la petición de login
     * @return Pair con los datos del usuario y la cookie de sesión (JSESSIONID).
     */
    suspend fun login(user: String, pass: String): Pair<LoginDTO, String?>? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/login")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.doOutput = true

            // Damos el formato a los datos para que el servidor pueda utilizarlos.
            val formData = "username=${URLEncoder.encode(user, "UTF-8")}&password=${URLEncoder.encode(pass, "UTF-8")}"

            conn.outputStream.use { it.write(formData.toByteArray()) }

            if (conn.responseCode == 200) {
                // Extraemos el JSESSIONID necesario para mantener la sesión y para el Logout
                val cookieHeader = conn.headerFields["Set-Cookie"]
                val sessionId = cookieHeader?.firstOrNull { it.contains("JSESSIONID") }
                    ?.split(";")?.get(0)?.split("=")?.get(1)

                val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
                val loginData = Gson().fromJson(responseBody, LoginDTO::class.java)

                return@withContext Pair(loginData, sessionId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }
}
