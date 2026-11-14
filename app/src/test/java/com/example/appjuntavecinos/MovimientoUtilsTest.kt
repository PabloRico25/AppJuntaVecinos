package com.example.appjuntavecinos

import com.example.appjuntavecinos.utils.debeAparecerEscalonado
import com.example.appjuntavecinos.utils.desplazamientoSacudida
import com.example.appjuntavecinos.utils.escalaPop
import com.example.appjuntavecinos.utils.estadoSinConexion
import com.example.appjuntavecinos.utils.estadoSolicitudEnviada
import com.example.appjuntavecinos.utils.progresoDeTramo
import com.example.appjuntavecinos.utils.pulso
import com.example.appjuntavecinos.utils.retrasoEscalonado
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

class MovimientoUtilsTest : StringSpec({

    val tolerancia = 0.001f

    "un tramo que no ha empezado va en cero, uno en curso va por la mitad y uno terminado va en uno" {
        progresoDeTramo(100f, 400f, 500f) shouldBe 0f
        progresoDeTramo(650f, 400f, 500f) shouldBe (0.5f plusOrMinus tolerancia)
        progresoDeTramo(2000f, 400f, 500f) shouldBe 1f
    }

    "un tramo de duración cero salta de cero a uno" {
        progresoDeTramo(399f, 400f, 0f) shouldBe 0f
        progresoDeTramo(400f, 400f, 0f) shouldBe 1f
    }

    "el pop crece hasta 1,15 al 65 por ciento y se asienta en 1" {
        escalaPop(0f) shouldBe 0f
        escalaPop(0.65f) shouldBe (1.15f plusOrMinus tolerancia)
        escalaPop(1f) shouldBe (1f plusOrMinus tolerancia)
    }

    "el pop no se sale de rango" {
        escalaPop(-3f) shouldBe 0f
        escalaPop(9f) shouldBe (1f plusOrMinus tolerancia)
    }

    "la sacudida pasa por los puntos del kit y termina quieta" {
        desplazamientoSacudida(0f) shouldBe 0f
        desplazamientoSacudida(0.2f) shouldBe (-7f plusOrMinus tolerancia)
        desplazamientoSacudida(0.4f) shouldBe (7f plusOrMinus tolerancia)
        desplazamientoSacudida(0.6f) shouldBe (-5f plusOrMinus tolerancia)
        desplazamientoSacudida(0.8f) shouldBe (4f plusOrMinus tolerancia)
        desplazamientoSacudida(1f) shouldBe 0f
    }

    "la sacudida acepta otros puntos, como los 6 dp del banner" {
        desplazamientoSacudida(0.5f, listOf(0f, -6f, 6f, 0f)) shouldBe (0f plusOrMinus tolerancia)
        desplazamientoSacudida(1f / 3f, listOf(0f, -6f, 6f, 0f)) shouldBe (-6f plusOrMinus tolerancia)
    }

    "el pulso es cero antes de empezar, uno a mitad de vuelta y cero al cerrar la vuelta" {
        pulso(500f, 1000f) shouldBe 0f
        pulso(1900f, 1000f) shouldBe (1f plusOrMinus tolerancia)
        pulso(2800f, 1000f) shouldBe (0f plusOrMinus tolerancia)
    }

    "solo las primeras nueve tarjetas aparecen escalonadas, 40 ms una tras otra" {
        debeAparecerEscalonado(0) shouldBe true
        debeAparecerEscalonado(8) shouldBe true
        debeAparecerEscalonado(9) shouldBe false
        retrasoEscalonado(3) shouldBe 120L
        retrasoEscalonado(20) shouldBe 0L
    }

    "sin conexión: al empezar la nube no se ve y está 24 unidades más abajo" {
        val e = estadoSinConexion(0f)
        e.opacidadNube shouldBe 0f
        e.subidaNube shouldBe (24f plusOrMinus tolerancia)
        e.progresoRaya shouldBe 0f
    }

    "sin conexión: a los 560 ms la nube llegó y a los 850 ms la raya terminó de dibujarse" {
        estadoSinConexion(560f).opacidadNube shouldBe (1f plusOrMinus tolerancia)
        estadoSinConexion(560f).subidaNube shouldBe (0f plusOrMinus tolerancia)
        estadoSinConexion(850f).progresoRaya shouldBe (1f plusOrMinus tolerancia)
    }

    "sin conexión: los puntos esperan su turno y después parpadean entre 0,25 y 1" {
        estadoSinConexion(500f).opacidadPuntos shouldBe listOf(0.25f, 0.25f, 0.25f)
        estadoSinConexion(1900f).opacidadPuntos[0] shouldBe (1f plusOrMinus tolerancia)
    }

    "sin conexión con las animaciones desactivadas es el cuadro final, quieto" {
        val e = estadoSinConexion(0f, animar = false)
        e.opacidadNube shouldBe 1f
        e.subidaNube shouldBe 0f
        e.progresoRaya shouldBe 1f
        e.escalaNube shouldBe 1f
        e.opacidadPuntos shouldBe listOf(1f, 1f, 1f)
    }

    "solicitud enviada: al empezar solo hay un calendario por subir, sin avión ni insignia" {
        val e = estadoSolicitudEnviada(0f)
        e.opacidadCalendario shouldBe 0f
        e.opacidadAvion shouldBe 0f
        e.escalaInsignia shouldBe 0f
    }

    "solicitud enviada: el avión pasa por el centro con 12 grados de inclinación" {
        // El vuelo empieza a los 400 ms, dura 700 y llega al centro al 65 % de su recorrido
        val e = estadoSolicitudEnviada(400f + 0.65f * 700f)
        e.avionX shouldBe (104f plusOrMinus 0.01f)
        e.avionY shouldBe (104f plusOrMinus 0.01f)
        e.avionGrados shouldBe (-12f plusOrMinus 0.01f)
        e.opacidadAvion shouldBe (1f plusOrMinus tolerancia)
    }

    "solicitud enviada: al terminar el avión ya se desvaneció y la insignia quedó en tamaño normal" {
        val e = estadoSolicitudEnviada(1550f)
        e.opacidadAvion shouldBe (0f plusOrMinus tolerancia)
        e.escalaInsignia shouldBe (1f plusOrMinus tolerancia)
        e.opacidadCalendario shouldBe (1f plusOrMinus tolerancia)
    }

    "solicitud enviada: la insignia rebota hasta 1,15 antes de asentarse" {
        estadoSolicitudEnviada(1050f + 0.65f * 500f).escalaInsignia shouldBe (1.15f plusOrMinus tolerancia)
    }

    "solicitud enviada con las animaciones desactivadas es el cuadro final, sin avión" {
        val e = estadoSolicitudEnviada(0f, animar = false)
        e.opacidadCalendario shouldBe 1f
        e.subidaCalendario shouldBe 0f
        e.opacidadAvion shouldBe 0f
        e.opacidadEstela shouldBe 0f
        e.escalaInsignia shouldBe 1f
    }
})
