package com.example.appjuntavecinos

import com.example.appjuntavecinos.utils.barridoQr
import com.example.appjuntavecinos.utils.parpadeoQr
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.floats.shouldBeBetween
import io.kotest.matchers.shouldBe

class QrScanUtilsTest : StringSpec({
    "la línea de barrido parte arriba, llega abajo a los 1800 ms y vuelve arriba a los 3600" {
        barridoQr(0f) shouldBe (0f plusOrMinus 0.001f)
        barridoQr(1800f) shouldBe (1f plusOrMinus 0.001f)
        barridoQr(3600f) shouldBe (0f plusOrMinus 0.001f)
    }

    "a mitad de camino la línea está en el centro, y baja y sube de forma simétrica" {
        barridoQr(900f) shouldBe (0.5f plusOrMinus 0.01f)
        barridoQr(2700f) shouldBe (barridoQr(900f) plusOrMinus 0.01f)
    }

    "el barrido nunca se sale de 0 a 1 y se repite" {
        for (t in 0..8000 step 97) barridoQr(t.toFloat()).shouldBeBetween(0f, 1f, 0f)
        barridoQr(3600f + 700f) shouldBe (barridoQr(700f) plusOrMinus 0.001f)
    }

    "los módulos parpadean entre pleno brillo y la mitad" {
        parpadeoQr(0f) shouldBe (1f plusOrMinus 0.001f)
        parpadeoQr(900f) shouldBe (0.5f plusOrMinus 0.001f)
        parpadeoQr(1800f) shouldBe (1f plusOrMinus 0.001f)
        for (t in 0..4000 step 53) parpadeoQr(t.toFloat()).shouldBeBetween(0.5f, 1f, 0f)
    }
})
