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
import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.ui.components.EstadoReserva
import com.example.appjuntavecinos.utils.fechaParaMostrar
import com.example.appjuntavecinos.viewmodel.ReservasDirectivaViewModel

@Composable
fun ReservasDirectivaScreen(
    usuario: UsuarioEntity, puedeConfirmar: Boolean,
    viewModel: ReservasDirectivaViewModel = viewModel()
) {
    val reservas by viewModel.reservas.collectAsState()
    val dependencias by viewModel.dependencias.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val procesando by viewModel.procesando.collectAsState()
    var seleccion by remember { mutableStateOf<ReservaEntity?>(null) }
    var confirmar by remember { mutableStateOf(true) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()) {
        item { Text("${usuario.nombre} · ${usuario.cargo.lowercase().replaceFirstChar { it.uppercase() }}") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("PENDIENTE" to "Pendientes", "CONFIRMADA" to "Confirmadas", "TODAS" to "Todas").forEach { (valor, texto) ->
                    FilterChip(selected = filtro == valor, onClick = { viewModel.cambiarFiltro(valor) }, label = { Text(texto) })
                }
            }
        }
        if (!puedeConfirmar) item { Text("Solo la Secretaría puede confirmar o rechazar reservas.") }
        if (procesando) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (reservas.isEmpty()) item { Text("No hay reservas con este filtro.") }
        items(reservas, key = { it.id }) { reserva ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(usuarios.find { it.id == reserva.vecinoId }?.nombre.orEmpty(),
                        style = MaterialTheme.typography.titleMedium)
                    Text(dependencias.find { it.id == reserva.dependenciaId }?.nombre.orEmpty())
                    Text("${fechaParaMostrar(reserva.fecha)} · ${reserva.horaInicio} a ${reserva.horaFin}")
                    EstadoReserva(reserva.estado)
                    if (puedeConfirmar && reserva.estado == "PENDIENTE") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { seleccion = reserva; confirmar = true }, enabled = !procesando) { Text("Confirmar") }
                            OutlinedButton(onClick = { seleccion = reserva; confirmar = false }, enabled = !procesando) { Text("Rechazar") }
                        }
                    }
                }
            }
        }
    }
    seleccion?.let { reserva ->
        AlertDialog(
            onDismissRequest = { seleccion = null },
            title = { Text(if (confirmar) "Confirmar reserva" else "Rechazar solicitud") },
            text = { Text("${fechaParaMostrar(reserva.fecha)}, ${reserva.horaInicio} a ${reserva.horaFin}. ¿Continuar?") },
            confirmButton = { TextButton(colors = coloresBotonTexto(), onClick = {
                seleccion = null; viewModel.gestionar(reserva, usuario, confirmar)
            }) { Text("Aceptar") } },
            dismissButton = { TextButton(colors = coloresBotonTexto(), onClick = { seleccion = null }) { Text("Volver") } }
        )
    }
    if (mensaje != null) AlertDialog(
        onDismissRequest = viewModel::limpiarMensaje,
        title = { Text("Resultado") }, text = { Text(mensaje.orEmpty()) },
        confirmButton = { TextButton(colors = coloresBotonTexto(), onClick = viewModel::limpiarMensaje) { Text("Entendido") } }
    )
}
