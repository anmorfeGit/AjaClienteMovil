package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ajaclientemovil.data.NotifyStatusDTO
import com.example.ajaclientemovil.data.PostEntityDTO
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel
import com.example.ajaclientemovil.ui.viewmodel.WebSocketViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Interfaz de usuario para ver los detalles de un tema específico.
 * Se comunica con [HomeViewModel] para gestionar los eventos y estados.
 * @param topicId Identificador del tema para el que se mostrarán los detalles.
 * @param viewModel Modelo de vista asociado a los detalles del tema.
 * @receiver [HomeViewModel] asociado a esta pantalla.
 */
@Composable
fun TopicDetailScreen(
    topicId: Long,
    topicTitle: String?= null,
    viewModel: HomeViewModel = viewModel(),
    wsViewModel: WebSocketViewModel? = null
) {
    var replyText by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var postToEdit by remember { mutableStateOf<PostEntityDTO?>(null) }
    var editPostText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()



    val fixedTitleForThisSession = remember {
        topicTitle
            ?: viewModel.postList.firstOrNull()?.topic?.title
            ?: "Tema #$topicId"
    }

    var isCurrentlyTyping by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentTopicId = topicId
    val titleSnapshot = remember { fixedTitleForThisSession }

    LaunchedEffect(viewModel.postList.size) {
        if (viewModel.postList.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.postList.size - 1)
        }
    }

    LaunchedEffect(topicId) {
        viewModel.fetchPostsByTopic(topicId)
    }

    DisposableEffect(topicId) {
        wsViewModel?.notifyActivity(topicId, titleSnapshot, true)

        onDispose {
            wsViewModel?.clearActivity(topicId, titleSnapshot)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.postList) { post ->
                // Permisos calculados por post
                val canEdit = viewModel.canEditPost(post.user!!.id)
                val canDelete = viewModel.canDeletePost(post.user!!.id)

                Card(
                    colors = CardDefaults.cardColors(
                        // Color destacado si el usuario tiene algún poder sobre el post
                        containerColor = if (canEdit || canDelete)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.user.username, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("#${post.messageNumber}", style = MaterialTheme.typography.labelSmall)
                        }

                        Text(post.text, modifier = Modifier.padding(vertical = 4.dp))

                        post.creationDate?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.End))
                        }

                        // FILA DE ACCIONES
                        if (canEdit || canDelete) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                if (canEdit) {
                                    IconButton(onClick = {
                                        postToEdit = post
                                        editPostText = post.text
                                        showEditDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                if (canDelete) {
                                    IconButton(onClick = { viewModel.onDeletePost(post.id, topicId) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Barra inferior para responder
        Surface(tonalElevation = 3.dp) {
            Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { nuevoValor ->
                        if (replyText.isEmpty() && nuevoValor.isNotEmpty()) {
                            isCurrentlyTyping = true // Marcamos que está escribiendo
                            wsViewModel?.notifyActivity(topicId, fixedTitleForThisSession, true)
                        } else if (replyText.isNotEmpty() && nuevoValor.isEmpty()) {
                            isCurrentlyTyping = false // Marcamos que dejó de escribir
                            wsViewModel?.notifyActivity(topicId, fixedTitleForThisSession, false)
                        }
                        replyText = nuevoValor
                    },
                    placeholder = { Text("Escribe una respuesta...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3
                )

                IconButton(onClick = {
                    if (replyText.isNotBlank()) {
                        // 4. LIMPIAR ESTADO AL ENVIAR:
                        // Como reseteamos el texto manualmente, debemos notificar el fin.
                        isCurrentlyTyping = false
                        wsViewModel?.notifyActivity(topicId, fixedTitleForThisSession, false)

                        viewModel.onSendPost(replyText, topicId)
                        replyText = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    // --- DIÁLOGO DE EDICIÓN ---
    if (showEditDialog && postToEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar mensaje") },
            text = {
                OutlinedTextField(
                    value = editPostText,
                    onValueChange = { editPostText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Mensaje") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.onEditPost(postToEdit!!.id, editPostText, topicId) {
                        showEditDialog = false
                    }
                }) { Text("GUARDAR") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("CANCELAR") }
            }
        )
    }
}