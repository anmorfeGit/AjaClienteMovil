package com.example.ajaclientemovil.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ajaclientemovil.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar la lógica de negocio para el registro de nuevos usuarios.
 * * Esta clase actúa como puente entre la [RegisterScreen] y el [UserRepository],
 * manteniendo el estado de la interfaz (carga, errores y éxito) de forma independiente
 * a la navegación, siguiendo el patrón arquitectónico MVVM.
 *
 * @param application Referencia al contexto de la aplicación para el acceso al repositorio.
 */
class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(application)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun onRegisterClicked(user: String, email: String, pass: String, confirmPass: String, onSuccess: () -> Unit) {
        // Validaciones locales para campos obligatorios
        if (user.isBlank() || email.isBlank() || pass.isBlank()) {
            errorMessage = "Todos los campos son obligatorios"
            return
        }
        if (pass != confirmPass) {
            errorMessage = "Las contraseñas no coinciden"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = userRepository.performRegister(user, email, pass)

            result.onSuccess {
                successMessage = "Cuenta creada con exito!"
                onSuccess()
            }
            result.onFailure {
                errorMessage = it.message ?: "Error desconocido en el registro"
            }
            isLoading = false
        }
    }
}