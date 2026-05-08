package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ajaclientemovil.ui.viewmodel.WebSocketViewModel

/**
 * Pantalla que muestra el estado del servidor WebSocket.
 * @param viewModel Modelo de vista asociado a esta pantalla.
 */
@Composable
fun StatusServerScreen(viewModel: WebSocketViewModel) {

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.startListening()
    }

    Scaffold(
        topBar = {}
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "Usuarios escribiendo",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            if (viewModel.typingUsers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay actividad", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    // Usamos una clave (key) para que Compose gestione mejor la lista dinámica
                    items(viewModel.typingUsers) { user ->
                        ListItem(
                            headlineContent = { Text(user.username) },
                            supportingContent = { Text("En: ${user.topicTitle ?: "Sin título"}") },
                            leadingContent = { Icon(Icons.Default.HistoryEdu, null) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}