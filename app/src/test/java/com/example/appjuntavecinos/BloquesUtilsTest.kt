package com.example.appjuntavecinos

import com.example.appjuntavecinos.data.model.BloqueDto
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.utils.BloqueHorario
import com.example.appjuntavecinos.utils.calcularBloques
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BloquesUtilsTest : StringSpec({

    fun reserva(
        inicio: String,
        fin: String,
        estado: String,
        vecinoId: Int = 1,
        dependenciaId: Int = 1,
        fecha: String = "2026-10-27"
    ) = ReservaEntity(
        dependenciaId = dependenciaId,
        vecinoId = vecinoId,
        fecha = fecha,
        horaInicio = inicio,
        horaFin = fin,
        estado = estado,
        fechaSolicitud = "2026-10-20"
    )

    "un día sin reservas es un solo bloque libre" {
        calcularBloques(emptyList<ReservaEntity>(), 1, "2026-10-27") shouldBe
            listOf(BloqueHorario("09:00", "22:00", "LIBRE"))
    }

    "una confirmada parte el día en tres bloques" {
        val bloques = calcularBloques(listOf(reserva("18:00", "21:00", "CONFIRMADA")), 1, "2026-10-27")
        bloques shouldBe listOf(
            BloqueHorario("09:00", "18:00", "LIBRE"),
            BloqueHorario("18:00", "21:00", "CONFIRMADA", 1),
            BloqueHorario("21:00", "22:00", "LIBRE")
        )
    }

    "una solicitada se muestra aparte de las libres" {
        val bloques = calcularBloques(listOf(reserva("11:00", "14:00", "PENDIENTE")), 1, "2026-10-27")
        bloques shouldBe listOf(
            BloqueHorario("09:00", "11:00", "LIBRE"),
            BloqueHorario("11:00", "14:00", "SOLICITADA", 1),
            BloqueHorario("14:00", "22:00", "LIBRE")
        )
    }

    "una solicitada que choca con la confirmada no abre un hueco" {
        val reservas = listOf(
            reserva("18:00", "21:00", "CONFIRMADA", vecinoId = 1),
            reserva("19:00", "21:00", "PENDIENTE", vecinoId = 3)
        )
        calcularBloques(reservas, 1, "2026-10-27") shouldBe listOf(
            BloqueHorario("09:00", "18:00", "LIBRE"),
            BloqueHorario("18:00", "21:00", "CONFIRMADA", 1),
            BloqueHorario("19:00", "21:00", "SOLICITADA", 3),
            BloqueHorario("21:00", "22:00", "LIBRE")
        )
    }

    "se ignoran canceladas, rechazadas, otra sala y otro día" {
        val reservas = listOf(
            reserva("10:00", "12:00", "CANCELADA"),
            reserva("12:00", "14:00", "RECHAZADA"),
            reserva("15:00", "17:00", "CONFIRMADA", dependenciaId = 2),
            reserva("16:00", "18:00", "CONFIRMADA", fecha = "2026-10-28")
        )
        calcularBloques(reservas, 1, "2026-10-27") shouldBe listOf(BloqueHorario("09:00", "22:00", "LIBRE"))
    }

    "con los bloques del servidor, un vecino no ve quién reservó" {
        // El servidor entrega vecinoId = null a los vecinos: el bloque debe conservarlo así
        val ocupados = listOf(BloqueDto("18:00", "21:00", "CONFIRMADA", null))
        calcularBloques(ocupados) shouldBe listOf(
            BloqueHorario("09:00", "18:00", "LIBRE"),
            BloqueHorario("18:00", "21:00", "CONFIRMADA", null),
            BloqueHorario("21:00", "22:00", "LIBRE")
        )
    }

    "los bloques del servidor se ordenan por hora aunque lleguen desordenados" {
        val ocupados = listOf(
            BloqueDto("15:00", "17:00", "CONFIRMADA", null),
            BloqueDto("10:00", "12:00", "PENDIENTE", null)
        )
        calcularBloques(ocupados).map { it.horaInicio } shouldBe
            listOf("09:00", "10:00", "12:00", "15:00", "17:00")
    }
})
