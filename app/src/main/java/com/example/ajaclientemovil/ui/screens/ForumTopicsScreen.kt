package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ajaclientemovil.data.TopicEntityDTO
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel

/**
 * Pantalla que muestra la lista de temas (topics) pertenecientes a un foro específico.
 * Permite a los usuarios crear nuevos temas y a los dueños/admins gestionarlos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumTopicsScreen(
    forumId: Long,
    viewModel: HomeViewModel = viewModel(),
    onTopicClick: (Long, String?) -> Unit
) {
    val topics = remember { mutableStateListOf<TopicEntityDTO>() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTopicTitle by remember { mutableStateOf("") }
    var forumTitle by remember { mutableStateOf("Cargando foro...") }
    var showEditDialog by remember { mutableStateOf(false) }
    var topicToEdit by remember { mutableStateOf<TopicEntityDTO?>(null) }
    var editTitle by remember { mutableStateOf("") }

    fun refreshData() {
        viewModel.fetchTopicsByForum(forumId) { fetchedTopics ->
            topics.clear()
            topics.addAll(fetchedTopics)
            if (fetchedTopics.isNotEmpty()) {
                forumTitle = fetchedTopics.first().forum.title
            }
        }
    }

    LaunchedEffect(forumId) { refreshData() }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(forumTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${topics.size} temas disponibles", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (topics.isEmpty() && !viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay temas en este foro. ¡Sé el primero en crear uno!")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(topics) { topic ->
                        TopicItem(
                            topic = topic,
                            canEdit = viewModel.canEditTopic(topic.userOwner.id),
                            canDelete = viewModel.canDeleteTopic(),
                            onClick = { onTopicClick(topic.id, topic.title) },
                            onEdit = {
                                topicToEdit = topic
                                editTitle = topic.title
                                showEditDialog = true
                            },
                            onDelete = {
                                viewModel.onDeleteTopic(topic.id, forumId) { refreshData() }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Crear Tema")
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Nuevo Tema") },
                text = {
                    Column {
                        Text("Introduce el título para iniciar la conversación:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newTopicTitle,
                            onValueChange = { newTopicTitle = it },
                            label = { Text("Título") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newTopicTitle.isNotBlank()) {
                            viewModel.onCreateTopic(newTopicTitle, forumId) {
                                showCreateDialog = false
                                newTopicTitle = ""
                                refreshData()
                            }
                        }
                    }) { Text("CREAR") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("CANCELAR") }
                }
            )
        }

        if (showEditDialog && topicToEdit != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Editar Tema") },
                text = {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Nuevo título") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.onEditTopic(topicToEdit!!.id, editTitle, forumId) {
                            showEditDialog = false
                            refreshData()
                        }
                    }) { Text("GUARDAR") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) { Text("CANCELAR") }
                }
            )
        }
    }
}

/**
 * Componente individual para cada fila de tema.
 * @param topic Tema a mostrar.
 * @param canManage Indica si el usuario actual puede gestionar el tema.
 * @param onClick Callback que se ejecuta al hacer clic en el tema.
 * @param onEdit Callback que se ejecuta al hacer clic en el botón de edición.
 * @param onDelete Callback que se ejecuta al hacer clic en el botón de eliminación.
 */
@Composable
fun TopicItem(
    topic: TopicEntityDTO,
    canEdit: Boolean,
    canDelete: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(topic.title, fontWeight = FontWeight.Bold) },
        supportingContent = { Text("Por ${topic.userOwner.username}") },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canEdit) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                    }
                }

                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red)
                    }
                }

                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    )
}