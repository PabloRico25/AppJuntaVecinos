package com.example.appjuntavecinos.ui.screens

import com.example.appjuntavecinos.ui.components.coloresBotonTexto
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.EstadoReserva
import com.example.appjuntavecinos.ui.components.MensajeError
import com.example.appjuntavecinos.utils.fechaParaMostrar
import com.example.appjuntavecinos.viewmodel.MisReservasViewModel

@Composable
fun MisReservasScreen(
    vecinoId: Int, onNuevaReserva: () -> Unit, onDetalle: (Int) -> Unit,
    viewModel: MisReservasViewModel = viewModel()
) {
    val flujo = remember(vecinoId, viewModel) { viewModel.reservas(vecinoId) }
    val reservas by flujo.collectAsState(initial = emptyList())
    val dependencias by viewModel.dependencias.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()) {
        item {
            Button(onClick = onNuevaReserva, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("Nueva reserva")
            }
        }
        item { MensajeError(mensaje) }
        if (reservas.isEmpty()) item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Todavía no tienes reservas", style = MaterialTheme.typography.titleMedium)
                    Text("Puedes consultar la disponibilidad y enviar tu primera solicitud.")
                }
            }
        }
        items(reservas, key = { it.id }) { reserva ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(dependencias.find { it.id == reserva.dependenciaId }?.nombre ?: "Dependencia",
                        style = MaterialTheme.typography.titleMedium)
                    Text("${fechaParaMostrar(reserva.fecha)} · ${reserva.horaInicio} a ${reserva.horaFin}")
                    EstadoReserva(reserva.estado)
                    TextButton(colors = coloresBotonTexto(), onClick = { onDetalle(reserva.id) }) { Text("Ver detalle #${reserva.id}") }
                }
            }
        }
    }
}
