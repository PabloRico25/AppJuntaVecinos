package com.example.appjuntavecinos.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.theme.Gray
import com.example.appjuntavecinos.ui.theme.Green
import com.example.appjuntavecinos.ui.theme.LightBlue
import com.example.appjuntavecinos.ui.theme.TextoPrincipal
import com.example.appjuntavecinos.viewmodel.MisReservasViewModel

@Composable
fun MisReservasScreen(
    vecinoId: Int,
    onNuevaReserva: () -> Unit,
    onDetalle: (Int) -> Unit,
    viewModel: MisReservasViewModel = viewModel()
) {
    val flujoDeReservas = remember(vecinoId) { viewModel.reservas(vecinoId) }
    val reservas by flujoDeReservas.collectAsState(initial = emptyList())
    val dependencias by viewModel.dependencias.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Mis reservas", style = MaterialTheme.typography.headlineMedium)

        Button(
            onClick = onNuevaReserva,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nueva reserva")
        }

        if (reservas.isEmpty()) {
            Text("Todavía no tienes reservas. Pide la primera con el botón Nueva reserva.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(count = reservas.size, key = { indice -> reservas[indice].id }) { indice ->
                    val reserva = reservas[indice]
                    val colorTarjeta by animateColorAsState(
                        targetValue = colorEstado(reserva.estado),
                        label = "colorTarjeta"
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDetalle(reserva.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = colorTarjeta,
                            contentColor = TextoPrincipal
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = dependencias.firstOrNull { it.id == reserva.dependenciaId }?.nombre ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("${reserva.fecha}, de ${reserva.horaInicio} a ${reserva.horaFin}")
                            Text("Estado: ${etiquetaEstado(reserva.estado)}")
                        }
                    }
                }
            }
        }
    }
}

fun etiquetaEstado(estado: String): String {
    return when (estado) {
        "PENDIENTE" -> "Pendiente"
        "CONFIRMADA" -> "Confirmada"
        "RECHAZADA" -> "Rechazada"
        "CANCELADA" -> "Cancelada"
        else -> estado
    }
}

fun colorEstado(estado: String): Color {
    return when (estado) {
        "PENDIENTE" -> LightBlue
        "CONFIRMADA" -> Green
        else -> Gray
    }
}