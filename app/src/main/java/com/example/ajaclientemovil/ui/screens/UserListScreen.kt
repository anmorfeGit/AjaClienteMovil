package com.example.ajaclientemovil.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel

@Composable
fun UserListScreen(viewModel: HomeViewModel= viewModel(), onBack: () -> Unit ) {
    // Cargar usuarios al entrar en la pantalla
    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    Scaffold(topBar = { /* Una barra simple con botón atrás */ }) { padding ->
        if (viewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize())
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(viewModel.userList) { user ->
                    ListItem(
                        headlineContent = { Text(user.username) },
                        supportingContent = { Text(user.email) },
                        //overlineContent = { Text(if(user.isActive) "Activo" else "Inactivo") },
                        trailingContent = {
                            Text(
                                text = user.role,
                                color = if (user.role == "ADMIN") Color.Red else Color.Blue
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = if (user.isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (user.isActive) Color.Green else Color.Gray
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}