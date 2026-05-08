package com.example.ajaclientemovil.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ajaclientemovil.data.ForumEntityDTO
import com.example.ajaclientemovil.data.PostEntityDTO
import com.example.ajaclientemovil.data.TopicEditDTO
import com.example.ajaclientemovil.data.TopicEntityDTO
import com.example.ajaclientemovil.data.UserEntityDTO
import com.example.ajaclientemovil.data.UserEntityDmDTO
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.repository.ForumRepository
import com.example.ajaclientemovil.repository.PostRepository
import com.example.ajaclientemovil.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla Home.
 * @param application Contexto de la aplicación.
 * @param userRepository Repositorio para operaciones relacionadas con usuarios.
 * @param forumRepository Repositorio para operaciones relacionadas con foros.
 * @param postRepository Repositorio para operaciones relacionadas con posts.
 * @param topicRepository Repositorio para operaciones relacionadas con temas.
 */
class HomeViewModel(
    application: Application,
    private val userRepository: UserRepository,
) : AndroidViewModel(application) {


    var username by mutableStateOf(SessionManager.getUsername(application))
    var userRole by mutableStateOf(SessionManager.getRole(application))
    var email by mutableStateOf(SessionManager.getEmail(application))
    var registerDate by mutableStateOf(SessionManager.getRegisterDate(application))
    var password by mutableStateOf("")
    var userList by mutableStateOf<List<UserEntityDTO>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var userId by mutableStateOf(SessionManager.getUser(application)?.id ?: -1L)

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

    private var dmUserList = mutableStateListOf<UserEntityDmDTO>()

    var filteredDMList = mutableStateListOf<UserEntityDmDTO>()
        private set

    var searchQuery by mutableStateOf("")

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
                .onSuccess {
                    list ->
                    android.util.Log.d("USER_DEBUG", "Usuarios cargados: ${list.size}")
                    userList = list }
                .onFailure { e ->
                    android.util.Log.e("USER_DEBUG", "Error: ${e.message}")
                    errorMessage = e.message }
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
                userRepository.performLogout()
                onLogoutSuccess()
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Procesa la actualización de los datos del perfil del usuario.
     * * Envía los cambios al repositorio y, si tiene éxito, refresca la sesión local.
     * @param onSuccess Callback que se ejecuta tras una actualización exitosa.
     * @param onUsernameChanged Callback que se ejecuta tras un cambio de nombre de usuario.
     * Ejecuta la petición de forma asíncrona mediante viewModelScope para no bloquear
     * el hilo principal de la interfaz.
     */

    fun onUpdateProfileClicked(onSuccess: () -> Unit, onUsernameChanged: () -> Unit) {
        if (password.isEmpty()) {
            errorMessage = "Debes introducir tu contraseña para confirmar los cambios"
            return
        }

        val oldUsername = SessionManager.getUsername(getApplication())
        errorMessage = null

        viewModelScope.launch {
            isLoading = true
            userRepository.updateProfile(username, email, password)
                .onSuccess {
                    if (oldUsername != username) {
                        userRepository.performLogout()
                        password = ""
                        onUsernameChanged()
                    } else {
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
            userRepository.toggleUserStatus(user.id, !user.isActive)
                .onSuccess { fetchUsers() }
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
                .onSuccess { fetchUsers() }
                .onFailure { errorMessage = it.message }
        }
    }

    /**
     * Cambia el role de un usuario (ADMIN - USER).
     *
     * Si el role actual del usuario es "USER" lo promociona a ADMIN,
     * y si ya es "ADMIN" lo degrada a USER.
     * Tras el cambio exitoso recarga la lista de usuarios para reflejar
     * el nuevo estado en la interfaz.
     *
     * @param user Usuario cuyo role se desea cambiar.
     */

    fun onChangeUserRole(user: UserEntityDTO) {
        val toAdmin = user.role != "ADMIN"
        viewModelScope.launch {
            userRepository.updateUserRole(user.id, toAdmin)
                .onSuccess {
                    fetchUsers()
                }
                .onFailure { error ->
                    errorMessage = error.message
                }
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
            android.util.Log.d("FORUM_DEBUG", "fetchForums() llamado")
            forumRepository.getForums()
                .onSuccess { list ->
                    android.util.Log.d("FORUM_DEBUG", "Foros cargados: ${list.size}")
                    forumList = list.sortedBy { it.creationDate ?: "" }
                }
                .onFailure { e ->
                    android.util.Log.e("FORUM_DEBUG", "Error foros: ${e.message}")
                    errorMessage = "Error al cargar foros: ${e.message}"
                }
        }
    }

    /**
     * Crea un nuevo foro.
     * @param title Título del nuevo foro.
     * Ejecuta la petición de forma asíncrona mediante viewModelScope para no bloquear
     * el hilo principal de la interfaz.
     */
    fun onCreateForum(title: String) {
        viewModelScope.launch {
            isLoading = true
            val result = forumRepository.saveForum(ForumEntityDTO(title = title), isEdit = false)

            result.onSuccess {
                fetchForums()
            }.onFailure { e ->
                errorMessage = "Error al crear: ${e.message}"
            }
            isLoading = false
        }
    }

    /**
     * Edita un foro existente.
     * @param id Identificador del foro a editar.
     * @param newTitle Nuevo título del foro.
     * Ejecuta la petición de forma asíncrona mediante viewModelScope para no bloquear
     * el hilo principal de la interfaz.
     */
    fun onEditForum(id: Long, newTitle: String) {
        if (newTitle.isBlank()) {
            errorMessage = "El título no puede estar vacío"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val forumDto = ForumEntityDTO(id = id, title = newTitle)

            val result = forumRepository.saveForum(forumDto, isEdit = true)

            result.onSuccess {
                fetchForums()
            }.onFailure { e ->
                errorMessage = "Error al editar el foro: ${e.message}"
            }

            isLoading = false
        }
    }

    /**
     * Elimina un foro específico.
     * @param id Identificador del foro a eliminar.
     * Ejecuta la petición de forma asíncrona mediante viewModelScope para no bloquear
     * el hilo principal de la interfaz.
     */
    fun onDeleteForum(id: Long) {
        viewModelScope.launch {
            isLoading = true
            val result = forumRepository.deleteForum(id)

            result.onSuccess {
                fetchForums()
            }.onFailure { e ->
                errorMessage = "Error al eliminar: ${e.message}"
            }
            isLoading = false
        }
    }

    // ---TOPICS---

    /**
     * Obtiene la lista de temas de un foro específico.
     * @param forumId Identificador del foro del que se quieren obtener los temas.
     * Ejecuta la petición de forma asíncrona mediante viewModelScope para no bloquear
     * el hilo principal de la interfaz.
     */
    fun fetchTopicsByForum(forumId: Long, onResult: (List<TopicEntityDTO>) -> Unit) {
        viewModelScope.launch {
            forumRepository.getTopicsByForum(forumId)
                .onSuccess { onResult(it) }
                .onFailure { errorMessage = "Error al cargar los temas del foro" }
        }
    }


    /**
     * Comprueba si el usuario actual puede editar un tema.
     * @param topicOwnerId Identificador del propietario del tema.
     * @return True si el usuario puede gestionar el tema, False en caso contrario.
     */
    fun canEditTopic(topicOwnerId: Long): Boolean {
        val currentUserId = SessionManager.getUser(getApplication())?.id
        val isAdmin = SessionManager.getRole(getApplication()) == "ADMIN"
        return isAdmin || currentUserId == topicOwnerId
    }

    /**
     * Comprueba si el usuario actual puede eliminar un tema.
     * @return True si el usuario puede eliminar el tema, False en caso contrario.
     */
    fun canDeleteTopic(): Boolean {
        return SessionManager.getRole(getApplication()) == "ADMIN"
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
                .onSuccess { onSuccess(); fetchForums() }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    /**
     * Elimina un tema específico.
     * @param topicId Identificador del tema a eliminar.
     * @param forumId Identificador del foro al que pertenece el tema.
     */
    fun onDeleteTopic(topicId: Long, forumId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            forumRepository.deleteTopic(topicId)
                .onSuccess { onComplete()}
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
                .onSuccess { list ->
                    postList = list.sortedBy { it.messageNumber } }
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

    // ---POSTS---
    fun onSendPost(text: String, topicId: Long) {
        viewModelScope.launch {
            postRepository.createPost(text, topicId)
                .onSuccess { fetchPostsByTopic(topicId) } // Recargar el hilo
                .onFailure { errorMessage = it.message }
        }
    }


    /**
     * Comprueba si el usuario actual puede editar un mensaje.
     * @param postUserId Identificador del autor del mensaje.
     * @return True si el usuario puede editar el mensaje, False en caso contrario.
     */
    fun canEditPost(postUserId: Long): Boolean {
        val currentUserId = SessionManager.getUser(getApplication())?.id
        return currentUserId == postUserId
    }

    /**
     * Comprueba si el usuario actual puede eliminar un mensaje.
     * @param postUserId Identificador del autor del mensaje.
     * @return True si el usuario puede eliminar el mensaje, False en caso contrario.
     */
    fun canDeletePost(postUserId: Long): Boolean {
        val currentUserId = SessionManager.getUser(getApplication())?.id
        val isAdmin = SessionManager.getRole(getApplication()) == "ADMIN"
        return isAdmin || currentUserId == postUserId
    }

// --- ACCIONES (Llaman al repositorio) ---

    /**
     * Elimina un mensaje específico.
     * @param postId Identificador del mensaje a eliminar.
     * @param topicId Identificador del tema al que pertenece el mensaje.
     */
    fun onDeletePost(postId: Long, topicId: Long) {
        viewModelScope.launch {
            isLoading = true
            postRepository.deletePost(postId)
                .onSuccess {
                    fetchPostsByTopic(topicId)
                }
                .onFailure { errorMessage = "No se pudo eliminar: ${it.message}" }
            isLoading = false
        }
    }

    /**
     * Edita un mensaje existente.
     * @param postId Identificador del mensaje a editar.
     * @param newText Nuevo contenido del mensaje.
     * @param topicId Identificador del tema al que pertenece el mensaje.
     * @param onSuccess Callback que se ejecuta tras una edición exitosa
     */
    fun onEditPost(postId: Long, newText: String, topicId: Long, onSuccess: () -> Unit) {
        if (newText.isBlank()) {
            errorMessage = "El mensaje no puede estar vacío"
            return
        }

        viewModelScope.launch {
            isLoading = true
            val postDto = PostEntityDTO(id = postId, text = newText)

            postRepository.updatePost(postDto)
                .onSuccess {
                    fetchPostsByTopic(topicId)
                    onSuccess()
                }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    fun fetchUsersForDM() {
        viewModelScope.launch {
            isLoading = true
            userRepository.getUsersForDM()
                .onSuccess { users ->
                    dmUserList.clear()
                    dmUserList.addAll(users)
                    applyDMFilter()
                    android.util.Log.d("DM_DEBUG", "Lista cargada: ${dmUserList.size} usuarios")
                }
                .onFailure { e ->
                    errorMessage = e.message
                    android.util.Log.e("DM_DEBUG", "Error: ${e.message}")
                }
            isLoading = false
        }
    }
    fun applyDMFilter() {
        val query = searchQuery.trim().lowercase()
        val filtered = if (query.isEmpty()) {
            dmUserList
        } else {
            dmUserList.filter { it.username.lowercase().contains(query) }
        }
        filteredDMList.clear()
        filteredDMList.addAll(filtered)
    }
}