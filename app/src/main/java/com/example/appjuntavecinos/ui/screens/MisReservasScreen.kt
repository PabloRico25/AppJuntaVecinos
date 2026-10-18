package com.example.appjuntavecinos.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.ui.theme.Gray
import com.example.appjuntavecinos.ui.theme.Green
import com.example.appjuntavecinos.ui.theme.LightBlue
import com.example.appjuntavecinos.ui.theme.TextoPrincipal
import com.example.appjuntavecinos.utils.armarCodigoCorto
import com.example.appjuntavecinos.utils.armarContenidoQr
import com.example.appjuntavecinos.utils.generarQr
import com.example.appjuntavecinos.viewmodel.MisReservasViewModel

@Composable
fun MisReservasScreen(
    vecinoId: Int,
    nombreVecino: String,
    onNuevaReserva: () -> Unit,
    onDetalle: (Int) -> Unit,
    viewModel: MisReservasViewModel = viewModel()
) {
    val flujoDeReservas = remember(vecinoId) { viewModel.reservas(vecinoId) }
    val reservas by flujoDeReservas.collectAsState(initial = emptyList())
    val dependencias by viewModel.dependencias.collectAsState()
    val pagadas by viewModel.reservasPagadas.collectAsState(initial = emptyList())
    var reservaConQr by remember { mutableStateOf<ReservaEntity?>(null) }

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
                            if (reserva.estado == "CONFIRMADA" && reserva.id in pagadas) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(onClick = { reservaConQr = reserva }) {
                                        Text("Ver QR", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    reservaConQr?.let { reserva ->
        val imagen = remember(reserva.id) {
            generarQr(armarContenidoQr(reserva, nombreVecino),
                900).asImageBitmap()
        }
        AlertDialog(
            onDismissRequest = { reservaConQr = null },
            title = { Text("Tu código QR") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        bitmap = imagen,
                        contentDescription = "Código QR de la reserva",
                        modifier = Modifier.size(300.dp)
                    )
                    Text(
                        text = "Código: ${armarCodigoCorto(reserva)}",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Muéstralo en la entrada el día de tu reserva.")
                }
            },
            confirmButton = {
                TextButton(onClick = { reservaConQr = null }) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        )
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