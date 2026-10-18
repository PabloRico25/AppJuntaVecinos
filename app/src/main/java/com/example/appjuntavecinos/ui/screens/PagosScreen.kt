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
import coil.compose.AsyncImage
import com.example.appjuntavecinos.ui.components.MensajeError
import com.example.appjuntavecinos.utils.fechaParaMostrar
import com.example.appjuntavecinos.viewmodel.PagosViewModel

@Composable
fun PagosScreen(puedeRegistrar: Boolean, onRegistrarPago: () -> Unit, viewModel: PagosViewModel = viewModel()) {
    val pagos by viewModel.pagos.collectAsState()
    val porCobrar by viewModel.porCobrar.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    val error by viewModel.error.collectAsState()
    var comprobante by remember { mutableStateOf<String?>(null) }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            if (puedeRegistrar) Button(onClick = onRegistrarPago, modifier = Modifier.fillMaxWidth()) { Text("Registrar pago") }
            else Text("Solo la Tesorería puede registrar pagos.")
        }
        item { MensajeError(error) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Por cobrar: ${porCobrar.size}", style = MaterialTheme.typography.titleMedium)
                    if (porCobrar.isEmpty()) Text("Todas las reservas confirmadas tienen pago registrado.")
                    porCobrar.forEach {
                        Text("#${it.id} · ${usuarios.find { usuario -> usuario.id == it.vecinoId }?.nombre.orEmpty()} · ${fechaParaMostrar(it.fecha)}")
                    }
                }
            }
        }
        if (pagos.isEmpty()) item { Text("Todavía no hay pagos registrados.") }
        items(pagos, key = { it.id }) { pago ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (pago.tipo == "ARRIENDO") "Arriendo · Reserva #${pago.reservaId}" else "Gasto · ${pago.categoria}",
                        style = MaterialTheme.typography.titleMedium)
                    Text(pesos(pago.monto), style = MaterialTheme.typography.headlineSmall)
                    Text(fechaParaMostrar(pago.fecha))
                    Text("Registrado por: ${usuarios.find { it.id == pago.registradoPorId }?.nombre.orEmpty()}")
                    if (pago.descripcion.isNotBlank()) Text(pago.descripcion)
                    pago.comprobanteUri?.let { uri ->
                        OutlinedButton(onClick = { comprobante = uri }) { Text("Ver comprobante") }
                    }
                }
            }
        }
    }
    comprobante?.let {
        AlertDialog(onDismissRequest = { comprobante = null },
            title = { Text("Comprobante adjunto") },
            text = { AsyncImage(model = it, contentDescription = "Comprobante del pago",
                modifier = Modifier.fillMaxWidth().height(320.dp)) },
            confirmButton = { TextButton(colors = coloresBotonTexto(), onClick = { comprobante = null }) { Text("Cerrar") } })
    }
}
