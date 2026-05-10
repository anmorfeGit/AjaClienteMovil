package com.example.ajaclientemovil.ui.navigation

import DirectMessageScreen
import android.app.Application
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Dvr
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ajaclientemovil.data.ForumEntityDTO
import com.example.ajaclientemovil.network.NetworkManager
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.repository.DirectMessageRepository
import com.example.ajaclientemovil.ui.screens.AdminListScreen
import com.example.ajaclientemovil.ui.screens.ChatDetailScreen
import com.example.ajaclientemovil.ui.screens.ForumTopicsScreen
import com.example.ajaclientemovil.ui.screens.HomeScreen
import com.example.ajaclientemovil.ui.screens.LoginScreen
import com.example.ajaclientemovil.ui.screens.MyProfileScreen
import com.example.ajaclientemovil.ui.screens.RegisterScreen
import com.example.ajaclientemovil.ui.screens.StatusServerScreen
import com.example.ajaclientemovil.ui.screens.TopicDetailScreen
import com.example.ajaclientemovil.ui.screens.UserListScreen
import com.example.ajaclientemovil.ui.viewmodel.ChatViewModel
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel
import com.example.ajaclientemovil.ui.viewmodel.LoginViewModel
import com.example.ajaclientemovil.ui.viewmodel.LoginViewModelFactory
import com.example.ajaclientemovil.ui.viewmodel.RegisterViewModel
import com.example.ajaclientemovil.ui.viewmodel.RegisterViewModelFactory
import com.example.ajaclientemovil.ui.viewmodel.WebSocketViewModel
import kotlinx.coroutines.launch

/**
 * Estructura base de navegación de la aplicación.
 * Gestiona el Scaffold global, el Drawer lateral con foros dinámicos y el diálogo de cuenta.
 * @param context Contexto de la aplicación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val application = context.applicationContext as Application
    val apiService = NetworkManager.apiService

    val userRepo = remember { com.example.ajaclientemovil.repository.UserRepository(apiService, context) }
    val dmRepo = remember { DirectMessageRepository(apiService, context) }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(application, userRepo)
    )

    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(dmRepo)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isAuthRoute = currentRoute == Screen.Login.route || currentRoute == Screen.Register.route

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showForumDialog by remember { mutableStateOf(false) }
    var forumToEdit by remember { mutableStateOf<ForumEntityDTO?>(null) }
    var forumTitleText by remember { mutableStateOf("") }
    val wsViewModel: WebSocketViewModel = viewModel()


    LaunchedEffect(Unit) {
        if (SessionManager.isUserLoggedIn(context)) {
            homeViewModel.fetchForums()
            wsViewModel.startListening()
        }
    }

    LaunchedEffect(currentRoute) {
        if (drawerState.isOpen) { drawerState.close() }
        if (currentRoute != Screen.ChatDetail.route &&
            currentRoute?.startsWith("chat_detail") == false) {
            chatViewModel.clearChat()
        }
    }

    val startDestination = if (SessionManager.isUserLoggedIn(context)) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isAuthRoute,
        drawerContent = {
            if (!isAuthRoute) {
                AppDrawerSheet(
                    onNavigate = { route ->
                        navController.navigate(route)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ){
        Scaffold(
            containerColor = if (isAuthRoute) Color.Transparent else MaterialTheme.colorScheme.background,
            topBar = {
                if (!isAuthRoute) {
                    GlobalTopBar(
                        title = when {
                            currentRoute == Screen.Home.route -> "AJA CLIENTE"
                            currentRoute == Screen.UserList.route -> "DIRECTORIO"
                            currentRoute == Screen.AdminList.route -> "GESTIÓN DE USUARIOS"
                            currentRoute == Screen.MyProfile.route -> "MIS DATOS"
                            currentRoute == Screen.DirectMessages.route -> "MIS MENSAJES"
                            currentRoute == Screen.StatusServer.route -> "ESTADO DEL SERVIDOR"
                            currentRoute?.startsWith("chat_detail") == true -> "CHAT"
                            else -> "AJA"
                        },
                        username = homeViewModel.username,
                        isAdmin = (homeViewModel.userRole == "ADMIN"),
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onProfileClick = { navController.navigate(Screen.MyProfile.route) },
                        onAdminClick = { navController.navigate(Screen.AdminList.route) },
                        onLogoutClick = {
                            homeViewModel.onLogoutClicked {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onDeleteClick = { showDeleteConfirm = true}
                    )
                }
            }
        ) { paddingValues ->

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false},
                    title = { Text("¿Eliminar tu cuenta?", fontWeight = FontWeight.Bold) },
                    text = { Text("Esta acción es permanente. Se borrarán todos tus datos y se cerrará la sesión.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                homeViewModel.onDeleteAccountClicked {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    showDeleteConfirm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("ELIMINAR") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) { Text("CANCELAR") }
                    }
                )
            }
            if (showForumDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text(if (forumToEdit == null) "Nuevo Foro" else "Editar Foro") },
                    text = {
                        OutlinedTextField(
                            value = forumTitleText,
                            onValueChange = { forumTitleText=it},
                            label = { Text("Título del foro") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (forumTitleText.isNotBlank()) {
                                if (forumToEdit == null) {
                                    homeViewModel.onCreateForum(forumTitleText)
                                } else {
                                    homeViewModel.onEditForum(forumToEdit!!.id!!,forumTitleText)
                                }
                                showForumDialog = false
                                forumTitleText = ""
                            }
                        }) { Text("GUARDAR") }
                    },
                    dismissButton = {
                        TextButton(onClick = { }) { Text("CANCELAR") }
                    }
                )
            }

            Box(modifier = if (isAuthRoute) Modifier.fillMaxSize() else Modifier.padding(paddingValues)) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(Screen.Login.route) {
                        val loginFactory = LoginViewModelFactory(
                            application = context.applicationContext as Application,
                            userRepository = userRepo
                        )

                        val loginViewModel: LoginViewModel = viewModel(factory = loginFactory)

                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                homeViewModel.refreshSessionData()
                                homeViewModel.fetchForums()
                                wsViewModel.startListening()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(Screen.Register.route)
                            }
                        )
                    }
                    composable(Screen.Register.route) {
                        val registerFactory = RegisterViewModelFactory(
                            application = application,
                            userRepository = userRepo
                        )


                        val registerViewModel: RegisterViewModel = viewModel(factory = registerFactory)

                        RegisterScreen(
                            viewModel = registerViewModel,
                            onBackToLogin = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onForumClick = { forumId ->
                                navController.navigate(Screen.ForumTopics.createRoute(forumId))
                            },
                            onAddForum = {
                                forumToEdit = null
                                forumTitleText = ""
                                showForumDialog = true
                            },
                            onEditForum = { forum ->
                                forumToEdit = forum
                                forumTitleText = forum.title
                                showForumDialog = true
                            },
                            onDeleteForum = { id ->
                                homeViewModel.onDeleteForum(id)
                            }
                        )
                    }
                    composable(Screen.UserList.route) {
                        UserListScreen(
                            viewModel = homeViewModel,
                            onUserClick = { userId, username ->
                                navController.navigate(Screen.ChatDetail.createRoute(userId, username))
                            }
                        )
                    }
                    composable(Screen.AdminList.route) {
                        AdminListScreen(
                            viewModel = homeViewModel,
                            onUserClick = { userId, username ->
                                navController.navigate(Screen.ChatDetail.createRoute(userId, username))
                            }
                        )
                    }
                    composable(Screen.MyProfile.route) {
                        MyProfileScreen(
                            viewModel = homeViewModel,
                            onLogout = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(
                        route = Screen.ForumTopics.route,
                        arguments = listOf(navArgument("forumId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val forumId = backStackEntry.arguments?.getLong("forumId") ?: -1L

                        ForumTopicsScreen(
                            forumId = forumId,
                            viewModel = homeViewModel,
                            onTopicClick = { topicId, topicTitle ->
                                navController.navigate(Screen.TopicDetail.createRoute(topicId, topicTitle))
                            }
                        )
                    }

                    composable(
                        route = Screen.TopicDetail.route,
                        arguments = listOf(
                            navArgument("topicId") { type = NavType.LongType },
                            navArgument("title") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val topicId = backStackEntry.arguments?.getLong("topicId") ?: -1L
                        val topicTitle = backStackEntry.arguments?.getString("title")
                        TopicDetailScreen(
                            topicId = topicId,
                            topicTitle = topicTitle,
                            viewModel = homeViewModel,
                            wsViewModel = wsViewModel
                        )
                    }
                    composable(Screen.DirectMessages.route) {
                        DirectMessageScreen(
                            viewModel = chatViewModel,
                            isAdmin = homeViewModel.userRole == "ADMIN",
                            onConversationClick = { userId, username ->
                                navController.navigate(Screen.ChatDetail.createRoute(userId, username))
                            }
                        )
                    }
                    composable(
                        route = Screen.ChatDetail.route,
                        arguments = listOf(
                            navArgument("userId") { type = NavType.LongType },
                            navArgument("username") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getLong("userId") ?: -1L
                        val username = backStackEntry.arguments?.getString("username") ?: "Usuario"

                        ChatDetailScreen(
                            otherUserId = userId,
                            otherUserName = username,
                            viewModel = chatViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.StatusServer.route) {
                        StatusServerScreen(viewModel = wsViewModel)
                    }
                }


                if (homeViewModel.isLoading) {
                    LoadingOverlay("Procesando...")
                }
            }
        }
    }
}

/**
 * Barra lateral que muestra dinámicamente la lista de foros disponibles.
 * @param forums Lista de foros a mostrar.
 * @param onForumClick Callback al hacer clic en un foro.
 * @param onNavigate Callback al navegar a otra pantalla.
 */
@Composable
fun AppDrawerSheet(
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(24.dp)
        ) {
            Text("MENÚ PRINCIPAL", color = Color.White, style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(12.dp))

        NavigationDrawerItem(
            label = { Text("Inicio") },
            selected = false,
            icon = { Icon(Icons.Default.Home, null) },
            onClick = { onNavigate(Screen.Home.route) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = { Text("Estado del Servidor") },
            selected = false,
            icon = { Icon(Icons.AutoMirrored.Filled.Dvr, null) },

            onClick = { onNavigate(Screen.StatusServer.route) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = { Text("Mis Mensajes") },
            selected = false,
            icon = { Icon(Icons.Default.Email, null) },
            onClick = { onNavigate(Screen.DirectMessages.route) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Directorio de Usuarios") },
            selected = false,
            icon = { Icon(Icons.Default.People, null) },
            onClick = { onNavigate(Screen.UserList.route) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

/**
 * Barra superior global con botones de navegación.
 * @param title Título de la pantalla.
 * @param username Nombre de usuario.
 * @param isAdmin Indica si el usuario es administrador.
 * @param onMenuClick Callback al hacer clic en el botón de menú.
 * @param onProfileClick Callback al hacer clic en el botón de perfil
 * @param onAdminClick Callback al hacer clic en el botón de gestión de usuarios.
 * @param onLogoutClick Callback al hacer clic en el botón de cierre de sesión.
 * @param onDeleteClick Callback al hacer clic en el botón de eliminación de cuenta.
 **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalTopBar(
    title: String,
    username: String,
    isAdmin: Boolean,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.ExtraBold) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    Text("Hola, $username", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Mis Datos") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        onClick = { showMenu = false; onProfileClick() }
                    )
                    if(isAdmin){
                        DropdownMenuItem(
                            text = { Text("Gestión de Usuarios") },
                            leadingIcon = { Icon(Icons.Default.People, null) },
                            onClick = { showMenu = false; onAdminClick() }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Cerrar Sesión") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
                        onClick = { showMenu = false; onLogoutClick() }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Eliminar Cuenta", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                        onClick = { showMenu = false; onDeleteClick() }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

/**
 * Muestra un overlay de carga con un mensaje.
 * @param message Mensaje a mostrar.
 */
@Composable
fun LoadingOverlay(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Card {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(message)
            }
        }
    }
}

/**
 * Factory para crear instancias de [ChatViewModel].
 * @param repository Repositorio de DirectMessage.
 * @return Factory personalizado.
 */
/**
 * Factory para crear instancias de [ChatViewModel].
 * @param repository Repositorio de DirectMessage.
 * @return Factory personalizado.
 */
class ChatViewModelFactory(private val repository: DirectMessageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * Factory para crear instancias de [HomeViewModel].
 * @param application Aplicación.
 * @param userRepository Repositorio de usuarios.
 * @return Factory personalizado.
 */
class HomeViewModelFactory(
    private val application: Application,
    private val userRepository: com.example.ajaclientemovil.repository.UserRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}