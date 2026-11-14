package com.example.appjuntavecinos

import com.example.appjuntavecinos.utils.partirMensaje
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TextoUtilsTest : StringSpec({

    "un mensaje de dos partes se separa en título y explicación" {
        partirMensaje("No se pudo conectar con el servidor. Revisa que esté encendido") shouldBe
            ("No se pudo conectar con el servidor" to "Revisa que esté encendido")
    }

    "un mensaje de una sola frase queda como título, sin explicación" {
        partirMensaje("El servidor no pudo atender la petición") shouldBe
            ("El servidor no pudo atender la petición" to null)
    }

    "un punto final no cuenta como separador" {
        partirMensaje("Ocurrió un error inesperado.") shouldBe ("Ocurrió un error inesperado." to null)
    }

    "si hay más de dos frases, solo se corta en la primera" {
        partirMensaje("Tu sesión venció. Inicia sesión de nuevo. Gracias") shouldBe
            ("Tu sesión venció" to "Inicia sesión de nuevo. Gracias")
    }

    "un texto vacío no rompe" {
        partirMensaje("") shouldBe ("" to null)
    }
})
