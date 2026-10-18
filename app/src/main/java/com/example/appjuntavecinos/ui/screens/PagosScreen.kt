package com.example.appjuntavecinos.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.ui.theme.LightBlue
import com.example.appjuntavecinos.ui.theme.TextoPrincipal
import com.example.appjuntavecinos.viewmodel.PagosViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PagosScreen(
    puedeRegistrar: Boolean,
    onRegistrarPago: () -> Unit,
    viewModel: PagosViewModel = viewModel()
) {
    val pagos by viewModel.pagos.collectAsState(initial = emptyList())
    val reservasPorCobrar by viewModel.reservasPorCobrar.collectAsState(initial = emptyList())   // NUEVO
    val dependencias by viewModel.dependencias.collectAsState()                                   // NUEVO
    val usuarios by viewModel.usuarios.collectAsState()                                           // NUEVO

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Pagos", style = MaterialTheme.typography.headlineMedium)

        if (puedeRegistrar) {
            Button(
                onClick = onRegistrarPago,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar pago")
            }
        } else {
            Text("Solo la Tesorería puede registrar pagos.")
        }


        if (reservasPorCobrar.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = LightBlue,
                    contentColor = TextoPrincipal
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Por cobrar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    reservasPorCobrar.forEach { reserva ->
                        val nombreVecino = usuarios.firstOrNull { it.id == reserva.vecinoId }?.nombre ?: ""
                        val nombreSala = dependencias.firstOrNull { it.id == reserva.dependenciaId }?.nombre ?: ""
                        Text("$nombreVecino, $nombreSala, ${reserva.fecha}")
                    }
                }
            }
        }

        if (pagos.isEmpty()) {
            Text("Todavía no hay pagos registrados.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(count = pagos.size, key = { indice -> pagos[indice].id }) { indice ->
                    val pago = pagos[indice]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = etiquetaPago(pago),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("${formatoPesos(pago.monto)}, ${pago.fecha}")
                            if (pago.reservaId != null) {
                                Text("Reserva N° ${pago.reservaId}")
                            }
                            if (pago.descripcion.isNotBlank()) {
                                Text(pago.descripcion)
                            }
                            if (pago.comprobanteUri != null) {
                                Text("Comprobante adjunto")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun etiquetaPago(pago: PagoEntity): String {
    return if (pago.tipo == "ARRIENDO") {
        "Arriendo"
    } else {
        "Consumo básico: ${pago.categoria.lowercase()}"
    }
}

fun formatoPesos(monto: Int): String {
    return "$" + NumberFormat.getIntegerInstance(Locale("es", "CL")).format(monto)
}