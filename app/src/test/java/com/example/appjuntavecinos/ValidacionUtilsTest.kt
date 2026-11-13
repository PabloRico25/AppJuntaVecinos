package com.example.appjuntavecinos

import com.example.appjuntavecinos.utils.correoEsValido
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ValidacionUtilsTest : StringSpec({

    "los correos de la junta son válidos" {
        correoEsValido("juan.perez@zaror.test") shouldBe true
        correoEsValido("maria.torres@zaror.test") shouldBe true
    }

    "un correo con subdominios y símbolos comunes es válido" {
        correoEsValido("a.b+c@correo.empresa.cl") shouldBe true
    }

    "un texto sin arroba no es un correo" {
        correoEsValido("juan") shouldBe false
        correoEsValido("juan.perez.zaror.test") shouldBe false
    }

    "un correo sin dominio o sin extensión no es válido" {
        correoEsValido("juan@") shouldBe false
        correoEsValido("juan@zaror") shouldBe false
        correoEsValido("@zaror.test") shouldBe false
    }

    "un correo con espacios no es válido" {
        correoEsValido("juan perez@zaror.test") shouldBe false
        correoEsValido(" juan@zaror.test") shouldBe false
    }

    "un texto vacío no es un correo" {
        correoEsValido("") shouldBe false
    }
})
