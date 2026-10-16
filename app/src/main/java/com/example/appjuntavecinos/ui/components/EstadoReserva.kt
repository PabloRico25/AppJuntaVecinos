package com.example.appjuntavecinos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.appjuntavecinos.ui.theme.*
import androidx.compose.ui.unit.dp

@Composable
fun EstadoReserva(estado: String) {
    val fondo by animateColorAsState(when (estado) {
        "CONFIRMADA", "LIBRE" -> Green
        "PENDIENTE", "SOLICITADO" -> Pendiente
        "RECHAZADA", "OCUPADO" -> Rechazada
        else -> Cancelada
    }, label = "Estado de la reserva")
    val texto = when (estado) {
        "CONFIRMADA", "LIBRE" -> TextoConfirmada
        "PENDIENTE", "SOLICITADO" -> TextoPendiente
        "RECHAZADA", "OCUPADO" -> TextoRechazada
        else -> TextoCancelada
    }
    Surface(color = fondo, contentColor = texto, shape = RoundedCornerShape(8.dp)) {
        Text(estado.lowercase().replaceFirstChar { it.uppercase() }, Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
    }
}
