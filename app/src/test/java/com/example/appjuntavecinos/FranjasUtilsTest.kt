package com.example.appjuntavecinos

import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.utils.Franja
import com.example.appjuntavecinos.utils.calcularFranjas
import org.junit.Assert.assertEquals
import org.junit.Test

class FranjasUtilsTest {

    private fun reserva(
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

    @Test
    fun un_dia_sin_reservas_es_una_sola_franja_libre() {
        val franjas = calcularFranjas(emptyList(), 1, "2026-10-27")
        assertEquals(listOf(Franja("09:00", "22:00", "LIBRE")), franjas)
    }

    @Test
    fun una_confirmada_parte_el_dia_en_tres_franjas() {
        val franjas = calcularFranjas(listOf(reserva("18:00", "21:00", "CONFIRMADA")), 1, "2026-10-27")
        assertEquals(
            listOf(
                Franja("09:00", "18:00", "LIBRE"),
                Franja("18:00", "21:00", "CONFIRMADA", 1),
                Franja("21:00", "22:00", "LIBRE")
            ),
            franjas
        )
    }

    @Test
    fun una_solicitada_se_muestra_aparte_de_las_libres() {
        val franjas = calcularFranjas(listOf(reserva("11:00", "14:00", "PENDIENTE")), 1, "2026-10-27")
        assertEquals(
            listOf(
                Franja("09:00", "11:00", "LIBRE"),
                Franja("11:00", "14:00", "SOLICITADA", 1),
                Franja("14:00", "22:00", "LIBRE")
            ),
            franjas
        )
    }

    @Test
    fun una_solicitada_que_choca_con_la_confirmada_no_abre_un_hueco() {
        val reservas = listOf(
            reserva("18:00", "21:00", "CONFIRMADA", vecinoId = 1),
            reserva("19:00", "21:00", "PENDIENTE", vecinoId = 3)
        )
        val franjas = calcularFranjas(reservas, 1, "2026-10-27")
        assertEquals(
            listOf(
                Franja("09:00", "18:00", "LIBRE"),
                Franja("18:00", "21:00", "CONFIRMADA", 1),
                Franja("19:00", "21:00", "SOLICITADA", 3),
                Franja("21:00", "22:00", "LIBRE")
            ),
            franjas
        )
    }

    @Test
    fun se_ignoran_canceladas_rechazadas_otra_sala_y_otro_dia() {
        val reservas = listOf(
            reserva("10:00", "12:00", "CANCELADA"),
            reserva("12:00", "14:00", "RECHAZADA"),
            reserva("15:00", "17:00", "CONFIRMADA", dependenciaId = 2),
            reserva("16:00", "18:00", "CONFIRMADA", fecha = "2026-10-28")
        )
        val franjas = calcularFranjas(reservas, 1, "2026-10-27")
        assertEquals(listOf(Franja("09:00", "22:00", "LIBRE")), franjas)
    }
}