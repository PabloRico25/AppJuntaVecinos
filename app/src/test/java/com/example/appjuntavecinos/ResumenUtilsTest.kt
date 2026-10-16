package com.example.appjuntavecinos

import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.utils.calcularResumen
import org.junit.Assert.assertEquals
import org.junit.Test

class ResumenUtilsTest {

    private val pagosDeOctubre = listOf(
        PagoEntity(reservaId = 8, tipo = "ARRIENDO", monto = 60000,
            fecha = "2026-10-04", registradoPorId = 5),
        PagoEntity(tipo = "CONSUMO_BASICO", categoria = "ELECTRICIDAD", monto = 38500,
            fecha = "2026-10-04", registradoPorId = 5),
        PagoEntity(reservaId = 7, tipo = "ARRIENDO", monto = 40000,
            fecha = "2026-10-03", registradoPorId = 5),
        PagoEntity(tipo = "CONSUMO_BASICO", categoria = "AGUA", monto = 24200,
            fecha = "2026-10-02", registradoPorId = 5),
        PagoEntity(tipo = "CONSUMO_BASICO", categoria = "GAS", monto = 24000,
            fecha = "2026-10-01", registradoPorId = 5)
    )

    @Test
    fun octubre_da_los_ingresos_gastos_y_saldo_del_caso() {
        val resumen = calcularResumen(pagosDeOctubre, "2026-10")
        assertEquals(100000, resumen.ingresos)
        assertEquals(86700, resumen.gastos)
        assertEquals(13300, resumen.saldo)
    }

    @Test
    fun un_pago_de_otro_mes_no_cuenta() {
        val deSeptiembre = PagoEntity(reservaId = 1, tipo = "ARRIENDO", monto = 999999,
            fecha = "2026-09-30", registradoPorId = 5)
        val resumen = calcularResumen(pagosDeOctubre + deSeptiembre, "2026-10")
        assertEquals(100000, resumen.ingresos)
        assertEquals(13300, resumen.saldo)
    }

    @Test
    fun los_gastos_se_agrupan_por_categoria() {
        val resumen = calcularResumen(pagosDeOctubre, "2026-10")
        assertEquals(
            mapOf("ELECTRICIDAD" to 38500, "AGUA" to 24200, "GAS" to 24000),
            resumen.gastosPorCategoria
        )
    }

    @Test
    fun sin_pagos_todo_queda_en_cero() {
        val resumen = calcularResumen(emptyList(), "2026-10")
        assertEquals(0, resumen.ingresos)
        assertEquals(0, resumen.gastos)
        assertEquals(0, resumen.saldo)
        assertEquals(emptyMap<String, Int>(), resumen.gastosPorCategoria)
    }
}