package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.theme.ErrorRojo
import com.example.appjuntavecinos.utils.fechaParaMostrar
import com.example.appjuntavecinos.viewmodel.MisReservasViewModel

@Composable
fun DetalleReservaScreen(
    reservaId: Int,
    vecinoId: Int,
    onTicket: (Int) -> Unit,
    viewModel: MisReservasViewModel = viewModel()
) {
    val flujoDeReservas = remember(vecinoId) { viewModel.reservas(vecinoId) }
    val reservas by flujoDeReservas.collectAsState(initial = null)
    val dependencias by viewModel.dependencias.collectAsState()
    val pagadas by viewModel.reservasPagadas.collectAsState(initial = null)
    val reserva = reservas?.firstOrNull { it.id == reservaId }
    var confirmandoCancelar by remember { mutableStateOf(false) }

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
            Text("Fecha: ${fechaParaMostrar(reserva.fecha)}")
            Text("Horario: ${reserva.horaInicio} a ${reserva.horaFin}")
            Text("Solicitada el: ${reserva.fechaSolicitud}")

            if (reserva.estado == "PENDIENTE") {
                Text("Secretaría aún no revisa tu solicitud. Cuando la confirme, podrás ver tu ticket QR aquí.")
                Button(
                    onClick = { confirmandoCancelar = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRojo,
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancelar solicitud", fontWeight = FontWeight.Bold)
                }
            }

            if (reserva.estado == "CONFIRMADA") {
                if (reserva.id in pagadas.orEmpty()) {
                    Button(
                        onClick = { onTicket(reserva.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver ticket QR", fontWeight = FontWeight.Bold)
                    }
                } else if (pagadas != null) {
                    Text("Tu reserva está confirmada. El ticket estará disponible cuando Tesorería registre el pago.")
                }
            }
        }
    }

    if (confirmandoCancelar && reserva != null) {
        AlertDialog(
            onDismissRequest = { confirmandoCancelar = false },
            title = { Text("Cancelar reserva") },
            text = { Text("¿Quieres cancelar esta reserva? No se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelar(reserva)
                        confirmandoCancelar = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRojo,
                        contentColor = Color.White
                    )
                ) {
                    Text("Sí, cancelar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmandoCancelar = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("No", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}