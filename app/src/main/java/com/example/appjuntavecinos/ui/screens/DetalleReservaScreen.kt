package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.*
import com.example.appjuntavecinos.utils.fechaParaMostrar
import com.example.appjuntavecinos.viewmodel.MisReservasViewModel

@Composable
fun DetalleReservaScreen(reservaId: Int, vecinoId: Int, onTicket: (Int) -> Unit, viewModel: MisReservasViewModel = viewModel()) {
    val flujo = remember(vecinoId, viewModel) { viewModel.reservas(vecinoId) }
    val reservas by flujo.collectAsState(initial = null)
    val dependencias by viewModel.dependencias.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val procesando by viewModel.procesando.collectAsState()
    val pagadas by viewModel.pagadas.collectAsState(initial = emptyList())
    var confirmarCancelar by remember { mutableStateOf(false) }
    val reserva = reservas?.find { it.id == reservaId }
    ContenidoFormulario {
        if (reservas == null) {
            CircularProgressIndicator()
        } else if (reserva == null) {
            Text("Esta reserva no está disponible para tu cuenta.")
        } else {
            Text(dependencias.find { it.id == reserva.dependenciaId }?.nombre.orEmpty(),
                style = MaterialTheme.typography.headlineSmall)
            EstadoReserva(reserva.estado)
            Text("Fecha: ${fechaParaMostrar(reserva.fecha)}")
            Text("Horario: ${reserva.horaInicio} a ${reserva.horaFin}")
            Text("Solicitada: ${fechaParaMostrar(reserva.fechaSolicitud)}")
            reserva.gestionadaPorId?.let { id ->
                Text("Gestionada por: ${usuarios.find { it.id == id }?.nombre ?: "Secretaría"}")
            }
            when (reserva.estado) {
                "PENDIENTE" -> {
                    Text("Secretaría aún no revisa tu solicitud.")
                    OutlinedButton(onClick = { confirmarCancelar = true }, enabled = !procesando,
                        modifier = Modifier.fillMaxWidth()) { Text("Cancelar solicitud") }
                }
                "CONFIRMADA" -> {
                    if (reserva.id in pagadas) Button(onClick = { onTicket(reserva.id) },
                        modifier = Modifier.fillMaxWidth()) { Text("Ver ticket QR") }
                    else Text("Tu reserva está confirmada. El ticket estará disponible cuando Tesorería registre el pago.")
                }
                "RECHAZADA" -> Text("Puedes consultar otros horarios y enviar una nueva solicitud.")
                "CANCELADA" -> Text("Cancelaste esta solicitud. Ya no está pendiente de revisión.")
            }
            if (procesando) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (mensaje != null) Text(mensaje.orEmpty())
        }
    }
    if (confirmarCancelar && reserva != null) {
        AlertDialog(
            onDismissRequest = { confirmarCancelar = false },
            title = { Text("¿Cancelar esta solicitud?") },
            text = { Text("La solicitud quedará cancelada. Puedes crear otra después.") },
            confirmButton = { TextButton(colors = coloresBotonTexto(), onClick = {
                confirmarCancelar = false; viewModel.cancelar(reserva, vecinoId)
            }) { Text("Sí, cancelar") } },
            dismissButton = { TextButton(colors = coloresBotonTexto(), onClick = { confirmarCancelar = false }) { Text("Volver") } }
        )
    }
}
