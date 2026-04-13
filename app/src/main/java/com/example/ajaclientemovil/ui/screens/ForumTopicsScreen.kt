package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
    onTopicClick: (Long) -> Unit
) {
    // Estado local para la lista de temas y diálogos
    val topics = remember { mutableStateListOf<TopicEntityDTO>() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTopicTitle by remember { mutableStateOf("") }
    var forumTitle by remember { mutableStateOf("Cargando foro...") }
    var showEditDialog by remember { mutableStateOf(false) }
    var topicToEdit by remember { mutableStateOf<TopicEntityDTO?>(null) }
    var editTitle by remember { mutableStateOf("") }

    /**
     * Actualiza la lista de temas y el título del foro.
     * @param fetchedTopics Lista de temas actualizados.
     * @param title Título del foro.
     */
    fun refreshData() {
        viewModel.fetchTopicsByForum(forumId) { fetchedTopics ->
            topics.clear()
            topics.addAll(fetchedTopics)
            if (fetchedTopics.isNotEmpty()) {
                forumTitle = fetchedTopics.first().forum.title
            }
        }
    }

    // Carga inicial al entrar o cambiar de foro
    LaunchedEffect(forumId) {
        refreshData()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Tema")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Cabecera de la pantalla
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = forumTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${topics.size} temas disponibles",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (topics.isEmpty() && !viewModel.isLoading) {
                // Estado vacío
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay temas en este foro. ¡Sé el primero en crear uno!")
                }
            } else {
                // Listado de temas
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(topics) { topic ->
                        TopicItem(
                            topic = topic,
                            canManage = viewModel.canManageTopic(topic.userOwner.id),
                            onClick = { onTopicClick(topic.id) },
                            onEdit = {
                                // Al pulsar editar, cargamos los datos en el estado
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

        // --- Diálogo para crear un nuevo tema ---
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
                    Button(
                        onClick = {
                            if (newTopicTitle.isNotBlank()) {
                                viewModel.onCreateTopic(newTopicTitle, forumId) {
                                    showCreateDialog = false
                                    newTopicTitle = ""
                                    refreshData()
                                }
                            }
                        }
                    ) { Text("CREAR") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("CANCELAR") }
                }
            )
        }
        // --- Diálogo para editar un tema ---
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
                            refreshData() // Refrescamos la lista
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
    canManage: Boolean,
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
                if (canManage) {
                    // BOTÓN EDITAR (Añadido)
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                    }
                    // BOTÓN BORRAR
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red)
                    }
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
    )
}