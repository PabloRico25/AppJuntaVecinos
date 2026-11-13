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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.components.AvisoInformativo
import com.example.appjuntavecinos.ui.components.EtiquetaEstado
import com.example.appjuntavecinos.ui.components.FichaIcono
import com.example.appjuntavecinos.ui.components.TarjetaBlanca
import com.example.appjuntavecinos.ui.components.aparecerEscalonado
import com.example.appjuntavecinos.ui.theme.JuntaTheme
import com.example.appjuntavecinos.utils.fechaParaMostrar
import com.example.appjuntavecinos.viewmodel.ReservasDirectivaViewModel

@Composable
fun ReservasDirectivaScreen(
    gestionadaPorId: Int,
    puedeConfirmar: Boolean,
    onAviso: (String) -> Unit,
    viewModel: ReservasDirectivaViewModel = viewModel()
) {
    val reservas by viewModel.reservas.collectAsState(initial = emptyList())
    val dependencias by viewModel.dependencias.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Reservas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)

        if (!puedeConfirmar) {
            AvisoInformativo("Solo la Secretaría puede confirmar o rechazar reservas.")
        }

        if (reservas.isEmpty()) {
            Text("Todavía no hay reservas.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(count = reservas.size, key = { indice -> reservas[indice].id }) { indice ->
                    val reserva = reservas[indice]
                    TarjetaBlanca(modifier = Modifier.fillMaxWidth().aparecerEscalonado(indice)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                FichaIcono { Icon(Icons.Filled.Home, contentDescription = null) }
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = dependencias.firstOrNull { it.id == reserva.dependenciaId }?.nombre ?: "",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        "Solicitada por ${usuarios.firstOrNull { it.id == reserva.vecinoId }?.nombre ?: ""}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "${fechaParaMostrar(reserva.fecha)}, de ${reserva.horaInicio} a ${reserva.horaFin}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    EtiquetaEstado(reserva.estado, etiquetaEstado(reserva.estado), Modifier.padding(top = 3.dp))
                                }
                            }
                            if (reserva.gestionadaPorId != null) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Text(
                                    "Gestionada por ${usuarios.firstOrNull { it.id == reserva.gestionadaPorId }?.nombre ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (reserva.estado == "PENDIENTE" && puedeConfirmar) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { viewModel.confirmar(reserva, gestionadaPorId, puedeConfirmar, onAviso) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Confirmar", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.rechazar(reserva, gestionadaPorId, puedeConfirmar, onAviso) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = JuntaTheme.colores.rechazadaContenedor,
                                            contentColor = JuntaTheme.colores.sobreRechazada
                                        )
                                    ) {
                                        Text("Rechazar", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}