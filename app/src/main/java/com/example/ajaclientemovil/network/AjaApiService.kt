package com.example.ajaclientemovil.data.network

import com.example.ajaclientemovil.data.LoginDTO
import com.example.ajaclientemovil.data.UserListDTO
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Interfaz de Retrofit que define los puntos de acceso (endpoints) de la API de autenticación.
 * Se encarga de la comunicación asíncrona con el backend de Spring Boot.
 */
interface AjaApiService {

    /**
     * Realiza una petición de inicio de sesión.
     * @param user Nombre de usuario enviado como parámetro de formulario.
     * @param pass Contraseña enviada como parámetro de formulario.
     * @return Objeto [Response] que envuelve un [LoginDTO].
     * * Nota técnica: Se utiliza @FormUrlEncoded y @Field porque el controlador del servidor
     * espera los datos mediante @RequestParam (tipo x-www-form-urlencoded).
     * Usamos Response<> para poder interceptar las cabeceras, como el JSESSIONID.
     */
    @FormUrlEncoded
    @POST("/api/auth/login")
    suspend fun login(
        @Field("username") user: String,
        @Field("password") pass: String
    ): Response<LoginDTO>

    /**
     * Realiza una petición de cierre de sesión en el servidor.
     * @return Una respuesta que contiene un mapa con el estado de la operación (success/message).
     * * Nota técnica: Al usar @POST en la ruta "/api/auth/logout", el servidor invalida la
     * sesión vinculada a la Cookie (JSESSIONID) enviada automáticamente en la cabecera
     * por el cliente OkHttp. No requiere parámetros de cuerpo ya que la identificación
     * se basa en el estado de la sesión en el servidor.
     */
    @POST("/api/auth/logout")
    suspend fun logout(): Response<Map<String, Any>>

    /**
     * Obtiene la lista completa de usuarios registrados en el sistema.
     * @param authCookie Cadena que contiene el JWT en formato "JWT_TOKEN=valor".
     * @return Objeto UserListDTO que contiene el flag de éxito y la lista de usuarios.
     * * NOTA DE SEGURIDAD: Este endpoint requiere que el JWT sea válido y
     * que el usuario tenga permisos suficientes (según la lógica del backend).
     */
    @GET("api/user")
    suspend fun getAllUsers(
        @Header("Cookie") authCookie: String
    ): Response<UserListDTO>
}