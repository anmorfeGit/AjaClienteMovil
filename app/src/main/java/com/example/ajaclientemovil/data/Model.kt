package com.example.ajaclientemovil.data

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

/**
 * DTO con la estructura del usuario para mapeo manual.
 * @param id Identificador único del usuario.
 * @param username Nombre de usuario.
 * @param email Dirección de correo electrónico.
 * @param role Rol del usuario (ADMIN o USER).
 * @param active Indica si el usuario está activo.
 */
data class UserEntityDTO(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    val active: Boolean
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