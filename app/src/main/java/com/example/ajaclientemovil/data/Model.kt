package com.example.ajaclientemovil.data

import java.time.LocalDate

/**
 * DTO específico para el proceso de alta de nuevos usuarios.
 *
 * A diferencia de [UserEntityDTO], este objeto incluye el campo de contraseña,
 * necesario únicamente durante la creación de la cuenta y nunca retornado por el servidor
 * por motivos de seguridad.
 *
 * @property username Nombre de identificación único que el usuario utilizará para el login.
 * @property email Dirección de correo electrónico para notificaciones y recuperación de cuenta.
 * @property password Contraseña en texto plano que será enviada de forma segura mediante HTTPS
 * y posteriormente cifrada en el backend mediante BCrypt.
 */
data class UserRegisterDTO(
    val username: String,
    val email: String,
    val password: String
)
/**
 * DTO para la respuesta del Login.
 * @param success Indica si la operación fue exitosa.
 * @param message Campo dinámico:
 * - Si success es true: Contiene un objeto (UserEntityDTO).
 * - Si success es false: Contiene un String con el error.
 */
data class LoginDTO(
    val success: Boolean,
    val message: Any?
)
// --- USUARIOS ---
/**
 * DTO con la estructura del usuario para mapeo manual.
 * @param id Identificador único del usuario.
 * @param username Nombre de usuario.
 * @param email Dirección de correo electrónico.
 * @param role Rol del usuario (ADMIN o USER).
 * @param isActive Indica si el usuario está activo.
 */
data class UserEntityDTO(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    val isActive: Boolean,
    val registerDate: String? = null,
    val password: String? = null
)

/**
 * DTO para actualizar los datos del usuario.
 * @param email Nuevo email.
 * @param password Nueva contraseña.
 */
data class UserUpdateDTO(
    val email: String,
    val password: String
)

/**
 * DTO para cuando el servidor responde específicamente con una lista de usuarios.
 * @param success Indica si la operación fue exitosa.
 * @param message Lista de usuarios.
 */
data class UserListDTO(
    val success: Boolean,
    val message: List<UserEntityDTO>
)

// --- FOROS ---
/**
 * DTO para el registro de un nuevo foro.
 * @param id Identificador único del foro.
 * @param title Título del foro.
 * @param creationDate Fecha de creación del foro.
 * @param lastModification Fecha de última modificación del foro.
 */
data class ForumEntityDTO(
    val id: Long? = null,
    val title: String,
    val creationDate: String? = null,
    val lastModification: String? = null
)

/**
 * DTO para el listado de foros.
 * @param success Indica si la operación fue exitosa.
 * @param message Lista de foros.
 */
data class ForumListDTO(
    val success: Boolean,
    val message: List<ForumEntityDTO>
)

// --- TEMAS ---
/**
 * DTO para el registro de un nuevo tema.
 * @param id Identificador único del tema.
 * @param title Título del tema.
 * @param creationDate Fecha de creación del tema.
 * @param lastModification Fecha de última modificación del tema.
 * @param userOwner Usuario que creó el tema.
 * @param forum Foro al que pertenece el tema.
 */
data class TopicEntityDTO(
    val id: Long,
    val title: String,
    val creationDate: String?,
    val lastModification: String?,
    val userOwner: UserEntityDTO,
    val forum: ForumEntityDTO
)

/**
 * DTO para el listado de temas.
 * @param success Indica si la operación fue exitosa.
 * @param message Lista de temas.
 */
data class TopicListResponse(
    val success: Boolean,
    val message: List<TopicEntityDTO>
)

/**
 * DTO para el registro de un nuevo tema.
 * @param title Título del tema.
 * @param forumId Identificador único del foro al que pertenece el tema.
 */
data class TopicNewDTO(
    val title: String,
    val forumId: Long
)

/**
 * DTO para la edición de un tema.
 * @param id Identificador único del tema.
 * @param title Nuevo título del tema.
 * @param forumId Nuevo identificador del foro al que pertenece el tema.
 */
data class TopicEditDTO(
    val id: Long,
    val title: String,
    val currentForumId: Long,
    val newForumId: Long
)

// --- POSTS ---
/**
 * DTO para el registro de un nuevo post.
 * @param text Contenido del post.
 * @param topicId Identificador único del tema al que pertenece el post.
 */
data class PostNewDTO(
    val text: String,
    val topicId: Long
)

/**
 * DTO para la edición de un post.
 * @param id Identificador único del post.
 * @param text Nuevo contenido del post.
 */
data class PostEditDTO(
    val id: Long,
    val text: String
)

/**
 * DTO con la estructura del post para mapeo manual.
 * @param id Identificador único del post.
 * @param messageNumber Número de mensaje en el tema.
 * @param user Usuario que creó el post.
 * @param text Contenido del post.
 * @param creationDate Fecha de creación del post.
 * @param lastModification Fecha de última modificación del post.
 * @param topic Tema al que pertenece el post.
 */
data class PostEntityDTO(
    val id: Long,
    val messageNumber: Long,
    val user: UserEntityDTO,
    val text: String,
    val creationDate: String?,
    val lastModification: String?,
    val topic: TopicEntityDTO
)

/**
 * DTO para el listado de posts.
 * @param success Indica si la operación fue exitosa.
 * @param message Lista de posts.
 */
data class PostListResponse(
    val success: Boolean,
    val message: List<PostEntityDTO>
)

/**
 * DTO para la respuesta de un mensaje genérico.
 * @param message Mensaje de respuesta.
 * @param success Indica si la operación fue exitosa.
 */
data class GenericResponse(
    val message: String,
    val success: Boolean
)