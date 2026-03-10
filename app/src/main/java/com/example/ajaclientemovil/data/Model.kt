package com.example.ajaclientemovil.data
/**
 * Objeto de transferencia de datos (DTO) que representa la respuesta global del servidor
 * tras un intento de autenticación.
 * * @property success Indica si las identificacion fue exitosa.
 * * @property user Contiene la información detallada del usuario si el login es correcto.
 */
data class LoginDTO(
    val success: Boolean,
    val message: UserEntityDTO
)

/**
 * Representa la entidad de usuario dentro del sistema AJA.
 * * @property id Indica la id de la BBDD.
 * * @property username Nombre de usuario
 * * @property role Define los permisos del usuario (ej: "ADMIN", "USER").
 * * @property email Email del usuario
 * * @property isActive Indica si el usuario esta activo o no, pensado para desabilitar usuarios
 * en la parte del servidor.
 */
data class UserEntityDTO(
    val id: Long,
    val username: String,
    val email: String,
    val role: String, // "ADMIN" o "USER"
    val isActive: Boolean
)