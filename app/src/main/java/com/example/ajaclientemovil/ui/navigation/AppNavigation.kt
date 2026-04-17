package com.example.ajaclientemovil.ui.navigation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ajaclientemovil.data.ForumEntityDTO
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.ui.screens.*
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel
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
    val homeViewModel: HomeViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isAuthRoute = currentRoute == Screen.Login.route || currentRoute == Screen.Register.route

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showForumDialog by remember { mutableStateOf(false) }
    var forumToEdit by remember { mutableStateOf<ForumEntityDTO?>(null) }
    var forumTitleText by remember { mutableStateOf("") }

    // Dispara la carga de foros si el usuario ya está logueado al abrir la app
    LaunchedEffect(Unit) {
        if (SessionManager.isUserLoggedIn(context)) {
            homeViewModel.fetchForums()
        }
    }

    LaunchedEffect(currentRoute) {
        if (drawerState.isOpen) { drawerState.close() }
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
                    forums = homeViewModel.forumList,
                    isAdmin = (homeViewModel.userRole == "ADMIN"),
                    onForumClick = { forumId ->
                        navController.navigate(Screen.ForumTopics.createRoute(forumId))
                        scope.launch { drawerState.close() }
                    },
                    onNavigate = { route ->
                        navController.navigate(route)
                        scope.launch { drawerState.close() }
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
        }
    ) {
        Scaffold(
            containerColor = if (isAuthRoute) Color.Transparent else MaterialTheme.colorScheme.background,
            topBar = {
                if (!isAuthRoute) {
                    GlobalTopBar(
                        title = when (currentRoute) {
                            Screen.Home.route -> "AJA CLIENTE"
                            Screen.UserList.route -> "GESTIÓN USUARIOS"
                            Screen.MyProfile.route -> "MIS DATOS"
                            else -> "AJA"
                        },
                        username = homeViewModel.username,
                        isAdmin = (homeViewModel.userRole == "ADMIN"),
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onProfileClick = { navController.navigate(Screen.MyProfile.route) },
                        onAdminClick = { navController.navigate(Screen.UserList.route) },
                        onLogoutClick = {
                            homeViewModel.onLogoutClicked {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onDeleteClick = { showDeleteConfirm = true }
                    )
                }
            }
        ) { paddingValues ->

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("¿Eliminar tu cuenta?", fontWeight = FontWeight.Bold) },
                    text = { Text("Esta acción es permanente. Se borrarán todos tus datos y se cerrará la sesión.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteConfirm = false
                                homeViewModel.onDeleteAccountClicked {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
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
                    onDismissRequest = { showForumDialog = false },
                    title = { Text(if (forumToEdit == null) "Nuevo Foro" else "Editar Foro") },
                    text = {
                        OutlinedTextField(
                            value = forumTitleText,
                            onValueChange = { forumTitleText = it },
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
                        TextButton(onClick = { showForumDialog = false }) { Text("CANCELAR") }
                    }
                )
            }

            Box(modifier = if (isAuthRoute) Modifier.fillMaxSize() else Modifier.padding(paddingValues)) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                homeViewModel.refreshSessionData()
                                homeViewModel.fetchForums() // Cargar foros tras login exitoso
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                        )
                    }
                    composable(Screen.Register.route) {
                        RegisterScreen(onBackToLogin = { navController.popBackStack() })
                    }
                    composable(Screen.Home.route) { HomeScreen() }
                    composable(Screen.UserList.route) { UserListScreen() }
                    composable(Screen.MyProfile.route) {
                        MyProfileScreen(
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
                            onTopicClick = { topicId ->
                                navController.navigate(Screen.TopicDetail.createRoute(topicId))
                            }
                        )
                    }
                    composable(
                        route = Screen.TopicDetail.route,
                        arguments = listOf(navArgument("topicId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val topicId = backStackEntry.arguments?.getLong("topicId") ?: -1L
                        TopicDetailScreen(topicId = topicId, viewModel = homeViewModel)
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
    forums: List<ForumEntityDTO>,
    isAdmin: Boolean,
    onForumClick: (Long) -> Unit,
    onNavigate: (String) -> Unit,
    onAddForum: () -> Unit,      // Callback para añadir
    onEditForum: (ForumEntityDTO) -> Unit, // Callback para editar
    onDeleteForum: (Long) -> Unit // Callback para eliminar
) {
    ModalDrawerSheet {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(24.dp)
        ) {
            Text("FOROS AJA", color = Color.White, style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TEMÁTICAS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            // BOTÓN AÑADIR (Solo Admin)
            if (isAdmin) {
                IconButton(onClick = onAddForum, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Foro", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (forums.isEmpty()) {
            Text("Cargando foros...", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
        } else {
            forums.forEach { forum ->
                NavigationDrawerItem(
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(forum.title, modifier = Modifier.weight(1f))
                            if (isAdmin) {
                                IconButton(onClick = { onEditForum(forum) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDeleteForum(forum.id!!) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    },
                    selected = false,
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    onClick = { onForumClick(forum.id!!) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            label = { Text("Inicio") },
            selected = false,
            icon = { Icon(Icons.Default.Home, null) },
            onClick = { onNavigate(Screen.Home.route) },
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
                    if (isAdmin) {
                        DropdownMenuItem(
                            text = { Text("Gestión Usuarios") },
                            leadingIcon = { Icon(Icons.Default.Settings, null) },
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