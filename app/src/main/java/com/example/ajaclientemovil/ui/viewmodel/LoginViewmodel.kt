package com.example.ajaclientemovil.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ajaclientemovil.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar la lógica de negocio de la pantalla de Login.
 * * Actúa como puente entre la interfaz (UI) y el repositorio de datos (UserRepository).
 * * Utiliza [mutableStateOf] para que la interfaz de Compose reaccione automáticamente
 * a los cambios de estado (carga, errores, éxito).
 * * @param application Referencia al contexto de la aplicación necesaria para el repositorio.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Instancia del repositorio para gestionar los datos
    private val userRepository = UserRepository(application)

    // --- ESTADOS DE LA UI ---

    /** Indica si hay una petición al servidor de Alex en curso. Útil para mostrar un ProgressIndicator. */
    var isLoading by mutableStateOf(false)
        private set // Solo el ViewModel puede cambiar este valor

    /** Almacena el mensaje de error si el login falla. Si es null, no hay error. */
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