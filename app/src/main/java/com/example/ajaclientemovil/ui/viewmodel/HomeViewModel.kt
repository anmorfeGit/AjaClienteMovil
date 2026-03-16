package com.example.ajaclientemovil.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ajaclientemovil.repository.UserRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)

    // Estado para controlar el indicador de carga durante el logout
    var isLoading by mutableStateOf(false)
        private set

    // Estado para el nombre de usuario (puedes mostrarlo en la Home)
    var username by mutableStateOf("")
        private set

    init {
        // Al iniciar, podríamos cargar datos del usuario si fuera necesario
        //username = userRepository.getCurrentUsername() ?: "Usuario"
    }

    /**
     * Ejecuta el cierre de sesión:
     * 1. Llama al endpoint /api/auth/logout del servidor.
     * 2. Borra los datos locales (SessionManager).
     * 3. Ejecuta el callback para navegar al Login.
     */
    fun onLogoutClicked(onLogoutSuccess: () -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                // El repositorio ahora se encarga de la red y lo local
                userRepository.performLogout()

                // Si va bien, volvemos a la pantalla de Login
                onLogoutSuccess()
            } catch (e: Exception) {
                // Podrías manejar errores de red aquí si quisieras avisar al usuario
            } finally {
                isLoading = false
            }
        }
    }
}

