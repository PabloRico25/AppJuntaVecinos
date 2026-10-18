package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.MenuDesplegable
import com.example.appjuntavecinos.ui.theme.Green
import com.example.appjuntavecinos.ui.theme.LightBlue
import com.example.appjuntavecinos.ui.theme.TextoPrincipal
import com.example.appjuntavecinos.utils.HORA_APERTURA
import com.example.appjuntavecinos.utils.HORA_CIERRE
import com.example.appjuntavecinos.viewmodel.DisponibilidadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisponibilidadScreen(
    mostrarNombres: Boolean,
    viewModel: DisponibilidadViewModel = viewModel()
) {
    val dependencias by viewModel.dependencias.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    val dependenciaId by viewModel.dependenciaId.collectAsState()
    val fecha by viewModel.fecha.collectAsState()
    val franjas by viewModel.franjas.collectAsState(initial = emptyList())
    var mostrarCalendario by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // No visto en clase (lo integramos con permiso del docente): verticalScroll deja recorrer la pantalla hacia abajo.
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Disponibilidad", style = MaterialTheme.typography.headlineMedium)

        MenuDesplegable(
            etiqueta = "Dependencia",
            valor = dependencias.firstOrNull { it.id == dependenciaId }?.nombre ?: "",
            opciones = dependencias.map { it.nombre },
            onSeleccion = { viewModel.onDependenciaChange(dependencias[it].id) },
            error = null,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fecha,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = { mostrarCalendario = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Elegir fecha")
        }

        Text("Horario de la sede: $HORA_APERTURA a $HORA_CIERRE")

        franjas.forEach { franja ->
            val colorTarjeta = when (franja.tipo) {
                "CONFIRMADA" -> Green
                "SOLICITADA" -> LightBlue
                else -> Color.White
            }
            val etiqueta = when (franja.tipo) {
                "CONFIRMADA" -> "Confirmada"
                "SOLICITADA" -> "Solicitada"
                else -> "Libre"
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorTarjeta,
                    contentColor = TextoPrincipal
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${franja.horaInicio} a ${franja.horaFin}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(etiqueta)
                    if (mostrarNombres && franja.vecinoId != null) {
                        val nombre = usuarios.firstOrNull { it.id == franja.vecinoId }?.nombre ?: ""
                        Text(nombre)
                    }
                }
            }
        }

        Text("Solo las reservas confirmadas bloquean el horario. Las solicitadas aún esperan la respuesta de la Secretaría.")
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