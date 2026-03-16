package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel

/**
 * Pantalla principal de aterrizaje tras un login exitoso.
 * @param onLogout Callback que se ejecuta para limpiar la sesión y navegar al Login.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogoutSuccess: () -> Unit,
    viewModel: HomeViewModel = viewModel() // Inyectamos el nuevo ViewModel
) {
    val context = LocalContext.current

    // Recuperamos los datos que guardamos en el login
    val username = SessionManager.getUsername(context) ?: "Usuario"
    val role = SessionManager.getRole(context) ?: "Sin Rol"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Inicio") },
                actions = {
                    // Botón de Logout en la barra superior
                    IconButton(onClick = onLogoutSuccess) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono de perfil
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¡Bienvenido, $username!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Rol actual: $role",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón central de Logout (más visible para testear)
            Button(
                onClick = {
                    viewModel.onLogoutClicked() { // Pasamos el contexto si el VM lo necesita
                        onLogoutSuccess() // <--- USAMOS EL NOMBRE DEL PARÁMETRO CORRECTO
                    }
                },
                enabled = !viewModel.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("CERRAR SESIÓN")
                }
            }
        }
    }
}