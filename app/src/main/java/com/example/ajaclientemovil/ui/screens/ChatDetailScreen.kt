package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ajaclientemovil.data.MessageContentDTO
import com.example.ajaclientemovil.network.SessionManager
import com.example.ajaclientemovil.ui.viewmodel.ChatViewModel

/**
 * Pantalla de chat con un usuario específico.
 * @param otherUserId Identificador del usuario con el que se está hablando.
 * @param otherUserName Nombre del usuario con el que se está hablando.
 * @param viewModel Modelo de vista asociado a esta pantalla.
 * @param onBack Callback al hacer clic en el botón de retroceso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    otherUserId: Long,
    otherUserName: String,
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val myId = SessionManager.getUser(context)?.id ?: -1L
    var textState by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    LaunchedEffect(otherUserId) {
        viewModel.clearError()
        viewModel.fetchChatWithUser(otherUserId)
    }

    // Efecto para hacer scroll al último mensaje cuando la lista cambie
    LaunchedEffect(viewModel.activeMessages.size) {
        if (viewModel.activeMessages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.activeMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(otherUserName.take(1).uppercase(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(otherUserName, style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ÁREA DE MENSAJES
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.activeMessages) { msg ->
                    ChatBubble(
                        message = msg,
                        isMine = msg.fromId == myId
                    )
                }
            }

            // BARRA DE ENTRADA DE TEXTO
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje...") },
                        shape = CircleShape,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                viewModel.sendMessage(otherUserId, textState)
                                textState = ""
                            }
                        },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }
}

/**
 * Componente de burbuja de chat.
 * @param message Mensaje a mostrar.
 * @param isMine Indica si el mensaje es del usuario actual.
 */
@Composable
fun ChatBubble(message: MessageContentDTO, isMine: Boolean) {
    val arrangement = if (isMine) Alignment.End else Alignment.Start
    val bubbleColor = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMine) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (isMine) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp) // Punta abajo-derecha
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp) // Punta abajo-izquierda
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = arrangement
    ) {
        Surface(
            color = bubbleColor,
            shape = shape,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.message,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = message.dateTime.substringAfter("T").take(5),
                    color = textColor.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}