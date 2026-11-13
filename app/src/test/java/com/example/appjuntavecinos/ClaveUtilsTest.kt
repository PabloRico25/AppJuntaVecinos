package com.example.appjuntavecinos

import com.example.appjuntavecinos.utils.hashClave
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ClaveUtilsTest : StringSpec({

    "el hash de 1234 es el conocido" {
        hashClave("1234") shouldBe "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"
    }

    "la misma clave da el mismo hash" {
        hashClave("abcd") shouldBe hashClave("abcd")
    }

    "claves distintas dan hashes distintos" {
        hashClave("1234") shouldNotBe hashClave("1235")
    }

    "el hash tiene 64 caracteres" {
        hashClave("cualquier clave").length shouldBe 64
    }

    "el hash no es la clave en texto plano" {
        hashClave("1234") shouldNotBe "1234"
    }
})
