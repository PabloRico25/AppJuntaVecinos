package com.example.appjuntavecinos

import com.example.appjuntavecinos.ui.components.fotoDeSala
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FotosSalasTest : StringSpec({
    "cada sala conocida tiene su foto" {
        fotoDeSala("Salón Principal") shouldBe R.drawable.foto_salon_principal
        fotoDeSala("Sala Multiuso") shouldBe R.drawable.foto_sala_multiuso
    }

    "no importan las mayúsculas del nombre" {
        fotoDeSala("SALA MULTIUSO") shouldBe R.drawable.foto_sala_multiuso
    }

    "una sala sin foto devuelve null y la tarjeta queda con fondo liso" {
        fotoDeSala("Cancha") shouldBe null
        fotoDeSala("") shouldBe null
    }
})
