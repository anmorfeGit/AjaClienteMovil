package com.example.ajaclientemovil.ui.navigation

/**
 * Representa las rutas de navegación de la aplicación.
 * El uso de una sealed class centraliza las claves de ruta,
 * facilitando el mantenimiento y evitando errores de hardcoding.
 */
sealed class Screen(val route: String) {
    /**
     * Objeto que representa la pantalla de inicio de sesión.
     * @property route Ruta asociada a esta pantalla.
     */
    object Login : Screen("login_screen")

    /**
     * Objeto que representa la pantalla de inicio.
     * @property route Ruta asociada a esta pantalla.
     */
    object Home : Screen("home_screen")

    /**
     * Objeto que representa la pantalla de mensajes de usuarios.
     * @property route Ruta asociada a esta pantalla.
     */
    object UserList : Screen("user_list")

    /**
     * Objeto que representa la pantalla de administración de usuarios.
     * @property route Ruta asociada a esta pantalla.
     */
    object AdminList : Screen("admin_list")

    /**
     * Objeto que representa la pantalla de perfil del usuario.
     * @property route Ruta asociada a esta pantalla.
     */
    object MyProfile : Screen("my_profile")

    /**
     * Objeto que representa la pantalla de registro de usuarios.
     * @property route Ruta asociada a esta pantalla.
     */
    object Register : Screen("register_screen")
    /**
     * Objeto que representa la pantalla de detalles de un tema.
     * @property route Ruta asociada a esta pantalla.
     */
    object ForumTopics : Screen("forum_topics/{forumId}") {
        fun createRoute(forumId: Long) = "forum_topics/$forumId"
    }

    /**
     * Objeto que representa la pantalla de detalles de un tema.
     * @property route Ruta asociada a esta pantalla.
     */

    object TopicDetail : Screen("topic_detail/{topicId}?title={title}") {
        fun createRoute(topicId: Long, topicTitle: String? = null): String {
            val base = "topic_detail/$topicId"
            return if (!topicTitle.isNullOrBlank()) {
                val encodedTitle = android.net.Uri.encode(topicTitle)
                "$base?title=$encodedTitle"
            } else {
                base
            }
        }
    }

    /**
     * Objeto que representa la pantalla de mensajes directos.
     * @property route Ruta asociada a esta pantalla.
     */
    object DirectMessages : Screen("direct_messages")

    /**
     * Objeto que representa la pantalla de un chat.
     * @property route Ruta asociada a esta pantalla.
     */
    object ChatDetail : Screen("chat_detail/{userId}/{username}") {
        fun createRoute(userId: Long, username: String) = "chat_detail/$userId/$username"
    }

    /**
     * Objeto que representa la pantalla de notificaciones del servidor.
     * @property route Ruta asociada a esta pantalla.
     */
    object StatusServer : Screen("status_server")
}

