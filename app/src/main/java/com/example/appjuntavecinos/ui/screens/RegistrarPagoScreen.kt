package com.example.appjuntavecinos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.appjuntavecinos.ui.components.MenuDesplegable
import com.example.appjuntavecinos.utils.crearUriCamara
import com.example.appjuntavecinos.viewmodel.RegistrarPagoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarPagoScreen(
    registradoPorId: Int,
    puedeRegistrar: Boolean,
    onPagoRegistrado: () -> Unit,
    viewModel: RegistrarPagoViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val reservas by viewModel.reservas.collectAsState()
    val context = LocalContext.current
    var mostrarCalendario by remember { mutableStateOf(false) }
    var uriPendienteCamara by rememberSaveable { mutableStateOf<Uri?>(null) }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onComprobanteElegido(uri)
    }

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            viewModel.onComprobanteElegido(uriPendienteCamara)
        }
    }

    fun abrirCamara() {
        try {
            val uri = crearUriCamara(context)
            uriPendienteCamara = uri
            camaraLauncher.launch(uri)
        } catch (e: Exception) {
            viewModel.avisarComprobante("No se pudo abrir la cámara")
        }
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            viewModel.avisarComprobante("Sin permiso de cámara. Puedes elegir una imagen de la galería")
        }
    }

    fun tomarFotografia() {
        val concedido = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (concedido) {
            abrirCamara()
        } else {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Registrar pago", style = MaterialTheme.typography.headlineMedium)
        Text("Es un registro informativo: no se procesa ningún pago real.")

        MenuDesplegable(
            etiqueta = "Tipo de pago",
            valor = etiquetaTipo(estado.tipo),
            opciones = viewModel.tipos.map { etiquetaTipo(it) },
            onSeleccion = { viewModel.onTipoChange(viewModel.tipos[it]) },
            error = estado.errores.tipo,
            modifier = Modifier.fillMaxWidth()
        )

        if (estado.tipo == "ARRIENDO") {
            if (reservas.isEmpty()) {
                Text(
                    "No hay reservas confirmadas por pagar.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                MenuDesplegable(
                    etiqueta = "Reserva que se paga",
                    valor = estado.reserva?.let { viewModel.etiquetaReserva(it) } ?: "",
                    opciones = reservas.map { viewModel.etiquetaReserva(it) },
                    onSeleccion = { viewModel.onReservaChange(reservas[it]) },
                    error = estado.errores.reserva,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (estado.tipo == "CONSUMO_BASICO") {
            MenuDesplegable(
                etiqueta = "Categoría",
                valor = etiquetaCategoria(estado.categoria),
                opciones = viewModel.categorias.map { etiquetaCategoria(it) },
                onSeleccion = { viewModel.onCategoriaChange(viewModel.categorias[it]) },
                error = estado.errores.categoria,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = estado.monto,
            onValueChange = { viewModel.onMontoChange(it) },
            label = { Text("Monto en pesos") },
            prefix = { Text("$") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = estado.errores.monto != null,
            supportingText = { estado.errores.monto?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.fecha,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha del pago") },
            isError = estado.errores.fecha != null,
            supportingText = { estado.errores.fecha?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = { mostrarCalendario = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Elegir fecha")
        }

        OutlinedTextField(
            value = estado.descripcion,
            onValueChange = { viewModel.onDescripcionChange(it) },
            label = { Text("Descripción (opcional)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Adjuntar comprobante")
            Switch(
                checked = estado.adjuntarComprobante,
                onCheckedChange = { viewModel.onAdjuntarChange(it) }
            )
        }

        if (estado.adjuntarComprobante) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { tomarFotografia() }) {
                    Text("Tomar fotografía")
                }
                OutlinedButton(onClick = { galeriaLauncher.launch("image/*") }) {
                    Text("Elegir de la galería")
                }
            }
            if (estado.comprobante != null) {
                AsyncImage(
                    model = estado.comprobante,
                    contentDescription = "Comprobante elegido",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
            estado.errores.comprobante?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = estado.confirma,
                onCheckedChange = { viewModel.onConfirmaChange(it) }
            )
            Text("Confirmo que es un registro informativo")
        }
        estado.errores.confirma?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 12.dp))
        }

        estado.errores.general?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { viewModel.guardar(registradoPorId, puedeRegistrar, onPagoRegistrado) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar pago")
        }
    }

    if (mostrarCalendario) {
        val calendario = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onFechaChange(calendario.selectedDateMillis)
                        mostrarCalendario = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = calendario)
        }
    }
}

private fun etiquetaTipo(tipo: String): String {
    return when (tipo) {
        "ARRIENDO" -> "Arriendo"
        "CONSUMO_BASICO" -> "Consumo básico"
        else -> ""
    }
}

private fun etiquetaCategoria(categoria: String): String {
    return categoria.lowercase().replaceFirstChar { it.uppercase() }
}