package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla principal de aterrizaje tras un login exitoso.
 * @param onLogout Callback que se ejecuta para limpiar la sesión y navegar al Login.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onLogoutSuccess: () -> Unit,
    onNavigateToUserList: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showProfileMenu by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Cabecera del menú lateral con el azul corporativo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(24.dp)
                ) {
                    Text(
                        "Foros AJA",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("General", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { /* Ir a foro */ },
                    icon = { Icon(Icons.Default.Menu, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Dudas", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { /* Ir a foro */ },
                    icon = { Icon(Icons.Default.Menu, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Aplicamos el Azul Corporativo a la barra superior
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "AJA CLIENTE",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    },
                    navigationIcon = {
                        Box {
                            IconButton(onClick = { showProfileMenu = true }) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Perfil",
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showProfileMenu,
                                onDismissRequest = { showProfileMenu = false }
                            ) {
                                // Estilizamos el encabezado del dropdown
                                Text(
                                    "Hola, ${viewModel.username}",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                HorizontalDivider()

                                DropdownMenuItem(
                                    text = { Text("Mis Datos") },
                                    leadingIcon = { Icon(Icons.Default.Person, null) },
                                    onClick = {
                                        showProfileMenu = false
                                        onNavigateToProfile()
                                    }
                                )

                                if (viewModel.userRole == "ADMIN") {
                                    DropdownMenuItem(
                                        text = { Text("Gestionar Usuarios") },
                                        leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                                        onClick = {
                                            showProfileMenu = false
                                            onNavigateToUserList()
                                        }
                                    )
                                }

                                DropdownMenuItem(
                                    text = { Text("Cerrar Sesión") },
                                    leadingIcon = { Icon(Icons.Default.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        viewModel.onLogoutClicked { onLogoutSuccess() }
                                    }
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú foros")
                        }
                    },
                    // ESTÉTICA: Forzamos los colores de la barra
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            // Contenido principal con un fondo sutil
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Bienvenido al foro principal",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}