package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ajaclientemovil.network.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Recuperamos los datos que guardamos en SessionManager al hacer login
    val username = SessionManager.getUsername(context)
    val role = SessionManager.getRole(context) ?: "Usuario"
    val email = SessionManager.getEmail(context) ?: "No disponible"


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Datos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de perfil grande
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de información
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileItem(label = "Nombre de Usuario", value = username)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileItem(label = "Rol del Sistema", value = role)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileItem(label = "Dirección de Email", value = email)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón informativo o de edición futura
            Text(
                text = "Datos vinculados a la sesión actual",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}