package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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

/**
 * Pantalla de visualización del perfil del usuario logueado.
 * * Extrae la información de sesión guardada en SharedPreferences.
 */
@Composable
fun MyProfileScreen() {
    val context = LocalContext.current

    // Recuperamos los datos locales guardados tras el login exitoso
    val username = SessionManager.getUsername(context)
    val role = SessionManager.getRole(context) ?: "Usuario"
    val email = SessionManager.getEmail(context) ?: "No disponible"

        Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabecera visual de perfil
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta contenedora de datos personales (Estética Material3)
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ProfileItem(label = "Nombre de Usuario", value = username)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ProfileItem(label = "Rol del Sistema", value = role)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ProfileItem(label = "Dirección de Email", value = email)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

    }
}

/**
 * Componente reutilizable para mostrar un par etiqueta-valor en el perfil.
 */
@Composable
fun ProfileItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}