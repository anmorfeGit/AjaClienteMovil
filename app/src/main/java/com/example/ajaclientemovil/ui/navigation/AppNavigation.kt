package com.example.ajaclientemovil.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.ui.screens.HomeScreen
import com.example.ajaclientemovil.ui.screens.LoginScreen
import com.example.ajaclientemovil.ui.screens.MyProfileScreen
import com.example.ajaclientemovil.ui.screens.UserListScreen

/**
 * Grafo de navegación principal de la aplicación.
 * @param context Necesario para que SessionManager verifique el estado de la sesión local.
 * * Este componente implementa la lógica de "Auto-Login": si existe un token persistido
 * tras la validación con el servidor, se redirige al usuario directamente al Home.
 */
@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()

    val startDestination = if (SessionManager.isUserLoggedIn(context)) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // --- PANTALLA LOGIN ---
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // --- PANTALLA HOME ---
        composable(Screen.Home.route) {
            HomeScreen(
                onLogoutSuccess = {
                    // Acción para cerrar sesión
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToUserList = {
                    // Acción para ir a la lista de usuarios
                    navController.navigate(Screen.UserList.route)
                },

                onNavigateToProfile = {
                    // Acción para ir a la pantalla de perfil
                    navController.navigate(Screen.MyProfile.route)
                }
            )
        }

        // --- PANTALLA LISTA DE USUARIOS (Solo Admin) ---
        composable(Screen.UserList.route) {
            // Aquí puedes llamar a la nueva pantalla que creamos antes
            UserListScreen(
                onBack = { navController.popBackStack() }
            )
        }
        // --- PANTALLA MIS DATOS ---
        composable(Screen.MyProfile.route) {
            MyProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}