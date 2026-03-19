package com.example.ajaclientemovil.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ajaclientemovil.data.ForumEntityDTO
import com.example.ajaclientemovil.data.PostEntityDTO
import com.example.ajaclientemovil.data.TopicEditDTO
import com.example.ajaclientemovil.data.TopicEntityDTO
import com.example.ajaclientemovil.data.UserEntityDTO
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.repository.ForumRepository
import com.example.ajaclientemovil.repository.PostRepository
import com.example.ajaclientemovil.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar la lógica de negocio de la pantalla principal y la administración.
 * * Sigue el patrón arquitectónico MVVM, actuando como puente entre el UserRepository
 * y las pantallas de la interfaz de usuario (Compose).
 * * @param application Referencia al contexto de la aplicación para acceso a recursos y sesión.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)

    // Datos del usuario actual para la UI
    var username by mutableStateOf(SessionManager.getUsername(application))
    var userRole by mutableStateOf(SessionManager.getRole(application))
    var email by mutableStateOf(SessionManager.getEmail(application))
    var registerDate by mutableStateOf(SessionManager.getRegisterDate(application))
    var password by mutableStateOf("") // Campo vacío para edición

    // Estado para la lista de usuarios (solo para ADMIN)
    var userList by mutableStateOf<List<UserEntityDTO>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var userId by mutableStateOf(SessionManager.getUser(application)?.id ?: -1L)
    // En HomeViewModel.kt

    var searchQuery by mutableStateOf("")

    // Esta lista se calcula automáticamente cada vez que cambia 'searchQuery' o 'userList'
    val filteredUserList: List<UserEntityDTO>
        get() {
            return if (searchQuery.isEmpty()) {
                userList
            } else {
                userList.filter {
                    it.username.contains(searchQuery, ignoreCase = true) ||
                            it.email.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    private val forumRepository = ForumRepository(application)
    var forumList by mutableStateOf<List<ForumEntityDTO>>(emptyList())
    private val postRepository = PostRepository(getApplication())
    var postList by mutableStateOf<List<PostEntityDTO>>(emptyList())

// ---USUARIOS---
    /**
     * Solicita al repositorio el listado completo de usuarios registrados.
     * * Ejecuta la petición de forma asíncrona mediante viewModelScope para no bloquear
     * el hilo principal de la interfaz.
     */
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
                // El repositorio se encarga de la red y lo local
                userRepository.performLogout()

                // Si va bien, volvemos a la pantalla de Login
                onLogoutSuccess()
            } catch (e: Exception) {
                // Para manejar errores de red aquí si queremos avisar al usuario
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Procesa la actualización de los datos del perfil del usuario.
     * * Envía los cambios al repositorio y, si tiene éxito, refresca la sesión local.
     * @param onSuccess Callback que se ejecuta tras una actualización exitosa.
     */
    /**
     * Procesa la actualización de los datos del perfil del usuario.
     */
    fun onUpdateProfileClicked(onSuccess: () -> Unit, onUsernameChanged: () -> Unit) {
        if (password.isEmpty()) {
            errorMessage = "Debes introducir tu contraseña para confirmar los cambios"
            return
        }

        val oldUsername = SessionManager.getUsername(getApplication()) // Guardamos el nombre actual
        errorMessage = null

        viewModelScope.launch {
            isLoading = true
            userRepository.updateProfile(username ?: "", email ?: "", password)
                .onSuccess {
                    // Comprobamos si el nombre de usuario ha cambiado
                    if (oldUsername != username) {
                        // Si ha cambiado, cerramos sesión localmente y en el servidor
                        userRepository.performLogout()
                        password = ""
                        onUsernameChanged() // Este callback nos llevará al Login
                    } else {
                        // Si solo cambió el email, refrescamos datos y seguimos en la pantalla
                        refreshSessionData()
                        password = ""
                        onSuccess()
                    }
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Error al actualizar perfil"
                }
            isLoading = false
        }
    }

    /**
     * Actualiza los datos de la interfaz con la sesión actual.
     * Se debe llamar después de cada Login exitoso o actualización de perfil.
     */
    fun refreshSessionData() {
        val app = getApplication<Application>()
        username = SessionManager.getUsername(app)
        userRole = SessionManager.getRole(app)
        email = SessionManager.getEmail(app)
        registerDate = SessionManager.getRegisterDate(app)
        userId = SessionManager.getUser(app)?.id ?: -1L
    }

    /**
     * Elimina un usuario del sistema.
     * * Si el ID coincide con el usuario actual, se limpia la sesión local.
     * @param onSuccess Callback para navegar al Login tras la eliminación.
     */
    fun onDeleteAccountClicked(onSuccess: () -> Unit) {
        val currentUserId = SessionManager.getUser(getApplication())?.id ?: return

        viewModelScope.launch {
            isLoading = true
            userRepository.deleteUser(currentUserId)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { e ->
                    errorMessage = e.message
                }
            isLoading = false
        }
    }

    /**
     * Cambia el estado de un usuario (activo/inactivo).
     * @param user Usuario a modificar.
     */
    fun onToggleUserStatus(user: UserEntityDTO) {
        viewModelScope.launch {
            // Si el usuario está activo, lo deshabilitamos, y viceversa
            userRepository.toggleUserStatus(user.id, !user.isActive)
                .onSuccess { fetchUsers() } // Recargamos la lista
                .onFailure { errorMessage = it.message }
        }
    }
    /**
     * Elimina un usuario del sistema.
     * * Si el ID coincide con el usuario actual, se limpia la sesión local.
     * @param userId Identificador único del usuario a eliminar.
     */
    fun onDeleteUserByAdmin(userId: Long) {
        viewModelScope.launch {
            userRepository.deleteUser(userId)
                .onSuccess { fetchUsers() } // Recargamos la lista
                .onFailure { errorMessage = it.message }
        }
    }

// ---FORUMS---
    /**
     * Obtiene la lista de foros disponibles.
     * Ejecuta la petición de forma asíncrona mediante viewModelScope para no bloquear
     * el hilo principal de la interfaz.
     */
    fun fetchForums() {
        viewModelScope.launch {
            forumRepository.getForums()
                .onSuccess { forumList = it }
                .onFailure { /* Manejar error */ }
        }
    }
    fun fetchTopicsByForum(forumId: Long, onResult: (List<TopicEntityDTO>) -> Unit) {
        viewModelScope.launch {
            forumRepository.getTopicsByForum(forumId)
                .onSuccess { onResult(it) }
                .onFailure { errorMessage = "Error al cargar los temas del foro" }
        }
    }

// ---TOPICS---
    /**
     * Comprueba si el usuario actual puede gestionar un tema.
     * @param topicOwnerId Identificador del propietario del tema.
     * @return True si el usuario puede gestionar el tema, False en caso contrario.
     */
    fun canManageTopic(topicOwnerId: Long): Boolean {
        val currentUserId = SessionManager.getUser(getApplication())?.id
        val isAdmin = SessionManager.getRole(getApplication()) == "ADMIN"
        return isAdmin || currentUserId == topicOwnerId
    }

    /**
     * Crea un nuevo tema en un foro específico.
     * @param title Título del nuevo tema.
     * @param forumId Identificador del foro al que pertenece el tema.
     * @param onSuccess Callback que se ejecuta tras una creación exitosa.
     */
    fun onCreateTopic(title: String, forumId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            forumRepository.createTopic(title, forumId)
                .onSuccess { onSuccess(); fetchForums() } // Refrescamos
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    /**
     * Elimina un tema específico.
     * @param topicId Identificador del tema a eliminar.
     * @param forumId Identificador del foro al que pertenece el tema.
     */
    fun onDeleteTopic(topicId: Long, forumId: Long, function: () -> Unit) {
        viewModelScope.launch {
            forumRepository.deleteTopic(topicId)
                .onSuccess { fetchTopicsByForum(forumId) { /* Actualizar lista UI */ } }
                .onFailure { errorMessage = it.message }
        }
    }

    /**
     * Obtiene la lista de mensajes de un tema específico.
     * @param topicId Identificador del tema al que pertenecen los mensajes.
     * Ejecuta la petición de forma asíncrona mediante viewModelScope para no bloquear
     * el hilo principal de la interfaz.
     */
    fun fetchPostsByTopic(topicId: Long) {
        viewModelScope.launch {
            isLoading = true
            postRepository.getPostsByTopic(topicId)
                .onSuccess { postList = it }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    /**
     * Edita un tema específico.
     * @param id Identificador del tema a editar.
     * @param newTitle Nuevo título del tema.
     * @param forumId Identificador del foro al que pertenece el tema.
     * @param onSuccess Callback que se ejecuta tras una edición exitosa.
     */
    fun onEditTopic(id: Long, newTitle: String, forumId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            // El servidor pide currentForumId y newForumId. Como no lo cambiamos de foro, usamos el mismo.
            val editData = TopicEditDTO(id, newTitle, forumId, forumId)
            forumRepository.editTopic(editData)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }
    /**
     * Crea un nuevo mensaje en un tema específico.
     * @param text Texto del nuevo mensaje.
     * @param topicId Identificador del tema al que pertenece el mensaje.
     * Ejecuta la petición de forma asíncrona mediante viewModelScope para no bloquear
     * el hilo principal de la interfaz.
     */
    fun onSendPost(text: String, topicId: Long) {
        viewModelScope.launch {
            postRepository.createPost(text, topicId)
                .onSuccess { fetchPostsByTopic(topicId) } // Recargar el hilo
                .onFailure { errorMessage = it.message }
        }
    }

    /**
     * Comprueba si el usuario actual puede gestionar un mensaje.
     * @param postUserId Identificador del autor del mensaje.
     * @return True si el usuario puede gestionar el mensaje, False en caso contrario.
     */
    fun canManagePost(postUserId: Long): Boolean {
        val currentUserId = SessionManager.getUser(getApplication())?.id
        val isAdmin = SessionManager.getRole(getApplication()) == "ADMIN"
        return isAdmin || currentUserId == postUserId
    }

}