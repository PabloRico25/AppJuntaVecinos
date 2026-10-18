package com.example.appjuntavecinos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.appjuntavecinos.ui.components.*
import com.example.appjuntavecinos.utils.crearUriCamara
import com.example.appjuntavecinos.viewmodel.RegistrarPagoViewModel

@Composable
fun RegistrarPagoScreen(registradoPorId: Int, puedeRegistrar: Boolean, onPagoRegistrado: () -> Unit,
                        viewModel: RegistrarPagoViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    val reservas by viewModel.reservas.collectAsState()
    val context = LocalContext.current
    var uriCamara by rememberSaveable { mutableStateOf<String?>(null) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardada ->
        if (guardada) uriCamara?.let { viewModel.onComprobanteElegido(Uri.parse(it)) }
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.onComprobanteElegido(it)
    }
    fun tomarFoto() {
        try {
            val uri = crearUriCamara(context)
            uriCamara = uri.toString()
            camara.launch(uri)
        } catch (_: Exception) { viewModel.avisarComprobante("No se pudo abrir la cámara. Puedes elegir una imagen de la galería.") }
    }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) tomarFoto() else viewModel.avisarComprobante("Permiso de cámara denegado. Puedes usar la galería.")
    }
    ContenidoFormulario {
        if (!puedeRegistrar) {
            Text("Solo la Tesorería puede registrar pagos.")
            return@ContenidoFormulario
        }
        Text("Registrar un pago", style = MaterialTheme.typography.headlineSmall)
        Text("Los montos se registran en pesos chilenos. No se realizan cobros bancarios.")
        Text("Tipo de pago", style = MaterialTheme.typography.titleMedium)
        viewModel.tipos.forEach { tipo ->
            Row(Modifier.fillMaxWidth().selectable(selected = estado.tipo == tipo, enabled = !estado.guardando,
                role = Role.RadioButton, onClick = { viewModel.onTipoChange(tipo) })) {
                RadioButton(selected = estado.tipo == tipo, onClick = null, enabled = !estado.guardando, modifier = Modifier.padding(12.dp))
                Text(if (tipo == "ARRIENDO") "Arriendo" else "Consumo básico", Modifier.padding(top = 12.dp))
            }
        }
        MensajeError(estado.errores.tipo)
        if (estado.tipo == "ARRIENDO") {
            if (reservas.isEmpty()) Text("No hay reservas confirmadas por pagar.")
            MenuDesplegable("Reserva", estado.reserva?.let(viewModel::etiquetaReserva).orEmpty(),
                reservas.map(viewModel::etiquetaReserva), { viewModel.onReservaChange(reservas[it]) },
                estado.errores.reserva, !estado.guardando && reservas.isNotEmpty())
        }
        if (estado.tipo == "CONSUMO_BASICO") {
            MenuDesplegable("Categoría", estado.categoria, viewModel.categorias,
                { viewModel.onCategoriaChange(viewModel.categorias[it]) }, estado.errores.categoria, !estado.guardando)
        }
        OutlinedTextField(value = estado.monto, onValueChange = viewModel::onMontoChange,
            colors = coloresCampo(),
            label = { Text("Monto en pesos") }, supportingText = { Text(estado.errores.monto ?: "Sin puntos ni decimales; hasta 9 dígitos.") },
            isError = estado.errores.monto != null, singleLine = true, enabled = !estado.guardando,
            trailingIcon = { if (estado.errores.monto != null) Icon(Icons.Default.Warning, "Error") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        CampoFecha(estado.fecha, viewModel::onFechaChange, estado.errores.fecha, !estado.guardando)
        OutlinedTextField(value = estado.descripcion, onValueChange = viewModel::onDescripcionChange,
            colors = coloresCampo(),
            label = { Text("Descripción (opcional)") }, supportingText = { Text("${estado.descripcion.length}/240") },
            enabled = !estado.guardando, modifier = Modifier.fillMaxWidth(), minLines = 2)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Switch(checked = estado.adjuntarComprobante, onCheckedChange = viewModel::onAdjuntarChange, enabled = !estado.guardando)
            Text("Adjuntar comprobante", Modifier.padding(top = 12.dp))
        }
        AnimatedVisibility(estado.adjuntarComprobante) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Elige una imagen de hasta 20 MB.")
                OutlinedButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) tomarFoto()
                    else permiso.launch(Manifest.permission.CAMERA)
                }, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth()) { Text("Tomar foto") }
                OutlinedButton(onClick = {
                    try { galeria.launch("image/*") }
                    catch (_: Exception) { viewModel.avisarComprobante("No se pudo abrir la galería") }
                }, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth()) { Text("Elegir de galería") }
                estado.comprobante?.let {
                    AsyncImage(model = it, contentDescription = "Vista previa del comprobante",
                        modifier = Modifier.fillMaxWidth().height(220.dp))
                }
                MensajeError(estado.errores.comprobante)
            }
        }
        Row(Modifier.fillMaxWidth().toggleable(value = estado.confirma, enabled = !estado.guardando,
            role = Role.Checkbox, onValueChange = viewModel::onConfirmaChange)) {
            Checkbox(checked = estado.confirma, onCheckedChange = null, enabled = !estado.guardando, modifier = Modifier.padding(12.dp))
            Text("Confirmo que este pago es un registro informativo.", Modifier.padding(top = 12.dp))
        }
        MensajeError(estado.errores.confirma)
        MensajeError(estado.errores.general)
        Button(onClick = { viewModel.guardar(registradoPorId, puedeRegistrar, onPagoRegistrado) },
            enabled = !estado.guardando, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(if (estado.guardando) "Guardando…" else "Guardar pago")
        }
        if (estado.guardando) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}
