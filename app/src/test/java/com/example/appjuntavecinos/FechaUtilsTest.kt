package com.example.appjuntavecinos

import com.example.appjuntavecinos.utils.fechaParaMostrar
import com.example.appjuntavecinos.utils.sumarMeses
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FechaUtilsTest : StringSpec({

    "suma un mes" {
        sumarMeses("2026-10", 1) shouldBe "2026-11"
    }

    "resta un mes" {
        sumarMeses("2026-10", -1) shouldBe "2026-09"
    }

    "al restar desde enero pasa al diciembre del año anterior" {
        sumarMeses("2026-01", -1) shouldBe "2025-12"
    }

    "al sumar desde diciembre pasa al enero del año siguiente" {
        sumarMeses("2026-12", 1) shouldBe "2027-01"
    }

    "la fecha se muestra como día, mes y año" {
        fechaParaMostrar("2026-11-13") shouldBe "13-11-2026"
    }
})
