package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ajaclientemovil.R
import com.example.ajaclientemovil.data.ForumEntityDTO
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List


/**
 * Pantalla principal de aterrizaje tras un login exitoso.
 * * Esta pantalla solo contiene el contenido informativo.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onForumClick: (Long) -> Unit,
    onAddForum: () -> Unit,
    onEditForum: (ForumEntityDTO) -> Unit,
    onDeleteForum: (Long) -> Unit
) {
    val forums = viewModel.forumList
    val isAdmin = viewModel.userRole == "ADMIN"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- SECCIÓN DE ENCABEZADO ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TEMÁTICAS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Explora los foros disponibles",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                if (isAdmin) {
                    Button(
                        onClick = onAddForum,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("NUEVO")
                    }
                }
            }
        }

        // --- LISTA DE FOROS ---
        if (forums.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxHeight(0.7f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No hay foros disponibles", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(forums) { forum ->
                ForumItemCard(
                    forum = forum,
                    isAdmin = isAdmin,
                    onClick = { onForumClick(forum.id!!) },
                    onEdit = { onEditForum(forum) },
                    onDelete = { onDeleteForum(forum.id!!) }
                )
            }
        }
    }
}

/**
 * Componente individual para cada fila de foro.
 * @param forum Foro a mostrar.
 * @param isAdmin Indica si el usuario actual es administrador.
 * @param onClick Callback que se ejecuta al hacer clic en el foro.
 * @param onEdit Callback que se ejecuta al hacer clic en el botón de edición.
 * @param onDelete Callback que se ejecuta al hacer clic en el botón de eliminación.
 */
@Composable
fun ForumItemCard(
    forum: ForumEntityDTO,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = forum.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (isAdmin) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}

