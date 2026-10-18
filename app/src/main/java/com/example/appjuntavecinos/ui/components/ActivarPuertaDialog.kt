package com.example.appjuntavecinos.ui.components

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.viewmodel.PuertaViewModel

@Composable
fun TituloConSieteToques(texto: String, onActivar: () -> Unit) {
    var toques by remember { mutableIntStateOf(0) }
    var ultimo by remember { mutableLongStateOf(0) }
    Text(texto, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.clickable {
        val ahora = SystemClock.elapsedRealtime()
        toques = if (ahora - ultimo <= 2000) toques + 1 else 1
        ultimo = ahora
        if (toques == 7) { toques = 0; onActivar() }
    })
}

@Composable
fun ActivarPuertaDialog(viewModel: PuertaViewModel, onCerrar: () -> Unit) {
    val dependencias by viewModel.dependencias.collectAsState()
    val error by viewModel.error.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val procesando by viewModel.procesando.collectAsState()
    var sala by rememberSaveable { mutableStateOf<Int?>(null) }
    var clave by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = { if (!procesando) onCerrar() },
        title = { Text("Activar modo puerta") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Esta tablet quedará como lector de una sala. Necesitarás la clave para salir.")
                MenuDesplegable("Sala de la puerta", dependencias.find { it.id == sala }?.nombre.orEmpty(),
                    dependencias.map { it.nombre }, { sala = dependencias[it].id; viewModel.limpiarError() },
                    error = errores.sala, habilitado = !procesando)
                OutlinedTextField(value = clave, onValueChange = { clave = it; viewModel.limpiarError() },
                    colors = coloresCampo(),
                    label = { Text("Clave de la puerta") }, singleLine = true, enabled = !procesando,
                    isError = errores.clave != null,
                    supportingText = { errores.clave?.let { Text(it) } },
                    trailingIcon = { if (errores.clave != null) Icon(Icons.Default.Warning, "Error") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false,
                        capitalization = KeyboardCapitalization.None))
                MensajeError(error)
                Text("La puerta es simulada; no controla una cerradura física.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(colors = coloresBotonTexto(), onClick = { viewModel.activar(sala, clave, onCerrar) }, enabled = !procesando) { Text("Activar") } },
        dismissButton = { TextButton(colors = coloresBotonTexto(), onClick = onCerrar, enabled = !procesando) { Text("Cancelar") } })
}
