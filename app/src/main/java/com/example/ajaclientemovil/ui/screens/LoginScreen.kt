package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ajaclientemovil.ui.viewmodel.LoginViewModel

/**
 * Interfaz de usuario para el inicio de sesión.
 * Se comunica con [LoginViewModel] para gestionar los eventos y estados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.tertiary, Color.White)
    )
    Box(modifier = Modifier.fillMaxSize().background(gradientBackground)) {
        Scaffold(
            topBar = {
                // Usamos la misma barra que en Home para dar continuidad visual
                CenterAlignedTopAppBar(
                    title = { Text("AJA CLIENTE", fontWeight = FontWeight.Bold) }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Un icono grande para reforzar la identidad visual
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bienvenido",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Introduce tus credenciales para continuar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Campo de Usuario con Icono
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        viewModel.resetError()
                    },
                    label = { Text("Usuario") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large, // Bordes redondeados modernos
                    enabled = !viewModel.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de Contraseña con Icono
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        viewModel.resetError()
                    },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    enabled = !viewModel.isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Gestión de estados
                if (viewModel.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = viewModel.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (viewModel.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = { viewModel.onLoginClicked(username, password, onLoginSuccess) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.large, // Mismo redondeado que los inputs
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("ENTRAR", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    }
                }
            }
        }
    }
}