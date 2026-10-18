package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.ContenidoFormulario
import com.example.appjuntavecinos.utils.*
import com.example.appjuntavecinos.viewmodel.MisReservasViewModel

@Composable
fun TicketQrScreen(reservaId: Int, vecinoId: Int, nombreVecino: String, viewModel: MisReservasViewModel = viewModel()) {
    val flujo = remember(vecinoId, viewModel) { viewModel.reservas(vecinoId) }
    val reservas by flujo.collectAsState(initial = null)
    val pagadas by viewModel.pagadas.collectAsState(initial = null)
    val dependencias by viewModel.dependencias.collectAsState()
    val reserva = reservas?.find { it.id == reservaId }
    ContenidoFormulario {
        when {
            reservas == null || pagadas == null -> CircularProgressIndicator()
            reserva == null || reserva.estado != "CONFIRMADA" || reserva.id !in pagadas.orEmpty() ->
                Text("El ticket solo está disponible para tus reservas confirmadas y pagadas.")
            else -> {
                val contenido = remember(reserva, nombreVecino) { armarContenidoQr(reserva, nombreVecino) }
                val imagen = remember(contenido) { generarQr(contenido) }
                val datos = remember(contenido) { leerContenidoQr(contenido)!! }
                Text("Ticket de ingreso", style = MaterialTheme.typography.headlineSmall)
                Text(nombreVecino)
                Text(dependencias.find { it.id == reserva.dependenciaId }?.nombre.orEmpty())
                Text("${fechaParaMostrar(reserva.fecha)} · ${reserva.horaInicio} a ${reserva.horaFin}")
                Image(bitmap = imagen.asImageBitmap(), contentDescription = "Código QR de la reserva ${reserva.id}",
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.White).padding(8.dp))
                Text("Ingreso: ${ventanaIngreso(datos)}", style = MaterialTheme.typography.titleMedium)
                Text("Código alternativo (solo en el dispositivo que tiene la reserva):")
                SelectionContainer { Text(armarCodigoCorto(reserva), style = MaterialTheme.typography.headlineSmall) }
                Text("Muestra este QR al ingresar. La validación usa la fecha y la hora del aparato que lo lee.")
            }
        }
    }
}
