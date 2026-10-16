package com.example.appjuntavecinos

import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.utils.EstadoPuerta
import com.example.appjuntavecinos.utils.armarContenidoQr
import com.example.appjuntavecinos.utils.evaluarPuerta
import org.junit.Assert.assertEquals
import org.junit.Test

class PuertaUtilsTest {

    private val reserva = ReservaEntity(
        id = 7,
        dependenciaId = 1,
        vecinoId = 1,
        fecha = "2026-10-26",
        horaInicio = "18:00",
        horaFin = "21:00",
        estado = "CONFIRMADA",
        fechaSolicitud = "2026-10-20"
    )

    private val qr = armarContenidoQr(reserva, "Juan Pérez")

    @Test
    fun un_qr_valido_a_la_hora_correcta_abre_la_puerta() {
        val resultado = evaluarPuerta(qr, 1, "2026-10-26", "18:05", emptySet())
        assertEquals(EstadoPuerta.ABIERTA, resultado.estado)
        assertEquals("Bienvenido/a, Juan Pérez", resultado.mensaje)
        assertEquals(7, resultado.reservaId)
    }

    @Test
    fun un_texto_cualquiera_no_abre() {
        val resultado = evaluarPuerta("hola", 1, "2026-10-26", "18:05", emptySet())
        assertEquals(EstadoPuerta.CERRADA, resultado.estado)
        assertEquals("Código no válido", resultado.mensaje)
    }

    @Test
    fun un_qr_con_un_dato_cambiado_no_abre() {
        val alterado = qr.replace("2026-10-26", "2026-10-27")
        val resultado = evaluarPuerta(alterado, 1, "2026-10-27", "18:05", emptySet())
        assertEquals(EstadoPuerta.CERRADA, resultado.estado)
        assertEquals("Código no válido", resultado.mensaje)
    }

    @Test
    fun un_qr_de_otra_sala_no_abre() {
        val resultado = evaluarPuerta(qr, 2, "2026-10-26", "18:05", emptySet())
        assertEquals(EstadoPuerta.CERRADA, resultado.estado)
        assertEquals("Este código es de otra sala", resultado.mensaje)
    }

    @Test
    fun fuera_de_la_ventana_no_abre() {
        val temprano = evaluarPuerta(qr, 1, "2026-10-26", "17:00", emptySet())
        val otroDia = evaluarPuerta(qr, 1, "2026-10-27", "18:05", emptySet())
        assertEquals(EstadoPuerta.CERRADA, temprano.estado)
        assertEquals(EstadoPuerta.CERRADA, otroDia.estado)
        assertEquals("Fuera del horario de la reserva", temprano.mensaje)
    }

    @Test
    fun un_qr_ya_usado_no_abre_otra_vez() {
        val resultado = evaluarPuerta(qr, 1, "2026-10-26", "18:05", setOf(7))
        assertEquals(EstadoPuerta.CERRADA, resultado.estado)
        assertEquals("Este código ya se usó", resultado.mensaje)
    }
}