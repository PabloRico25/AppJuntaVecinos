package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.*
import com.example.appjuntavecinos.viewmodel.DisponibilidadViewModel

@Composable
fun DisponibilidadScreen(esDirectiva: Boolean, viewModel: DisponibilidadViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    val dependencias by viewModel.dependencias.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()) {
        item {
            MenuDesplegable("Dependencia", estado.dependencia?.nombre.orEmpty(), dependencias.map { it.nombre },
                { viewModel.onDependenciaChange(dependencias[it]) })
        }
        item { CampoFecha(estado.fecha, viewModel::onFechaChange) }
        item { Text("Libre: disponible. Solicitado: pendiente de revisión. Ocupado: reserva confirmada.",
            style = MaterialTheme.typography.bodySmall) }
        item { MensajeError(estado.error) }
        items(estado.franjas, key = { it.inicio }) { franja ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${franja.inicio} – ${franja.fin}")
                        EstadoReserva(franja.estado)
                    }
                    if (esDirectiva) franja.reservas.forEach { reserva ->
                        Text("${usuarios.find { it.id == reserva.vecinoId }?.nombre.orEmpty()} · ${reserva.horaInicio} a ${reserva.horaFin}",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
