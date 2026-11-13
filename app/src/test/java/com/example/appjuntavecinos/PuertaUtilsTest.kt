package com.example.appjuntavecinos

import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.utils.EstadoPuerta
import com.example.appjuntavecinos.utils.MotivoPuerta
import com.example.appjuntavecinos.utils.armarContenidoQr
import com.example.appjuntavecinos.utils.ResultadoPuerta
import com.example.appjuntavecinos.utils.evaluarPuerta
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PuertaUtilsTest : StringSpec({

    val reserva = ReservaEntity(
        id = 7,
        dependenciaId = 1,
        vecinoId = 1,
        fecha = "2026-10-26",
        horaInicio = "18:00",
        horaFin = "21:00",
        estado = "CONFIRMADA",
        fechaSolicitud = "2026-10-20"
    )

    val qr = armarContenidoQr(reserva, "Juan Pérez")

    "un QR válido a la hora correcta abre la puerta" {
        val resultado = evaluarPuerta(qr, 1, "2026-10-26", "18:05", emptySet())
        resultado.estado shouldBe EstadoPuerta.ABIERTA
        resultado.mensaje shouldBe "Bienvenido/a, Juan Pérez"
        resultado.reservaId shouldBe 7
        resultado.motivo shouldBe MotivoPuerta.ABIERTA
    }

    "un texto cualquiera no abre" {
        val resultado = evaluarPuerta("hola", 1, "2026-10-26", "18:05", emptySet())
        resultado.estado shouldBe EstadoPuerta.CERRADA
        resultado.mensaje shouldBe "Código no válido"
        resultado.motivo shouldBe MotivoPuerta.NO_VALIDO
    }

    "un QR con un dato cambiado no abre" {
        val alterado = qr.replace("2026-10-26", "2026-10-27")
        val resultado = evaluarPuerta(alterado, 1, "2026-10-27", "18:05", emptySet())
        resultado.estado shouldBe EstadoPuerta.CERRADA
        resultado.mensaje shouldBe "Código no válido"
        resultado.motivo shouldBe MotivoPuerta.NO_VALIDO
    }

    "un QR de otra sala no abre" {
        val resultado = evaluarPuerta(qr, 2, "2026-10-26", "18:05", emptySet())
        resultado.estado shouldBe EstadoPuerta.CERRADA
        resultado.mensaje shouldBe "Este código es de otra sala"
        resultado.motivo shouldBe MotivoPuerta.OTRA_SALA
    }

    "fuera de la ventana no abre" {
        val temprano = evaluarPuerta(qr, 1, "2026-10-26", "17:00", emptySet())
        val otroDia = evaluarPuerta(qr, 1, "2026-10-27", "18:05", emptySet())
        temprano.estado shouldBe EstadoPuerta.CERRADA
        otroDia.estado shouldBe EstadoPuerta.CERRADA
        temprano.mensaje shouldBe "Fuera del horario de la reserva"
        temprano.motivo shouldBe MotivoPuerta.FUERA_DE_HORARIO
        otroDia.motivo shouldBe MotivoPuerta.FUERA_DE_HORARIO
    }

    "un QR ya usado no abre otra vez" {
        val resultado = evaluarPuerta(qr, 1, "2026-10-26", "18:05", setOf(7))
        resultado.estado shouldBe EstadoPuerta.CERRADA
        resultado.mensaje shouldBe "Este código ya se usó"
        resultado.motivo shouldBe MotivoPuerta.YA_USADO
    }
    "un resultado de puerta cerrada sin motivo explícito se trata como código no válido" {
        ResultadoPuerta(EstadoPuerta.CERRADA, "x").motivo shouldBe MotivoPuerta.NO_VALIDO
    }

    "un resultado de puerta abierta sin motivo explícito es ABIERTA" {
        ResultadoPuerta(EstadoPuerta.ABIERTA, "x").motivo shouldBe MotivoPuerta.ABIERTA
    }

    "si el código es de otra sala y además ya se usó, gana el motivo de la sala" {
        val resultado = evaluarPuerta(qr, 2, "2026-10-26", "18:05", setOf(7))
        resultado.motivo shouldBe MotivoPuerta.OTRA_SALA
    }

    "fuera de horario gana sobre ya usado" {
        val resultado = evaluarPuerta(qr, 1, "2026-10-26", "17:00", setOf(7))
        resultado.motivo shouldBe MotivoPuerta.FUERA_DE_HORARIO
    }
})
