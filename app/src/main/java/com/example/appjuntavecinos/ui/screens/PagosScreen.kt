package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.ui.components.ActualizarAlVolver
import com.example.appjuntavecinos.ui.components.AvisoInformativo
import com.example.appjuntavecinos.ui.components.BotonJunta
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.FichaIcono
import com.example.appjuntavecinos.ui.components.TarjetaBlanca
import com.example.appjuntavecinos.ui.components.aparecerEscalonado
import com.example.appjuntavecinos.ui.theme.JuntaTheme
import com.example.appjuntavecinos.utils.fechaParaMostrar
import com.example.appjuntavecinos.viewmodel.PagosViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PagosScreen(
    puedeRegistrar: Boolean,
    onRegistrarPago: () -> Unit,
    viewModel: PagosViewModel = viewModel()
) {
    ActualizarAlVolver { viewModel.actualizar() }
    val pagos by viewModel.pagos.collectAsState(initial = emptyList())
    val reservasPorCobrar by viewModel.reservasPorCobrar.collectAsState(initial = emptyList())
    val dependencias by viewModel.dependencias.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Pagos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)

        if (puedeRegistrar) {
            BotonJunta(
                onClick = onRegistrarPago,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar pago")
            }
        } else {
            AvisoInformativo("Solo la Tesorería puede registrar pagos.")
        }


        if (reservasPorCobrar.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = JuntaTheme.colores.avisoContenedor,
                    contentColor = JuntaTheme.colores.sobreAviso
                ),
                border = BorderStroke(1.dp, JuntaTheme.colores.sobreAviso.copy(alpha = 0.25f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FichaIcono(color = JuntaTheme.colores.sobreAviso.copy(alpha = 0.15f)) {
                        Icon(painterResource(R.drawable.ic_reloj), contentDescription = null)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Por cobrar", style = MaterialTheme.typography.bodySmall)
                        reservasPorCobrar.forEach { reserva ->
                            val nombreVecino = usuarios.firstOrNull { it.id == reserva.vecinoId }?.nombre ?: ""
                            val nombreSala = dependencias.firstOrNull { it.id == reserva.dependenciaId }?.nombre ?: ""
                            Text(
                                "$nombreVecino, $nombreSala, ${fechaParaMostrar(reserva.fecha)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        if (pagos.isEmpty()) {
            EstadoVacio(R.drawable.ilus_sin_pagos, "Todavía no hay pagos registrados")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(count = pagos.size, key = { indice -> pagos[indice].id }) { indice ->
                    val pago = pagos[indice]
                    TarjetaBlanca(modifier = Modifier.fillMaxWidth().aparecerEscalonado(indice)) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            FichaIcono {
                                if (pago.tipo == "ARRIENDO") Icon(Icons.Filled.Home, contentDescription = null)
                                else Icon(painterResource(R.drawable.ic_documento), contentDescription = null)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(etiquetaPago(pago), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                Text("${formatoPesos(pago.monto)}, ${fechaParaMostrar(pago.fecha)}", style = MaterialTheme.typography.bodyMedium)
                                if (pago.reservaId != null) {
                                    Text("Reserva N° ${pago.reservaId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (pago.descripcion.isNotBlank()) {
                                    Text(pago.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (pago.comprobanteUri != null) {
                                    Text("Comprobante adjunto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
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
        "Consumo básico: ${pago.categoria.lowercase().replaceFirstChar { it.uppercase() }}"
    }
}

fun formatoPesos(monto: Int): String {
    return "$" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CL")).format(monto)
}