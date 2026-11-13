package com.example.appjuntavecinos

import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.utils.calcularResumen
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ResumenUtilsTest : StringSpec({

    val pagosDeOctubre = listOf(
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

    "octubre da los ingresos, gastos y saldo del caso" {
        val resumen = calcularResumen(pagosDeOctubre, "2026-10")
        resumen.ingresos shouldBe 100000
        resumen.gastos shouldBe 86700
        resumen.saldo shouldBe 13300
    }

    "un pago de otro mes no cuenta" {
        val deSeptiembre = PagoEntity(reservaId = 1, tipo = "ARRIENDO", monto = 999999,
            fecha = "2026-09-30", registradoPorId = 5)
        val resumen = calcularResumen(pagosDeOctubre + deSeptiembre, "2026-10")
        resumen.ingresos shouldBe 100000
        resumen.saldo shouldBe 13300
    }

    "los gastos se agrupan por categoría" {
        calcularResumen(pagosDeOctubre, "2026-10").gastosPorCategoria shouldBe
            mapOf("ELECTRICIDAD" to 38500, "AGUA" to 24200, "GAS" to 24000)
    }

    "sin pagos todo queda en cero" {
        val resumen = calcularResumen(emptyList(), "2026-10")
        resumen.ingresos shouldBe 0
        resumen.gastos shouldBe 0
        resumen.saldo shouldBe 0
        resumen.gastosPorCategoria shouldBe emptyMap()
    }
})
