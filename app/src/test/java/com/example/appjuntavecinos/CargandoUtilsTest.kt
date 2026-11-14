package com.example.appjuntavecinos

import com.example.appjuntavecinos.utils.opacidadCuadritoCargando
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.floats.shouldBeBetween

class CargandoUtilsTest : StringSpec({
    "antes de su inicio, el cuadrito está apagado" {
        opacidadCuadritoCargando(tiempoMs = 100f, inicioMs = 300f) shouldBe 0f
    }

    "al comenzar su vuelta está apagado y llega a pleno brillo al 18 %" {
        opacidadCuadritoCargando(tiempoMs = 0f, inicioMs = 0f) shouldBe (0f plusOrMinus 0.001f)
        opacidadCuadritoCargando(tiempoMs = 324f, inicioMs = 0f) shouldBe (1f plusOrMinus 0.001f)
    }

    "se mantiene encendido hasta el 55 %" {
        opacidadCuadritoCargando(tiempoMs = 700f, inicioMs = 0f) shouldBe 1f
        opacidadCuadritoCargando(tiempoMs = 990f, inicioMs = 0f) shouldBe (1f plusOrMinus 0.001f)
    }

    "se apaga entre el 55 % y el 75 %, y queda apagado el resto de la vuelta" {
        opacidadCuadritoCargando(tiempoMs = 1170f, inicioMs = 0f).shouldBeBetween(0.01f, 0.99f, 0f)
        opacidadCuadritoCargando(tiempoMs = 1500f, inicioMs = 0f) shouldBe 0f
    }

    "la animación se repite cada 1800 ms" {
        opacidadCuadritoCargando(tiempoMs = 2124f, inicioMs = 0f) shouldBe (1f plusOrMinus 0.001f)
    }

    "cada cuadrito arranca 300 ms después del anterior" {
        opacidadCuadritoCargando(tiempoMs = 624f, inicioMs = 300f) shouldBe (1f plusOrMinus 0.001f)
        opacidadCuadritoCargando(tiempoMs = 624f, inicioMs = 600f).shouldBeBetween(0f, 0.6f, 0f)
    }
})
