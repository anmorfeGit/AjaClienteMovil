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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel

@Composable
fun TopicDetailScreen(topicId: Long, viewModel: HomeViewModel = viewModel()) {
    var replyText by remember { mutableStateOf("") }

    LaunchedEffect(topicId) {
        viewModel.fetchPostsByTopic(topicId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Lista de mensajes
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.postList) { post ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.canManagePost(post.user.id))
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.user.username, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("#${post.messageNumber}", style = MaterialTheme.typography.labelSmall)
                        }
                        Text(post.text, modifier = Modifier.padding(vertical = 4.dp))
                        post.creationDate?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.End)) }

                        if (viewModel.canManagePost(post.user.id)) {
                            // Iconos de Editar/Borrar aquí
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
                    onValueChange = { replyText = it },
                    placeholder = { Text("Escribe una respuesta...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3
                )
                IconButton(onClick = {
                    viewModel.onSendPost(replyText, topicId)
                    replyText = ""
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}