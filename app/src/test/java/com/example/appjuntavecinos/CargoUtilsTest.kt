package com.example.appjuntavecinos

import com.example.appjuntavecinos.utils.nombreDelCargo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CargoUtilsTest : StringSpec({
    "un cargo vacío o nulo se muestra como Vecino" {
        nombreDelCargo("") shouldBe "Vecino"
        nombreDelCargo(null) shouldBe "Vecino"
        nombreDelCargo("   ") shouldBe "Vecino"
    }

    "los cargos conocidos se escriben con tilde y solo la inicial en mayúscula" {
        nombreDelCargo("SECRETARIA") shouldBe "Secretaría"
        nombreDelCargo("TESORERIA") shouldBe "Tesorería"
        nombreDelCargo("PRESIDENCIA") shouldBe "Presidencia"
    }

    "un cargo desconocido se muestra con la inicial en mayúscula" {
        nombreDelCargo("VOCAL") shouldBe "Vocal"
    }
})
