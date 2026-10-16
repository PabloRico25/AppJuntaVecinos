package com.example.appjuntavecinos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.utils.*

@Composable
fun ContenidoFormulario(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 720.dp).fillMaxWidth().imePadding()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
fun MensajeError(mensaje: String?) {
    AnimatedVisibility(visible = mensaje != null) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
            Text(mensaje.orEmpty(), color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuDesplegable(
    etiqueta: String, valor: String, opciones: List<String>,
    onSeleccion: (Int) -> Unit, error: String? = null, habilitado: Boolean = true
) {
    var abierto by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { if (habilitado) abierto = it }) {
        OutlinedTextField(
            value = valor, onValueChange = {}, readOnly = true, enabled = habilitado,
            label = { Text(etiqueta) }, isError = error != null,
            trailingIcon = {
                if (error != null) Icon(Icons.Default.Warning, "Error")
                else ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto)
            },
            supportingText = { if (error != null) Text(error) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEachIndexed { indice, opcion ->
                DropdownMenuItem(text = { Text(opcion) }, onClick = { abierto = false; onSeleccion(indice) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(fecha: String, onFechaChange: (Long?) -> Unit, error: String? = null, habilitado: Boolean = true) {
    var abierto by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Fecha", style = MaterialTheme.typography.labelLarge)
        OutlinedButton(onClick = { abierto = true }, enabled = habilitado, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.DateRange, null)
            Spacer(Modifier.width(8.dp))
            Text(if (fecha.isBlank()) "Elegir fecha" else fechaParaMostrar(fecha))
        }
        MensajeError(error)
    }
    if (abierto) {
        val selector = rememberDatePickerState(initialSelectedDateMillis = millisDesdeFecha(fecha))
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(onClick = { onFechaChange(selector.selectedDateMillis); abierto = false },
                    enabled = selector.selectedDateMillis != null) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { abierto = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = selector)
        }
    }
}
