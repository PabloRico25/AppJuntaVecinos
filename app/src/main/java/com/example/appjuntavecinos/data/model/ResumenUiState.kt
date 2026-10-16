package com.example.appjuntavecinos.data.model

import com.example.appjuntavecinos.utils.fechaDeHoy

data class ResumenUiState(
    val mes: String = fechaDeHoy().take(7),
    val ingresos: Double = 0.0,
    val gastos: Double = 0.0,
    val gastosPorCategoria: Map<String, Double> = emptyMap(),
    val movimientos: List<PagoEntity> = emptyList(),
    val error: String? = null
) {
    val saldo: Double get() = ingresos - gastos
}
