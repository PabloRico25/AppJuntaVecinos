package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.ui.components.ActualizarAlVolver
import com.example.appjuntavecinos.ui.components.BotonJunta
import com.example.appjuntavecinos.ui.components.EsqueletoListaReservas
import com.example.appjuntavecinos.ui.components.EstadoDeError
import com.example.appjuntavecinos.ui.components.EstadoVacio
import com.example.appjuntavecinos.ui.components.FranjaAviso
import com.example.appjuntavecinos.ui.components.TarjetaReserva
import com.example.appjuntavecinos.ui.components.aparecerEscalonado
import com.example.appjuntavecinos.ui.components.fotoDeSala
import com.example.appjuntavecinos.utils.fechaParaMostrar
import com.example.appjuntavecinos.viewmodel.MisReservasViewModel

@Composable
fun MisReservasScreen(
    vecinoId: Int,
    onNuevaReserva: () -> Unit,
    onDetalle: (Int) -> Unit,
    viewModel: MisReservasViewModel = viewModel()
) {
    ActualizarAlVolver { viewModel.reintentar() }
    val flujoDeReservas = remember(vecinoId) { viewModel.reservas(vecinoId) }
    val reservas by flujoDeReservas.collectAsState(initial = emptyList())
    val dependencias by viewModel.dependencias.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val sinActualizar by viewModel.sinActualizar.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Mis reservas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)

        BotonJunta(
            onClick = onNuevaReserva,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nueva reserva")
        }

        if (sinActualizar && reservas.isNotEmpty()) {
            FranjaAviso(
                texto = "No se pudo actualizar. Se muestran las reservas guardadas en este equipo",
                onReintentar = { viewModel.reintentar() }
            )
        }

        if (reservas.isEmpty() && cargando) {
            EsqueletoListaReservas()
        } else if (reservas.isEmpty() && sinActualizar) {
            EstadoDeError(
                mensaje = "No se pudo conectar con el servidor. Revisa que esté encendido",
                onReintentar = { viewModel.reintentar() }
            )
        } else if (reservas.isEmpty()) {
            EstadoVacio(R.drawable.ilus_sin_reservas, "Todavía no tienes reservas", mensaje = "Pide la primera con el botón Nueva reserva.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(count = reservas.size, key = { indice -> reservas[indice].id }) { indice ->
                    val reserva = reservas[indice]
                    val nombreSala = dependencias.firstOrNull { it.id == reserva.dependenciaId }?.nombre ?: ""
                    TarjetaReserva(
                        nombre = nombreSala,
                        foto = fotoDeSala(nombreSala),
                        detalle = "${fechaParaMostrar(reserva.fecha)}, de ${reserva.horaInicio} a ${reserva.horaFin}",
                        estado = reserva.estado,
                        textoEstado = etiquetaEstado(reserva.estado),
                        onClick = { onDetalle(reserva.id) },
                        modifier = Modifier.fillMaxWidth().aparecerEscalonado(indice)
                    )
                }
            }
        }
    }
}

fun etiquetaEstado(estado: String): String {
    return when (estado) {
        "PENDIENTE" -> "Pendiente"
        "CONFIRMADA" -> "Confirmada"
        "RECHAZADA" -> "Rechazada"
        "CANCELADA" -> "Cancelada"
        else -> estado
    }
}
