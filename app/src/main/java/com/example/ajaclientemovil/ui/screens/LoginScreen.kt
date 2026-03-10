package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ajaclientemovil.ui.viewmodel.LoginViewModel

/**
 * Interfaz de usuario para el inicio de sesión.
 * Se comunica con [LoginViewModel] para gestionar los eventos y estados.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    // Estados locales para los campos de texto
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AJA Cliente Móvil",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de Usuario
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                viewModel.resetError() // Limpia el error mientras escribes
            },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !viewModel.isLoading // Se bloquea si está cargando
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.resetError()
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !viewModel.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Gestión de estados: Error o Carga
        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (viewModel.isLoading) {
            // Mientras el NetworkManager habla con el servidor de Alex
            CircularProgressIndicator()
        } else {
            // Botón de acción principal
            Button(
                onClick = { viewModel.onLoginClicked(username, password, onLoginSuccess) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("INICIAR SESIÓN")
            }
        }
    }
}