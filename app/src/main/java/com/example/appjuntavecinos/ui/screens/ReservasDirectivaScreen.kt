package com.example.appjuntavecinos.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.theme.ErrorRojo
import com.example.appjuntavecinos.ui.theme.TextoPrincipal
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
        Text("Reservas", style = MaterialTheme.typography.headlineMedium)

        if (!puedeConfirmar) {
            Text("Solo la Secretaría puede confirmar o rechazar reservas.")
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
                    val colorTarjeta by animateColorAsState(
                        targetValue = colorEstado(reserva.estado),
                        label = "colorTarjeta"
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colorTarjeta,
                            contentColor = TextoPrincipal
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = dependencias.firstOrNull { it.id == reserva.dependenciaId }?.nombre ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Solicitada por ${usuarios.firstOrNull { it.id == reserva.vecinoId }?.nombre ?: ""}")
                            Text("${fechaParaMostrar(reserva.fecha)}, de ${reserva.horaInicio} a ${reserva.horaFin}")
                            Text("Estado: ${etiquetaEstado(reserva.estado)}")
                            if (reserva.gestionadaPorId != null) {
                                Text("Gestionada por ${usuarios.firstOrNull { it.id == reserva.gestionadaPorId }?.nombre ?: ""}")
                            }
                            if (reserva.estado == "PENDIENTE" && puedeConfirmar) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.rechazar(reserva, gestionadaPorId, puedeConfirmar, onAviso)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ErrorRojo,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Rechazar", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.confirmar(reserva, gestionadaPorId, puedeConfirmar, onAviso)
                                        }
                                    ) {
                                        Text("Confirmar", fontWeight = FontWeight.Bold)
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