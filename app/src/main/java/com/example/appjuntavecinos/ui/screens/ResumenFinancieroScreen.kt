package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.*
import com.example.appjuntavecinos.utils.*
import com.example.appjuntavecinos.viewmodel.ResumenViewModel
import java.text.NumberFormat
import java.util.Locale

fun pesos(monto: Double): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")).apply {
    maximumFractionDigits = 0
}.format(monto)

@Composable
fun ResumenFinancieroScreen(esDirectiva: Boolean, viewModel: ResumenViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    ContenidoFormulario {
        Text("Finanzas de la junta", style = MaterialTheme.typography.headlineSmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            TextButton(colors = coloresBotonTexto(), onClick = { viewModel.cambiarMes(-1) }) { Text("Anterior") }
            Text(mesParaMostrar(estado.mes), modifier = Modifier.weight(1f))
            TextButton(colors = coloresBotonTexto(), onClick = { viewModel.cambiarMes(1) }) { Text("Siguiente") }
        }
        MensajeError(estado.error)
        val totales = listOf("Ingresos" to estado.ingresos, "Gastos" to estado.gastos, "Saldo" to estado.saldo)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 600.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    totales.forEach { (titulo, valor) -> TarjetaTotal(titulo, valor, Modifier.weight(1f)) }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    totales.forEach { (titulo, valor) -> TarjetaTotal(titulo, valor, Modifier.fillMaxWidth()) }
                }
            }
        }
        Text("Gastos por categoría", style = MaterialTheme.typography.titleMedium)
        listOf("ELECTRICIDAD", "AGUA", "GAS").forEach { categoria ->
            val total = estado.gastosPorCategoria[categoria] ?: 0.0
            Text("${categoria.lowercase().replaceFirstChar { it.uppercase() }}: ${pesos(total)}")
        }
        Text("Movimientos del mes", style = MaterialTheme.typography.titleMedium)
        if (estado.movimientos.isEmpty()) Text("No hay movimientos en este período.")
        estado.movimientos.forEach { pago ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (pago.tipo == "ARRIENDO") "Ingreso por arriendo" else "Gasto: ${pago.categoria.lowercase()}")
                    Text("${fechaParaMostrar(pago.fecha)} · ${pesos(pago.monto)}")
                    if (esDirectiva && pago.reservaId != null) Text("Reserva #${pago.reservaId}")
                }
            }
        }
        Text("Consulta de registros locales y ficticios. Solo Tesorería registra nuevos pagos desde la pestaña Pagos.",
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TarjetaTotal(titulo: String, valor: Double, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(
        contentColor = MaterialTheme.colorScheme.onSurface,
        containerColor = if (titulo == "Saldo") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo)
            Text(pesos(valor), style = MaterialTheme.typography.headlineSmall)
        }
    }
}
