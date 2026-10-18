package com.example.appjuntavecinos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.*
import com.example.appjuntavecinos.ui.components.*
import com.example.appjuntavecinos.ui.theme.*
import com.example.appjuntavecinos.utils.*
import com.example.appjuntavecinos.viewmodel.EscanearViewModel

@Composable
fun EscanearScreen(viewModel: EscanearViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    val dependencias by viewModel.dependencias.collectAsState()
    val context = LocalContext.current
    var leyendo by remember { mutableStateOf(false) }
    ContenidoFormulario {
        Text("Validar un ticket", style = MaterialTheme.typography.headlineSmall)
        MenuDesplegable("Estás en", dependencias.find { it.id == estado.dependenciaId }?.nombre.orEmpty(),
            dependencias.map { it.nombre }, { viewModel.onDependenciaChange(dependencias[it].id) },
            habilitado = !leyendo && !estado.validando)
        Button(onClick = {
            leyendo = true
            try {
                val opciones = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).enableAutoZoom().build()
                GmsBarcodeScanning.getClient(context, opciones).startScan()
                    .addOnSuccessListener { leyendo = false; viewModel.validar(it.rawValue.orEmpty()) }
                    .addOnCanceledListener { leyendo = false }
                    .addOnFailureListener {
                        leyendo = false
                        viewModel.avisarError("No se pudo iniciar el lector. Comprueba Google Play Services y conexión para su primera descarga, o escribe el código.")
                    }
            } catch (_: Exception) { leyendo = false; viewModel.avisarError("No se pudo abrir el lector") }
        }, enabled = estado.dependenciaId != null && !leyendo && !estado.validando,
            modifier = Modifier.fillMaxWidth()) { Text(if (leyendo) "Abriendo cámara…" else "Escanear QR") }
        Text("También puedes escribir el código corto del ticket en el dispositivo donde se creó la reserva.")
        OutlinedTextField(value = estado.codigo, onValueChange = viewModel::onCodigoChange,
            colors = coloresCampo(),
            label = { Text("Código") }, enabled = !estado.validando && !leyendo, maxLines = 4, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = viewModel::validarEscrito, enabled = !estado.validando && !leyendo,
            modifier = Modifier.fillMaxWidth()) { Text("Validar código") }
        MensajeError(estado.error)
        if (estado.validando || leyendo) LinearProgressIndicator(Modifier.fillMaxWidth())
        AnimatedVisibility(estado.resultado != null) {
            estado.resultado?.let { resultado ->
                val valido = resultado.estado == EstadoIngreso.VALIDO
                Card(colors = CardDefaults.cardColors(containerColor = if (valido) Green else Rechazada)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(if (valido) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = if (valido) "Acceso válido" else "Acceso rechazado",
                            tint = if (valido) TextoConfirmada else TextoRechazada)
                        Text(resultado.motivo ?: when (resultado.estado) {
                            EstadoIngreso.VALIDO -> "Acceso permitido"
                            EstadoIngreso.FUERA_DE_HORARIO -> "Fuera de horario"
                            EstadoIngreso.NO_VALIDO -> "Código no válido"
                        }, style = MaterialTheme.typography.titleLarge, color = TextoPrincipal)
                        resultado.datos?.let {
                            Text(it.nombreVecino, color = TextoPrincipal)
                            Text("${fechaParaMostrar(it.fecha)} · ${it.horaInicio} a ${it.horaFin}", color = TextoPrincipal)
                            Text("Ingreso: ${ventanaIngreso(it)}", color = TextoPrincipal)
                        }
                    }
                }
            }
        }
        Text("Esta consulta no consume el ticket. El modo puerta registra su uso en ese aparato.",
            style = MaterialTheme.typography.bodySmall)
    }
}
