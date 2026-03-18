package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ajaclientemovil.R


/**
 * Pantalla principal de aterrizaje tras un login exitoso.
 * * Esta pantalla solo contiene el contenido informativo.
 */
@Composable
fun HomeScreen() {
    // Ya no recibimos parámetros de navegación aquí
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
       /* Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo de la Aplicación",
            modifier = Modifier
                .size(250.dp) // 1. Aumentamos el tamaño (prueba con 200.dp o 250.dp)
                .padding(bottom = 32.dp) // 2. Más separación con el formulario
                .graphicsLayer(
                    shadowElevation = 8f, // 3. Opcional: le da un poco de relieve
                    shape = CircleShape,
                    clip = false
                ),
            contentScale = ContentScale.Fit // Asegura que no se deforme
        )*/
        Text(
            text = "Bienvenido al Panel Principal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Selecciona un foro desde el menú lateral para empezar a interactuar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}