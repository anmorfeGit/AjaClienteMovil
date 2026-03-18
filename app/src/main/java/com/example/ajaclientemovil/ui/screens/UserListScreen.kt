package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel

/**
 * Pantalla de administración para la visualización del listado de usuarios.
 * * Utiliza un LazyColumn para mostrar la lista de usuarios con su información.
 * * Esta pantalla es exclusiva para usuarios con rol 'ADMIN'.
 */
@Composable
fun UserListScreen(viewModel: HomeViewModel = viewModel()) {

    // Disparamos la carga de usuarios desde el servidor al componer la pantalla
    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    // Contenedor principal sin Scaffold (gestionado globalmente)
    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.isLoading) {
            // Indicador de carga centrado mientras se recibe la respuesta JSON
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            // Listado de usuarios con componentes de Material Design 3
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(viewModel.userList) { user ->
                    ListItem(
                        headlineContent = {
                            Text(user.username, style = MaterialTheme.typography.titleMedium)
                        },
                        supportingContent = {
                            Text(user.email, style = MaterialTheme.typography.bodySmall)
                        },
                        trailingContent = {
                            // Etiqueta de rol con distinción cromática
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = (if (user.role == "ADMIN") Color.Red else Color.Blue).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = user.role,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = if (user.role == "ADMIN") Color.Red else Color.Blue,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },
                        leadingContent = {
                            // Indicador visual de estado de cuenta (Activo/Inactivo)
                            Icon(
                                imageVector = if (user.isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (user.isActive) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}