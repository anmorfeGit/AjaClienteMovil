package com.example.ajaclientemovil.data.network

import com.example.ajaclientemovil.data.ForumListDTO
import com.example.ajaclientemovil.data.LoginDTO
import com.example.ajaclientemovil.data.PostEditDTO
import com.example.ajaclientemovil.data.PostListResponse
import com.example.ajaclientemovil.data.PostNewDTO
import com.example.ajaclientemovil.data.TopicEditDTO
import com.example.ajaclientemovil.data.TopicEntityDTO
import com.example.ajaclientemovil.data.TopicListResponse
import com.example.ajaclientemovil.data.TopicNewDTO
import com.example.ajaclientemovil.data.UserEntityDTO
import com.example.ajaclientemovil.data.UserListDTO
import com.example.ajaclientemovil.data.UserRegisterDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Interfaz de Retrofit que define los puntos de acceso (endpoints) de la API de autenticación.
 * Se encarga de la comunicación asíncrona con el backend de Spring Boot.
 */
interface AjaApiService {
// --- USUARIOS ---
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
     */
    @POST("/api/auth/logout")
    suspend fun logout(): Response<Map<String, Any>>

    /**
     * Obtiene la lista completa de usuarios registrados en el sistema.
     * @param authCookie Cadena que contiene el JWT en formato "JWT_TOKEN=valor".
     * @return Objeto UserListDTO que contiene el flag de éxito y la lista de usuarios.
     */
    @GET("api/user")
    suspend fun getAllUsers(
        @Header("Cookie") authCookie: String
    ): Response<UserListDTO>

    /**
     * Registra un nuevo usuario en el sistema.
     * * Este método envía una solicitud POST al endpoint /api/user. A diferencia del Login,
     * los datos se envían en el cuerpo de la petición (JSON) utilizando el [UserRegisterDTO].
     * El servidor se encarga de cifrar la contraseña (BCrypt) y asignar los valores
     * por defecto para el rol (USER) y el estado de activación (TRUE).
     * @param newUser Objeto DTO que contiene las credenciales de registro (username, email, password).
     * @return Una [Response] que contiene un mapa con el resultado de la operación.
     * Normalmente incluye una clave "success" (Boolean) y "message" (String).
     */
    @POST("/api/user")
    suspend fun register(@Body newUser: UserRegisterDTO): Response<Map<String, Any>>

    /**
     * Actualiza la información de un usuario existente.
     * * Utiliza el método HTTP PUT para modificar de forma integral el recurso del usuario.
     * Requiere el ID del usuario en la URL y la validación de sesión mediante el JWT enviado
     * en la cabecera Cookie.
     * @param authCookie Cadena de autenticación en formato "JWT_TOKEN=valor".
     * Es vital para que el servidor valide que el usuario tiene permisos
     * para modificar su propio perfil o es un Administrador.
     * @param id Identificador único del usuario que se desea modificar (obtenido del SessionManager).
     * @param user Objeto [UserEntityDTO] con los datos actualizados (email, username, etc.).
     * @return Una [Response] con el estado de la actualización. El servidor retornará
     * un código 200 OK si la persistencia fue exitosa o 403 Forbidden si el token es inválido.
     */
    @PUT("/api/user") // Quitamos el /{id}
    suspend fun updateUser(
        @Header("Cookie") token: String,
        @Body user: UserEntityDTO
    ): Response<Map<String, Any>>

    /**
     * Elimina un usuario existente.
     * * Utiliza el método HTTP DELETE para eliminar de forma integral el recurso del usuario.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para eliminar al usuario.
     * @param id Identificador único del usuario que se desea eliminar.
     * @return Una [Response] con el estado de la eliminación.
     */
    @DELETE("/api/user/{id}")
    suspend fun deleteUser(
        @Header("Cookie") token: String,
        @Path("id") id: Long
    ): Response<Unit>

    /**
     * Habilita un usuario existente.
     * * Utiliza el método HTTP PUT para modificar de forma integral el recurso del usuario.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para habilitar al usuario.
     * @param id Identificador único del usuario que se desea habilitar.
     * @return Una [Response] con el estado de la operación.
     */
    @PUT("/api/user/enable/{id}")
    suspend fun enableUser(
        @Header("Cookie") token: String,
        @Path("id") id: Long
    ): Response<Map<String, Any>>

    /**
     * Deshabilita un usuario existente.
     * * Utiliza el método HTTP PUT para modificar de forma integral el recurso del usuario.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para deshabilitar al usuario.
     * @param id Identificador único del usuario que se desea deshabilitar.
     * @return Una [Response] con el estado de la operación.
     */
    @PUT("/api/user/disable/{id}")
    suspend fun disableUser(
        @Header("Cookie") token: String,
        @Path("id") id: Long
    ): Response<Map<String, Any>>

    /**
     * Obtiene la lista completa de foros disponibles.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para obtener la lista de foros.
     * @return Una [Response] con el estado de la operación.
     */
    @GET("/api/forum")
    suspend fun getAllForums(
        @Header("Cookie") token: String
    ): Response<ForumListDTO>

// --- FOROS ---
    /**
     * Obtiene un foro por su ID.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para obtener el foro.
     * @param id Identificador único del foro que se desea obtener.
     * @return Una [Response] con el estado de la operación.
     */
    @GET("/api/forum/{id}")
    suspend fun getForumById(
        @Header("Cookie") token: String,
        @Path("id") id: Long
    ): Response<Map<String, Any>>

// --- TEMAS ---
    /**
     * Obtiene la lista completa de temas disponibles.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para obtener la lista de temas.
     * @return Una [Response] con el estado de la operación.
     */
    @GET("/api/topic")
    suspend fun getAllTopics(
        @Header("Cookie") token: String
    ): Response<TopicListResponse>

    /**
     * Obtiene un tema por su ID.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para obtener el tema.
     * @param id Identificador único del tema que se desea obtener.
     * @return Una [Response] con el estado de la operación.
     */
    @GET("/api/topic/{id}")
    suspend fun getTopicById(
        @Header("Cookie") token: String,
        @Path("id") id: Long
    ): Response<TopicEntityDTO>

    /**
     * Crea un nuevo tema.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para crear el tema.
     * @param topicNewDTO Objeto [TopicNewDTO] con los datos del nuevo tema.
     * @return Una [Response] con el estado de la operación.
     */
    @POST("/api/topic")
    suspend fun createTopic(
        @Header("Cookie") token: String,
        @Body topicNewDTO: TopicNewDTO
    ): Response<Map<String, Any>>

    /**
     * Edita un tema existente.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para editar el tema.
     * @param topicEditDTO Objeto [TopicEditDTO] con los datos actualizados del tema.
     * @return Una [Response] con el estado de la operación.
     */
    @PUT("/api/topic")
    suspend fun editTopic(
        @Header("Cookie") token: String,
        @Body topicEditDTO: TopicEditDTO
    ): Response<Map<String, Any>>

    /**
     * Elimina un tema existente.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para eliminar el tema.
     * @param id Identificador único del tema que se desea eliminar.
     * @return Una [Response] con el estado de la operación.
     */
    @DELETE("/api/topic/{id}")
    suspend fun deleteTopic(
        @Header("Cookie") token: String,
        @Path("id") id: Long
    ): Response<Map<String, Any>>

    /**
     * Obtiene la lista completa de posts disponibles.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para obtener la lista de posts.
     * @return Una [Response] con el estado de la operación.
     */
    @GET("/api/post")
    suspend fun getAllPosts(
        @Header("Cookie") token: String
    ): Response<PostListResponse>

    /**
     * Obtiene un post por su ID.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para obtener el post.
     * @param id Identificador único del post que se desea obtener.
     * @return Una [Response] con el estado de la operación.
     */
    @GET("/api/post/{id}")
    suspend fun getPostById(
        @Header("Cookie") token: String,
        @Path("id") id: Long
    ): Response<Map<String, Any>>

    /**
     * Crea un nuevo post.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para crear el post.
     * @param postNewDTO Objeto [PostNewDTO] con los datos del nuevo post.
     * @return Una [Response] con el estado de la operación.
     */
    @POST("/api/post")
    suspend fun createPost(
        @Header("Cookie") token: String,
        @Body postNewDTO: PostNewDTO
    ): Response<Map<String, Any>>

    /**
     * Edita un post existente.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para editar el post.
     * @param postEditDTO Objeto [PostEditDTO] con los datos actualizados del post.
     * @return Una [Response] con el estado de la operación.
     */
    @PUT("/api/post")
    suspend fun editPost(
        @Header("Cookie") token: String,
        @Body postEditDTO: PostEditDTO
    ): Response<Map<String, Any>>

    /**
     * Elimina un post existente.
     * @param token Cadena de autenticación en formato "JWT_TOKEN=valor". Es vital para que el servidor
     * valide que el usuario tiene permisos para eliminar el post.
     * @param id Identificador único del post que se desea eliminar.
     * @return Una [Response] con el estado de la operación.
     */
    @DELETE("/api/post/{id}")
    suspend fun deletePost(
        @Header("Cookie") token: String,
        @Path("id") id: Long
    ): Response<Map<String, Any>>
}


