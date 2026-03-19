package com.example.ajaclientemovil.ui.navigation

/**
 * Representa las rutas de navegación de la aplicación.
 * El uso de una sealed class centraliza las claves de ruta,
 * facilitando el mantenimiento y evitando errores de hardcoding.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
    object UserList : Screen("user_list")
    object MyProfile : Screen("my_profile")
    object Register : Screen("register_screen")
    object ForumTopics : Screen("forum_topics/{forumId}") {
        fun createRoute(forumId: Long) = "forum_topics/$forumId"
    }
    object TopicDetail : Screen("topic_detail/{topicId}") {
        fun createRoute(topicId: Long) = "topic_detail/$topicId"
    }
}

