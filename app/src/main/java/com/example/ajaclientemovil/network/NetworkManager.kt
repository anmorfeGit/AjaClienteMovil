package com.example.ajaclientemovil.network

import com.example.ajaclientemovil.data.UserEntityDTO
import com.example.ajaclientemovil.data.network.AjaApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import com.google.gson.Gson
import org.json.JSONObject

/**
 * Cliente de red centralizado para la aplicación AJA.
 * Implementa Retrofit 2 para la gestión de peticiones HTTP y OkHttp para la capa de transporte.
 * * Características incluidas:
 * 1. Soporte HTTPS con gestión de certificados (Seguro/Inseguro).
 * 2. Interceptor de Log para depuración de cabeceras y cuerpo JSON.
 * 3. Gestión manual de Cookies para compatibilidad con el JWT del servidor.
 */
object NetworkManager {
    // URL base del servidor de Alex con protocolo seguro
    private const val BASE_URL = "https://ajaserver.mel0n.dev"

    /**
     * Configura un cliente HTTP capaz de ignorar la validación de certificados SSL.
     * Útil para entornos de desarrollo local o cuando el certificado presenta problemas de confianza.
     * Basado en la implementación sugerida para el servidor AJA (ignorar SSLContext).
     */
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        // Creamos un gestor de confianza que no valida la cadena de certificados
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        // Instalamos el gestor de confianza en un contexto SSL de tipo TLS
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        // Añadimos un interceptor para ver las peticiones en el Logcat (Consola)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true } // Permite dominios que no coincidan con el certificado
            .addInterceptor(logging)
            .build()
    }

    /**
     * Inicialización de la instancia de Retrofit.
     * Se utiliza el cliente "Unsafe" para garantizar la conexión con el servidor de desarrollo.
     */
    private val client = getUnsafeOkHttpClient()
    private val gson = Gson()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create()) // Conversión automática JSON -> Data Class
        .build()

    // Servicio que expone los métodos definidos en la interfaz AjaApiService
    val apiService: AjaApiService = retrofit.create(AjaApiService::class.java)

    /**
     * Realiza la petición de autenticación.
     * A diferencia de una API REST estándar, el servidor de Alex envía el JWT
     * dentro de una cabecera 'Set-Cookie' en lugar de un campo en el JSON.
     * @param user Nombre de usuario.
     * @param pass Contraseña.
     * @return Retorna un Triple con los siguientes valores:
     * 1. UserEntityDTO: Si el login es correcto, contiene los datos del usuario, sino devuelve null.
     * 2. String: Si el login es incorrecto, contiene el mensaje de error, sino devuelve null.
     * 3. String: Si el login es correcto, contiene el token JWT, sino devuelve null.
     */


    suspend fun login(user: String, pass: String): Triple<UserEntityDTO?, String?, String?> {
        return try {
            val response = apiService.login(user, pass)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                if (body.success) {
                    // LOGIN EXITOSO
                    val cookieHeader = response.headers()["Set-Cookie"]
                    val token = cookieHeader?.split(";")?.firstOrNull { it.contains("JWT_TOKEN") }
                        ?.split("=")?.get(1)

                    // Conversión manual de Any a UserEntityDTO usando el estándar del servidor
                    val jsonUser = gson.toJson(body.message)
                    val userDto = gson.fromJson(jsonUser, UserEntityDTO::class.java)

                    Triple(userDto, null, token)
                } else {
                    // ERROR CONTROLADO (Usuario no existe, etc.)
                    // Según el estándar del servidor, si success es false, message es String.
                    Triple(null, body.message.toString(), null)
                }
            } else {
                // ERROR HTTP (401, 500...)
                val errorMsg = response.errorBody()?.string()?.let {
                    JSONObject(it).optString("message", "Error de autenticación")
                } ?: "Error en el servidor"
                Triple(null, errorMsg, null)
            }
        } catch (e: Exception) {
            Triple(null, "Fallo de conexión: ${e.localizedMessage}", null)
        }
    }
    /**
     * Realiza la desconexión del usuario de forma sincronizada con el servidor.
     * * Esta función es de tipo 'suspend' para no bloquear el hilo principal de la UI
     * durante la petición de red. Envía una solicitud POST al endpoint de logout
     * para que el servidor de Spring Boot (AuthController) invalide la sesión
     * (JSESSIONID) en el backend.
     * * @return Boolean: 'true' si el servidor confirmó el cierre de sesión,
     * 'false' en caso de error de red o credenciales inválidas.
     */
    suspend fun logout(): Boolean {
        return try {
            // Ejecución de la llamada POST /api/auth/logout definida en AjaApiService
            val response = apiService.logout()

            if (response.isSuccessful) {
                // Se verifica que el diccionario de respuesta contenga success: true
                response.body()?.get("success") == true
            } else {
                // Manejo de errores de protocolo (ej: 401 Unauthorized o 500 Server Error)
                false
            }
        } catch (e: Exception) {
            // Captura de excepciones críticas (pérdida de conexión, timeout, etc.)
            false
        }
    }


}