package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.ui.components.BannerError
import com.example.appjuntavecinos.ui.components.BotonContorno
import com.example.appjuntavecinos.ui.components.BotonTexto
import com.example.appjuntavecinos.ui.components.EsqueletoDisponibilidad
import com.example.appjuntavecinos.ui.components.EstadoDeError
import com.example.appjuntavecinos.ui.components.EtiquetaBloque
import com.example.appjuntavecinos.ui.components.FilaCampoYBoton
import com.example.appjuntavecinos.ui.components.MenuDesplegable
import com.example.appjuntavecinos.ui.components.TarjetaBlanca
import com.example.appjuntavecinos.ui.components.aparecerEscalonado
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
    val errorSalas by viewModel.errorSalas.collectAsState()
    var mostrarCalendario by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // No visto en clase (lo integramos con permiso del docente): verticalScroll deja recorrer la pantalla hacia abajo.
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Disponibilidad", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)

        MenuDesplegable(
            etiqueta = "Dependencia",
            valor = dependencias.firstOrNull { it.id == dependenciaId }?.nombre ?: if (errorSalas != null) "Sin salas" else "",
            opciones = dependencias.map { it.nombre },
            onSeleccion = { viewModel.onDependenciaChange(dependencias[it].id) },
            error = null,
            modifier = Modifier.fillMaxWidth()
        )

        FilaCampoYBoton(
            campo = { modificador ->
                OutlinedTextField(
                    value = fechaParaMostrar(fecha),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha") },
                    modifier = modificador
                )
            },
            boton = { modificador ->
                BotonContorno(onClick = { mostrarCalendario = true }, modifier = modificador) {
                    Icon(painterResource(R.drawable.ic_nav_calendario), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Elegir fecha")
                }
            }
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource(R.drawable.ic_reloj), contentDescription = null, modifier = Modifier.size(18.dp))
            Text("Horario de la sede: $HORA_APERTURA a $HORA_CIERRE", fontWeight = FontWeight.Bold)
        }

        errorSalas?.let {
            BannerError(it)
            BotonContorno(onClick = { viewModel.reintentar() }, modifier = Modifier.fillMaxWidth()) {
                Text("Reintentar")
            }
        }
        if (errorSalas == null && cargando) {
            EsqueletoDisponibilidad()
        }
        if (errorSalas == null && !cargando) {
            error?.let {
                EstadoDeError(mensaje = it, onReintentar = { viewModel.reintentar() })
            }
        }

        bloques.forEachIndexed { indice, bloque ->
            TarjetaBlanca(modifier = Modifier.fillMaxWidth().aparecerEscalonado(indice)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${bloque.horaInicio} a ${bloque.horaFin}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (mostrarNombres && bloque.vecinoId != null) {
                            Text(usuarios.firstOrNull { it.id == bloque.vecinoId }?.nombre ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    EtiquetaBloque(bloque.tipo)
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