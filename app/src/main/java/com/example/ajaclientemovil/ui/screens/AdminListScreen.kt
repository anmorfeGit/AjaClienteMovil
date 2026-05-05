package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel

/**
 * Pantalla de administración y directorio para la visualización del listado de usuarios.
 * @param viewModel Modelo de vista asociado a esta pantalla.
 * @param onUserClick Callback para navegar al chat con el usuario seleccionado (id, username).
 */
@Composable
fun AdminListScreen(
    viewModel: HomeViewModel,
    onUserClick: (Long, String) -> Unit
) {
    val isAdmin = viewModel.userRole == "ADMIN"
    LaunchedEffect(Unit) { viewModel.fetchUsers() }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- BUSCADOR ---
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(if (isAdmin) "Buscar por nombre o email..." else "Buscar usuario...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (viewModel.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            shape = MaterialTheme.shapes.large,
            singleLine = true
        )

        // --- LISTA FILTRADA ---
        Box(modifier = Modifier.weight(1f)) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.filteredUserList.isEmpty()) {
                Text(
                    text = "No se encontraron usuarios",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.filteredUserList) { user ->
                        ListItem(
                            headlineContent = { Text(user.username) },
                            supportingContent = {
                                if (isAdmin) {
                                    Text("${user.email} • ${user.role}")
                                } else {
                                    Text("Rol: ${user.role}")
                                }
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = if (user.isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (user.isActive) Color(0xFF4CAF50) else Color.Red
                                )
                            },
                            trailingContent = {
                                Row {
                                    // --- BOTÓN DE MENSAJE ACTUALIZADO ---
                                    IconButton(onClick = {
                                        // En lugar de abrir un diálogo, navegamos directamente
                                        onUserClick(user.id, user.username)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Enviar mensaje",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (isAdmin) {
                                        // Acción: Activar/Desactivar
                                        if (user.role != "ADMIN") {
                                            IconButton(onClick = { viewModel.onToggleUserStatus(user) }) {
                                                Icon(
                                                    imageVector = if (user.isActive) Icons.Default.Lock
                                                    else Icons.Default.Refresh,
                                                    contentDescription = "Estado",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        // Acción: Cambiar Rol
                                        IconButton(onClick = { viewModel.onChangeUserRole(user) }) {
                                            Icon(
                                                imageVector = if (user.role == "ADMIN") Icons.Default.AdminPanelSettings
                                                else Icons.Default.Person,
                                                contentDescription = "Cambiar Rol",
                                                tint = if (user.role == "ADMIN") Color.Gray else MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Acción: Eliminar
                                        IconButton(onClick = { viewModel.onDeleteUserByAdmin(user.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}
