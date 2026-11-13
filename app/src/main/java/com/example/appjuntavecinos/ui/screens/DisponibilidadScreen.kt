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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.BotonContorno
import com.example.appjuntavecinos.ui.components.BotonTexto
import com.example.appjuntavecinos.ui.components.MenuDesplegable
import com.example.appjuntavecinos.ui.theme.JuntaTheme
import com.example.appjuntavecinos.utils.HORA_APERTURA
import com.example.appjuntavecinos.utils.HORA_CIERRE
import com.example.appjuntavecinos.utils.fechaParaMostrar
import com.example.appjuntavecinos.viewmodel.DisponibilidadViewModel

@Composable
fun DisponibilidadScreen(
    mostrarNombres: Boolean,
    viewModel: DisponibilidadViewModel = viewModel()
) {
    val dependencias by viewModel.dependencias.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    val dependenciaId by viewModel.dependenciaId.collectAsState()
    val fecha by viewModel.fecha.collectAsState()
    val bloques by viewModel.bloques.collectAsState(initial = emptyList())
    val cargando by viewModel.cargando.collectAsState()
    val error by viewModel.error.collectAsState()
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
            value = fechaParaMostrar(fecha),
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha") },
            modifier = Modifier.fillMaxWidth()
        )
        BotonContorno(
            onClick = { mostrarCalendario = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Elegir fecha")
        }

        Text("Horario de la sede: $HORA_APERTURA a $HORA_CIERRE")

        if (cargando) {
            CircularProgressIndicator()
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        bloques.forEach { bloque ->
            val colorTarjeta = when (bloque.tipo) {
                "CONFIRMADA" -> JuntaTheme.colores.confirmadaContenedor
                "SOLICITADA" -> JuntaTheme.colores.pendienteContenedor
                else -> MaterialTheme.colorScheme.surfaceContainerHighest
            }
            val etiqueta = when (bloque.tipo) {
                "CONFIRMADA" -> "Confirmada"
                "SOLICITADA" -> "Solicitada"
                else -> "Libre"
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorTarjeta,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${bloque.horaInicio} a ${bloque.horaFin}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(etiqueta)
                    if (mostrarNombres && bloque.vecinoId != null) {
                        val nombre = usuarios.firstOrNull { it.id == bloque.vecinoId }?.nombre ?: ""
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
                BotonTexto(
                    onClick = {
                        viewModel.onFechaChange(calendario.selectedDateMillis)
                        mostrarCalendario = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                BotonTexto(onClick = { mostrarCalendario = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = calendario)
        }
    }
}