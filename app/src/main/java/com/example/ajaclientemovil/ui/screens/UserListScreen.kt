package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ajaclientemovil.data.UserEntityDmDTO
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel

/**
 * Interfaz de usuario para la lista de usuarios.
 * Se comunica con [HomeViewModel] para gestionar los eventos y estados.
 * @param viewModel Modelo de vista asociado a esta pantalla.
 * @param onUserClick Callback para manejar el clic en un usuario.
 */
@Composable
fun UserListScreen(
    viewModel: HomeViewModel,
    onUserClick: (Long, String) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.fetchUsersForDM()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- BUSCADOR POR NOMBRE ---
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = {
                viewModel.searchQuery = it
                viewModel.applyDMFilter()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar usuario...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (viewModel.searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        viewModel.searchQuery = ""
                        viewModel.applyDMFilter()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            shape = MaterialTheme.shapes.large,
            singleLine = true
        )

        // --- LISTA DE USUARIOS ---
        Box(modifier = Modifier.weight(1f)) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.filteredDMList.isEmpty()) {
                Text(
                    text = "No se encontraron resultados",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.filteredDMList) { user ->
                        UserContactItem(
                            user = user,
                            onClick = { onUserClick(user.id, user.username) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Elemento de la lista de usuarios.
 * @param user Usuario a mostrar.
 * @param onClick Callback para manejar el clic en el usuario.
 */
@Composable
fun UserContactItem(
    user: UserEntityDmDTO,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(
                text = user.username,
                style = MaterialTheme.typography.titleMedium
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Enviar mensaje",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    )
}