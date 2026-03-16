package com.example.ajaclientemovil.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.ui.screens.HomeScreen
import com.example.ajaclientemovil.ui.screens.LoginScreen

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
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            // Pasamos la acción de navegación al HomeScreen
            HomeScreen(
                onLogoutSuccess = {
                    // Importante: Al volver al Login, "limpiamos" el Home del historial
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}