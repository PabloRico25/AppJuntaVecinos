package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.viewmodel.MisReservasViewModel

@Composable
fun DetalleReservaScreen(
    reservaId: Int,
    vecinoId: Int,
    viewModel: MisReservasViewModel = viewModel()
) {
    val flujoDeReservas = remember(vecinoId) { viewModel.reservas(vecinoId) }
    val reservas by flujoDeReservas.collectAsState(initial = null)
    val dependencias by viewModel.dependencias.collectAsState()
    val reserva = reservas?.firstOrNull { it.id == reservaId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Detalle de reserva", style = MaterialTheme.typography.headlineMedium)

        if (reservas == null) {
            CircularProgressIndicator()
        } else if (reserva == null) {
            Text("Esta reserva no está disponible para tu cuenta.")
        } else {
            Text(
                text = dependencias.firstOrNull { it.id == reserva.dependenciaId }?.nombre ?: "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text("Estado: ${etiquetaEstado(reserva.estado)}")
            Text("Fecha: ${reserva.fecha}")
            Text("Horario: ${reserva.horaInicio} a ${reserva.horaFin}")
            Text("Solicitada el: ${reserva.fechaSolicitud}")
        }
    }
}