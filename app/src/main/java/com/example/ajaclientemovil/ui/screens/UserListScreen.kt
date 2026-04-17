package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel

/**
 * Pantalla de administración para la visualización del listado de usuarios.
 * * Utiliza un LazyColumn para mostrar la lista de usuarios con su información.
 * * Esta pantalla es exclusiva para usuarios con rol 'ADMIN'.
 * @param viewModel Modelo de vista asociado a esta pantalla.
 * @receiver [HomeViewModel] asociado a esta pantalla.
 */
@Composable
fun UserListScreen(viewModel: HomeViewModel = viewModel()) {
    LaunchedEffect(Unit) { viewModel.fetchUsers() }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- BUSCADOR ---
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar por nombre o email...") },
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
        Box(modifier = Modifier.weight(1f)) { // El peso 1f hace que la lista ocupe el resto
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.filteredUserList.isEmpty()) {
                // Mensaje si no hay resultados
                Text(
                    text = "No se encontraron usuarios",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // IMPORTANTE: Usamos la lista filtrada del ViewModel
                    items(viewModel.filteredUserList) { user ->
                        ListItem(
                            headlineContent = { Text(user.username) },
                            supportingContent = { Text("${user.email} • ${user.role}") },
                            leadingContent = {
                                Icon(
                                    imageVector = if (user.isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (user.isActive) Color(0xFF4CAF50) else Color.Red
                                )
                            },
                            trailingContent = {
                                if (user.role != "ADMIN") {
                                    Row {
                                        IconButton(onClick = { viewModel.onToggleUserStatus(user) }) {
                                            Icon(
                                                imageVector = if (user.isActive) Icons.Default.Lock else Icons.Default.Refresh,
                                                contentDescription = "Estado",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
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
