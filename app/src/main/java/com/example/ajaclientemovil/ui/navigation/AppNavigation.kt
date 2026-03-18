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
import androidx.navigation.compose.*
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.ui.screens.*
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

/**
 * Componente central de navegación y estructura base de la aplicación.
 * El scaffold y el drawer estan presentes en todas las pantallas salvo en el login.
 * Para evitar tener que repetir el mismo codigo en cada pantalla que se vaya creando,
 * se ha diseñado la estructura base en el componente AppNavigation. *
 * * @param context Contexto de la aplicación necesario para interactuar con las SharedPreferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(context: Context) {
    // --- 1. ESTADOS DE CONTROL DE NAVEGACIÓN ---
    val navController = rememberNavController() // Controla el historial de pantallas
    val scope = rememberCoroutineScope() // Necesario para lanzar animaciones (como abrir el menú)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // Estado del menú lateral

    // Obtenemos el ViewModel compartido para manejar el estado del Logout y datos de usuario
    val homeViewModel: HomeViewModel = viewModel()

    // Observamos la ruta actual del NavHost para saber en qué pantalla estamos en cada momento
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        if (drawerState.isOpen) {
            drawerState.close()
        }
    }
    // --- 2. LÓGICA DE AUTO-LOGIN ---
    // Si el SessionManager confirma que hay un token guardado, empezamos en Home; si no, en Login.
    val startDestination = if (SessionManager.isUserLoggedIn(context)) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    // --- 3. ESTRUCTURA VISUAL (DRAWER + SCAFFOLD) ---
    ModalNavigationDrawer(
        drawerState = drawerState,
        // Solo permitimos arrastrar el menú si el usuario no está en la pantalla de Login
        gesturesEnabled = currentRoute != Screen.Login.route,
        drawerContent = {
            if (currentRoute != Screen.Login.route) {
                // Definimos el contenido del menú lateral
                AppDrawerSheet(onNavigate = { route ->
                    navController.navigate(route)
                    scope.launch { drawerState.close() } // Cerramos el menú tras navegar
                })
            }
        }
    ) {
        Scaffold(
            topBar = {
                // La barra superior es global: se muestra en todas las rutas excepto en Login
                if (currentRoute != Screen.Login.route) {
                    GlobalTopBar(
                        // Cambiamos el texto del título dinámicamente según la ruta activa
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
                            // Ejecuta el cierre de sesión en el ViewModel y navega al Login al terminar
                            homeViewModel.onLogoutClicked {
                                navController.navigate(Screen.Login.route) {
                                    // Limpiamos el historial para que el usuario no pueda volver atrás al Home
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            // --- 4. CONTENEDOR DE PANTALLAS (NAVHOST) ---
            Box(modifier = Modifier.padding(paddingValues)) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(Screen.Login.route) {
                        LoginScreen(onLoginSuccess = {
                            homeViewModel.refreshSessionData()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        })
                    }
                    // Pantallas simplificadas: no necesitan lógica de navegación interna
                    composable(Screen.Home.route) { HomeScreen() }
                    composable(Screen.UserList.route) { UserListScreen() }
                    composable(Screen.MyProfile.route) { MyProfileScreen() }
                }

                // --- 5. COMPONENTES GLOBALES DE ESTADO ---
                // Si el ViewModel está procesando algo (como el Logout), mostramos el bloqueo de carga
                if (homeViewModel.isLoading) {
                    LoadingOverlay("Cerrando sesión...")
                }
            }
        }
    }
}

/**
 * Barra superior unificada de la aplicación.
 * * Contiene el icono de menú (hamburguesa), el título dinámico y un menú desplegable
 * para las opciones de perfil y administración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalTopBar(
    title: String,
    username: String,
    isAdmin: Boolean,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) } // Controla la apertura del Dropdown

    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.ExtraBold) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Abrir menú lateral")
            }
        },
        actions = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Opciones de cuenta")
                }
                // Menú desplegable que aparece bajo el icono de perfil
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    Text("Hola, $username", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Mis Datos") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        onClick = { showMenu = false; onProfileClick() }
                    )
                    // Opción protegida: solo visible si el usuario tiene rol ADMIN
                    if (isAdmin) {
                        DropdownMenuItem(
                            text = { Text("Gestión Usuarios") },
                            leadingIcon = { Icon(Icons.Default.Settings, null) },
                            onClick = { showMenu = false; onAdminClick() }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Cerrar Sesión") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.Red) },
                        onClick = { showMenu = false; onLogoutClick() }
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
 * Interfaz de bloqueo que se muestra durante procesos de carga asíncronos.
 * Bloquea la interacción del usuario para evitar duplicidad de acciones.
 */
@Composable
fun LoadingOverlay(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)), // Oscurece el fondo
        contentAlignment = Alignment.Center
    ) {
        Card {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(message)
            }
        }
    }
}

/**
 * Define el contenido y diseño del Navigation Drawer (menú lateral).
 */
@Composable
fun AppDrawerSheet(onNavigate: (String) -> Unit) {
    ModalDrawerSheet {
        // Cabecera del Drawer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(20.dp)
        ) {
            Text("FOROS AJA", color = Color.White, style = MaterialTheme.typography.titleLarge)
        }
        // Ítems del menú (puedes añadir rutas reales aquí)
        NavigationDrawerItem(
            label = { Text("General") },
            selected = false,
            onClick = { /* Implementar navegación a foro */ }
        )
        NavigationDrawerItem(
            label = { Text("Dudas") },
            selected = false,
            onClick = { /* Implementar navegación a foro */ }
        )
    }
}
