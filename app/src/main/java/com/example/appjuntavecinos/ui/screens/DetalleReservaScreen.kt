package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.ui.components.BannerError
import com.example.appjuntavecinos.ui.components.BotonTexto
import com.example.appjuntavecinos.ui.components.EsqueletoDetalle
import com.example.appjuntavecinos.ui.components.EstadoDeError
import com.example.appjuntavecinos.ui.components.EtiquetaEstado
import com.example.appjuntavecinos.ui.components.FichaIcono
import com.example.appjuntavecinos.ui.components.FilaDato
import com.example.appjuntavecinos.ui.components.FranjaAviso
import com.example.appjuntavecinos.ui.components.TarjetaBlanca
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
    val usuarios by viewModel.usuarios.collectAsState()
    val pagadas by viewModel.reservasPagadas.collectAsState(initial = null)
    val cargando by viewModel.cargando.collectAsState()
    val sinActualizar by viewModel.sinActualizar.collectAsState()
    val errorCancelar by viewModel.errorCancelar.collectAsState()
    val reserva = reservas?.firstOrNull { it.id == reservaId }
    var confirmandoCancelar by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Detalle de reserva", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)

        if (reservas == null) {
            EsqueletoDetalle()
        } else if (reserva == null) {
            if (cargando) {
                EsqueletoDetalle()
            } else if (sinActualizar) {
                EstadoDeError(
                    mensaje = "No se pudo conectar con el servidor. Revisa que esté encendido",
                    onReintentar = { viewModel.reintentar() }
                )
            } else {
                Text("Esta reserva no está disponible para tu cuenta.")
            }
        } else {
            if (sinActualizar) {
                FranjaAviso(
                    texto = "No se pudo actualizar. Se muestran las reservas guardadas en este equipo",
                    onReintentar = { viewModel.reintentar() }
                )
            }
            TarjetaBlanca(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        FichaIcono { Icon(Icons.Filled.Home, contentDescription = null) }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = dependencias.firstOrNull { it.id == reserva.dependenciaId }?.nombre ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            EtiquetaEstado(reserva.estado, etiquetaEstado(reserva.estado))
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    FilaDato(R.drawable.ic_nav_calendario, "Fecha", fechaParaMostrar(reserva.fecha))
                    FilaDato(R.drawable.ic_reloj, "Horario", "${reserva.horaInicio} a ${reserva.horaFin}")
                    FilaDato(R.drawable.ic_documento, "Solicitada el", fechaParaMostrar(reserva.fechaSolicitud))
                    if (reserva.gestionadaPorId != null) {
                        Text(
                            "Gestionada por ${usuarios.firstOrNull { it.id == reserva.gestionadaPorId }?.nombre ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (reserva.estado == "PENDIENTE") {
                Text("Secretaría aún no revisa tu solicitud. Cuando la confirme, podrás ver tu ticket QR aquí.")
                Button(
                    onClick = { confirmandoCancelar = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Cancelar solicitud", fontWeight = FontWeight.Bold)
                }
                errorCancelar?.let { BannerError(it) }
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

            if (reserva.estado == "RECHAZADA") {
                Text("La Secretaría rechazó esta solicitud.")
            }

            if (reserva.estado == "CANCELADA") {
                Text("Cancelaste esta solicitud.")
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
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Sí, cancelar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                BotonTexto(
                    onClick = { confirmandoCancelar = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("No", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}