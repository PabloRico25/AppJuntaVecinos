package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.utils.armarCodigoCorto
import com.example.appjuntavecinos.utils.armarContenidoQr
import com.example.appjuntavecinos.utils.generarQr
import com.example.appjuntavecinos.utils.ventanaIngreso
import com.example.appjuntavecinos.viewmodel.MisReservasViewModel

@Composable
fun TicketQrScreen(
    reservaId: Int,
    vecinoId: Int,
    nombreVecino: String,
    viewModel: MisReservasViewModel = viewModel()
) {
    val flujoDeReservas = remember(vecinoId) { viewModel.reservas(vecinoId) }
    val reservas by flujoDeReservas.collectAsState(initial = null)
    val pagadas by viewModel.reservasPagadas.collectAsState(initial = null)
    val dependencias by viewModel.dependencias.collectAsState()
    val reserva = reservas?.firstOrNull { it.id == reservaId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // No visto en clase (lo integramos con permiso del docente): verticalScroll deja recorrer la pantalla hacia abajo.
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ticket QR", style = MaterialTheme.typography.headlineMedium)

        if (reservas == null || pagadas == null) {
            CircularProgressIndicator()
        } else if (reserva == null || reserva.estado != "CONFIRMADA" || reserva.id !in pagadas.orEmpty()) {
            Text("El ticket solo está disponible para tus reservas confirmadas y pagadas.")
        } else {
            val imagen = remember(reserva.id) {
                generarQr(armarContenidoQr(reserva, nombreVecino), 900).asImageBitmap()
            }
            Text(
                text = nombreVecino,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(dependencias.firstOrNull { it.id == reserva.dependenciaId }?.nombre ?: "")
            Text("${reserva.fecha}, de ${reserva.horaInicio} a ${reserva.horaFin}")
            Image(
                bitmap = imagen,
                contentDescription = "Código QR de la reserva",
                modifier = Modifier.size(300.dp)
            )
            Text("Ingreso: ${ventanaIngreso(reserva.horaInicio)}", fontWeight = FontWeight.Bold)
            Text("Código: ${armarCodigoCorto(reserva)}", fontWeight = FontWeight.Bold)
            Text("Muéstralo en la entrada el día de tu reserva.")
        }
    }
}