import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ajaclientemovil.data.DirectMessageChatEntity
import com.example.ajaclientemovil.network.SessionManager
import androidx.compose.ui.platform.LocalContext
import com.example.ajaclientemovil.ui.viewmodel.ChatViewModel

/**
 * Pantalla de mensajes directos.
 * @param viewModel Modelo de vista asociado a esta pantalla.
 * @param isAdmin Indica si el usuario actual es administrador.
 * @param onConversationClick Callback para navegar al chat con el usuario seleccionado (id, username).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageScreen(
    viewModel: ChatViewModel,
    isAdmin: Boolean,
    onConversationClick: (Long, String) -> Unit
) {
    val context = LocalContext.current
    val currentUserId = SessionManager.getUser(context)?.id ?: -1L

    LaunchedEffect(Unit) {
        viewModel.clearError()
        viewModel.fetchAllConversations()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (viewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (viewModel.conversations.isEmpty()) {
            Text(
                "No tienes conversaciones aún",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(viewModel.conversations) { chat ->
                    ConversationItem(
                        chat = chat,
                        currentUserId = currentUserId,
                        isAdmin = isAdmin,
                        onClick = onConversationClick,
                        onDelete = { id -> viewModel.deleteChat(id) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray
                    )
                }
            }
        }

        viewModel.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) { Text(error) }
        }

    }
}

/**
 * Componente de elemento de conversación en la pantalla de mensajes directos.
 * @param chat Chat a mostrar.
 * @param currentUserId Identificador del usuario actual.
 * @param isAdmin Indica si el usuario actual es administrador.
 * @param onClick Callback para navegar al chat con el usuario seleccionado (id, username).
 * @param onDelete Callback para eliminar un chat.
 */
@Composable
fun ConversationItem(
    chat: DirectMessageChatEntity,
    currentUserId: Long,
    isAdmin: Boolean,
    onClick: (Long, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    val otherUser = chat.participants.find { it.id != currentUserId }
    val otherUserName = otherUser?.username ?: "Usuario"
    val otherUserId = otherUser?.id ?: -1L
    val isParticipant = chat.participants.any { it.id == currentUserId }

    val lastMessage = chat.messages.lastOrNull()

    ListItem(
        modifier = Modifier.clickable { onClick(otherUserId, otherUserName) },
        headlineContent = {
            Text(otherUserName, fontWeight = FontWeight.Bold)
        },
        supportingContent = {
            Text(
                text = lastMessage?.message ?: "Chat vacío",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        otherUserName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                lastMessage?.dateTime?.let { dt ->
                    val time = dt.substringAfter("T").take(5)
                    Text(time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                if(isAdmin || isParticipant){
                    IconButton(onClick = { onDelete(otherUserId) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f))
                    }
                }
            }
        }
    )
}