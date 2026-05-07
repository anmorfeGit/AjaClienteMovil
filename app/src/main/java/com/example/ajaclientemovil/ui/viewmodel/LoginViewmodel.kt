package com.example.ajaclientemovil.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ajaclientemovil.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar la lógica de negocio de la pantalla de Login.
 * * Actúa como puente entre la interfaz (UI) y el repositorio de datos (UserRepository).
 * * Utiliza el patrón de arquitectura MVVM.
 * * @param application Referencia al contexto de la aplicación necesaria para el repositorio.
 */
class LoginViewModel(
    application: Application,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {


    var isLoading by mutableStateOf(false)
        private set // Solo el ViewModel puede cambiar este valor

    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * FUNCIÓN VALIDACION: Valida que los campos no estén vacíos antes de intentar el login.
     * @param user Nombre de usuario capturado en el TextField.
     * @param pass Contraseña capturada en el TextField.
     */
    fun validateFields(user: String, pass: String): Boolean {
        return user.trim().isNotEmpty() && pass.trim().isNotEmpty()
    }

    // --- LÓGICA DE NEGOCIO ---

    /**
     * Procesa el intento de inicio de sesión.
     * @param user Nombre de usuario capturado en el TextField.
     * @param pass Contraseña capturada en el TextField.
     * @param onSuccess Callback que se ejecuta cuando el login es correcto para navegar al Home.
     */
    fun onLoginClicked(user: String, pass: String, onSuccess: () -> Unit) {
        // Validación previa (Local)
        if (!validateFields(user, pass)) {
            errorMessage = "Por favor, rellena todos los campos"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // Llamada al repositorio
            val result = userRepository.performLogin(user, pass)

            result.onSuccess {
                onSuccess()
            }
            result.onFailure { exception ->
                // Aquí capturamos el mensaje dinámico que extrajo el NetworkManager
                errorMessage = exception.message
            }
            isLoading = false
        }
    }

    /**
     * Función para resetear el mensaje de error cuando el usuario vuelve a escribir.
     */
    fun resetError() {
        errorMessage = null
    }
}

/**
 * Factory para crear instancias de LoginViewModel.
 * @param application Referencia al contexto de la aplicación necesaria para el repositorio.
 * @param userRepository Repositorio para operaciones relacionadas con usuarios.
 * @return Factory personalizado para LoginViewModel.
 */
class LoginViewModelFactory(
    private val application: Application,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(application, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

