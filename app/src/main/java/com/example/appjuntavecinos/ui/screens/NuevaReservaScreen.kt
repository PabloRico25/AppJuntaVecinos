package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.MenuDesplegable
import com.example.appjuntavecinos.viewmodel.NuevaReservaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaReservaScreen(
    vecinoId: Int,
    onReservaCreada: () -> Unit,
    viewModel: NuevaReservaViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val dependencias by viewModel.dependencias.collectAsState()
    var mostrarCalendario by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // No visto en clase (lo integramos con permiso del docente): verticalScroll deja recorrer la pantalla hacia abajo.
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Nueva reserva", style = MaterialTheme.typography.headlineMedium)

        MenuDesplegable(
            etiqueta = "Dependencia",
            valor = estado.dependencia?.nombre ?: "",
            opciones = dependencias.map { it.nombre },
            onSeleccion = { viewModel.onDependenciaChange(dependencias[it]) },
            error = estado.errores.dependencia,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.fecha,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha") },
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

        MenuDesplegable(
            etiqueta = "Hora de inicio",
            valor = estado.horaInicio,
            opciones = viewModel.horas,
            onSeleccion = { viewModel.onHoraInicioChange(viewModel.horas[it]) },
            error = estado.errores.horaInicio,
            modifier = Modifier.fillMaxWidth()
        )

        MenuDesplegable(
            etiqueta = "Hora de fin",
            valor = estado.horaFin,
            opciones = viewModel.horas,
            onSeleccion = { viewModel.onHoraFinChange(viewModel.horas[it]) },
            error = estado.errores.horaFin,
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = estado.aceptaReglamento,
                onCheckedChange = { viewModel.onReglamentoChange(it) }
            )
            Text("Acepto el reglamento de la sede")
        }
        estado.errores.reglamento?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        estado.errores.general?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = { viewModel.guardar(vecinoId, onReservaCreada) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Solicitar reserva")
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