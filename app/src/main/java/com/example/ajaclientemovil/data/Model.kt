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
 */
data class UserEntityDTO(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    val active: Boolean
)