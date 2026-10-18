package com.example.appjuntavecinos.utils

import com.example.appjuntavecinos.data.model.PagoEntity

data class ResumenFinanciero(
    val ingresos: Int,
    val gastos: Int,
    val saldo: Int,
    val gastosPorCategoria: Map<String, Int>
)

fun calcularResumen(pagos: List<PagoEntity>, mes: String): ResumenFinanciero {
    val delMes = pagos.filter { it.fecha.startsWith(mes) }
    val ingresos = delMes.filter { it.tipo == "ARRIENDO" }.sumOf { it.monto }
    val consumos = delMes.filter { it.tipo == "CONSUMO_BASICO" }
    val gastos = consumos.sumOf { it.monto }
    val porCategoria = consumos
        .groupBy { it.categoria }
        .mapValues { entrada -> entrada.value.sumOf { it.monto } }
    return ResumenFinanciero(ingresos, gastos, ingresos - gastos, porCategoria)
}