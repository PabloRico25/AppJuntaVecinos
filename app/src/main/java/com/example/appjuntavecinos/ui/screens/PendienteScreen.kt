package com.example.appjuntavecinos.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.appjuntavecinos.ui.components.ContenidoFormulario

@Composable
fun PendienteScreen(titulo: String, detalle: String) {
    ContenidoFormulario {
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
        Text(detalle)
        Text("Esta función todavía no está disponible en esta etapa.", style = MaterialTheme.typography.bodyMedium)
    }
}
