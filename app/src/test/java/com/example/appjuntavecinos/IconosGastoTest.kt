package com.example.appjuntavecinos

import com.example.appjuntavecinos.ui.components.CategoriaDeGasto
import com.example.appjuntavecinos.ui.components.categoriaDeGasto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class IconosGastoTest : StringSpec({
    "cada categoría conocida tiene su ícono" {
        categoriaDeGasto("AGUA").icono shouldBe R.drawable.ic_gasto_agua
        categoriaDeGasto("ELECTRICIDAD").icono shouldBe R.drawable.ic_gasto_electricidad
        categoriaDeGasto("GAS").icono shouldBe R.drawable.ic_gasto_gas
    }

    "no importan las mayúsculas ni los espacios" {
        categoriaDeGasto(" agua ") shouldBe CategoriaDeGasto.AGUA
        categoriaDeGasto("Gas") shouldBe CategoriaDeGasto.GAS
    }

    "una categoría desconocida usa el ícono de otro gasto" {
        categoriaDeGasto("INTERNET") shouldBe CategoriaDeGasto.OTRO
        categoriaDeGasto("") shouldBe CategoriaDeGasto.OTRO
    }
})
