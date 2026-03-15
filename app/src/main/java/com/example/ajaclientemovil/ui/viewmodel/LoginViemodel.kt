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

    // --- LÓGICA DE NEGOCIO ---

    /**
     * Procesa el intento de inicio de sesión.
     * * @param user Nombre de usuario capturado en el TextField.
     * @param pass Contraseña capturada en el TextField.
     * @param onSuccess Callback que se ejecuta cuando el login es correcto para navegar al Home.
     */
    fun onLoginClicked(user: String, pass: String, onSuccess: () -> Unit) {
        // 1. Validaciones básicas antes de llamar a red
        if (user.isBlank() || pass.isBlank()) {
            errorMessage = "Por favor, rellena todos los campos"
            return
        }

        // 2. Iniciamos el estado de carga y limpiamos errores previos
        isLoading = true
        errorMessage = null

        // 3. Lanzamos una corrutina en el ámbito del ViewModel
        // Esto permite que la petición a Internet no congele la pantalla.
        viewModelScope.launch {
            try {
                // Llamamos al repositorio (nuestro almacén de datos)
                val response = userRepository.performLogin(user, pass)

                if (response != null && response.success) {
                    // Si ha ido bien, ejecutamos la función callback
                    onSuccess()
                } else {
                    // Si el servidor dice que no son válidos
                    errorMessage = "Credenciales incorrectas. Inténtalo de nuevo."
                }
            } catch (e: Exception) {
                // Si hay un error de red (sin internet, servidor caído...)
                errorMessage = "No se pudo conectar con el servidor."
            } finally {
                // Pase lo que pase, al final dejamos de cargar
                isLoading = false
            }
        }
    }

    /**
     * Función para resetear el mensaje de error cuando el usuario vuelve a escribir.
     */
    fun resetError() {
        errorMessage = null
    }
}