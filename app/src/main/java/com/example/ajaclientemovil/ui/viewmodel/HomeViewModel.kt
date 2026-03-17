package com.example.ajaclientemovil.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ajaclientemovil.data.UserEntityDTO
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.repository.UserRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)

    // Datos del usuario actual para la UI
    var username by mutableStateOf(SessionManager.getUsername(application))
    var userRole by mutableStateOf(SessionManager.getRole(application))

    // Estado para la lista de usuarios (solo para ADMIN)
    var userList by mutableStateOf<List<UserEntityDTO>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fetchUsers() {
        viewModelScope.launch {
            isLoading = true
            userRepository.getAllUsers()
                .onSuccess { list -> userList = list }
                .onFailure { e -> errorMessage = e.message }
            isLoading = false
        }
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

