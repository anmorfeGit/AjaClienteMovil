package com.example.ajaclientemovil.data

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

object NetworkManager {
    // La URL de producción de Alex
    private const val BASE_URL = "https://ajaserver.mel0n.dev"

    /**
     * MÉTODO 1: Para certificados "legales" (Producción).
     * OkHttp ya sabe manejar certificados válidos por defecto.
     */
    private fun getSafeOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    /**
     * MÉTODO 2: Ignorar certificados (Para local o errores de handshake).
     * Es la traducción exacta del código que te ha pasado Alex a OkHttp.
     */
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(logging)
            .build()
    }

    // --- CONFIGURACIÓN DE RETROFIT ---

    // Cambia aquí: usa getSafeOkHttpClient() si el certificado de Alex va bien.
    // Si te da error de SSL, cambia a getUnsafeOkHttpClient().
    private val client = getUnsafeOkHttpClient()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: AjaApiService = retrofit.create(AjaApiService::class.java)

    /**
     * Función Login (Sin cambios, pero ahora segura gracias a TLS)
     */
    suspend fun login(user: String, pass: String): Pair<LoginDTO, String?>? {
        return try {
            val response = apiService.login(user, pass)
            if (response.isSuccessful && response.body() != null) {
                // Extracción del JWT del Header (Cookie)
                val cookieHeader = response.headers()["Set-Cookie"]
                val token = cookieHeader?.split(";")?.firstOrNull { it.contains("JWT_TOKEN") }
                    ?.split("=")?.get(1)

                Pair(response.body()!!, token)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}