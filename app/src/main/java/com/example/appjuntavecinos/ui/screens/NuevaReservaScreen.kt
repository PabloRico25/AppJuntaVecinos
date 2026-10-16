package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.*
import com.example.appjuntavecinos.viewmodel.NuevaReservaViewModel

@Composable
fun NuevaReservaScreen(
    vecinoId: Int, onReservaCreada: () -> Unit, onDisponibilidad: () -> Unit,
    viewModel: NuevaReservaViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val dependencias by viewModel.dependencias.collectAsState()
    ContenidoFormulario {
        Text("Solicita un espacio para tu actividad", style = MaterialTheme.typography.titleLarge)
        Text("La sede atiende todos los días de 09:00 a 22:00.")
        MenuDesplegable("Dependencia", estado.dependencia?.nombre.orEmpty(), dependencias.map { it.nombre },
            { viewModel.onDependenciaChange(dependencias[it]) }, estado.errores.dependencia, !estado.guardando)
        CampoFecha(estado.fecha, viewModel::onFechaChange, estado.errores.fecha, !estado.guardando)
        MenuDesplegable("Hora de inicio", estado.horaInicio, viewModel.horas,
            { viewModel.onHoraInicioChange(viewModel.horas[it]) }, estado.errores.horaInicio, !estado.guardando)
        MenuDesplegable("Hora de fin", estado.horaFin, viewModel.horas,
            { viewModel.onHoraFinChange(viewModel.horas[it]) }, estado.errores.horaFin, !estado.guardando)
        Row {
            Checkbox(checked = estado.aceptaReglamento, onCheckedChange = viewModel::onReglamentoChange,
                enabled = !estado.guardando)
            Text("Acepto respetar el horario y cuidar las instalaciones de la sede.", Modifier.padding(top = 12.dp))
        }
        MensajeError(estado.errores.reglamento)
        MensajeError(estado.errores.general)
        OutlinedButton(onClick = onDisponibilidad, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth()) {
            Text("Ver disponibilidad")
        }
        Button(onClick = { viewModel.guardar(vecinoId, onReservaCreada) },
            enabled = !estado.guardando, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(if (estado.guardando) "Guardando…" else "Enviar solicitud")
        }
        if (estado.guardando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text("Secretaría revisará tu solicitud. Una solicitud pendiente todavía no bloquea el horario.",
            style = MaterialTheme.typography.bodySmall)
    }
}
