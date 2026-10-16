package com.example.appjuntavecinos

import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.utils.EstadoIngreso
import com.example.appjuntavecinos.utils.armarCodigoCorto
import com.example.appjuntavecinos.utils.armarContenidoQr
import com.example.appjuntavecinos.utils.codigoCortoEsValido
import com.example.appjuntavecinos.utils.evaluarIngreso
import com.example.appjuntavecinos.utils.leerCodigoCorto
import com.example.appjuntavecinos.utils.leerContenidoQr
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QrUtilsTest {

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

    @Test
    fun el_qr_se_arma_y_se_lee_con_los_mismos_datos() {
        val datos = leerContenidoQr(armarContenidoQr(reserva, "Juan Pérez"))

        assertNotNull(datos)
        assertEquals(7, datos!!.reservaId)
        assertEquals(1, datos.dependenciaId)
        assertEquals("2026-10-26", datos.fecha)
        assertEquals("18:00", datos.horaInicio)
        assertEquals("21:00", datos.horaFin)
        assertEquals("Juan Pérez", datos.nombreVecino)
    }

    @Test
    fun si_se_cambia_un_dato_la_firma_ya_no_calza() {
        val alterado = armarContenidoQr(reserva, "Juan Pérez").replace("2026-10-26", "2026-10-27")

        assertNull(leerContenidoQr(alterado))
    }

    @Test
    fun un_texto_cualquiera_no_es_un_qr_valido() {
        assertNull(leerContenidoQr("hola"))
    }

    @Test
    fun dentro_de_la_ventana_el_ingreso_es_valido() {
        val datos = leerContenidoQr(armarContenidoQr(reserva, "Juan Pérez"))!!

        assertEquals(EstadoIngreso.VALIDO, evaluarIngreso(datos, "2026-10-26", "17:50"))
        assertEquals(EstadoIngreso.VALIDO, evaluarIngreso(datos, "2026-10-26", "18:00"))
        assertEquals(EstadoIngreso.VALIDO, evaluarIngreso(datos, "2026-10-26", "18:15"))
    }

    @Test
    fun fuera_de_la_ventana_el_ingreso_no_es_valido() {
        val datos = leerContenidoQr(armarContenidoQr(reserva, "Juan Pérez"))!!

        assertEquals(EstadoIngreso.FUERA_DE_HORARIO, evaluarIngreso(datos, "2026-10-26", "17:49"))
        assertEquals(EstadoIngreso.FUERA_DE_HORARIO, evaluarIngreso(datos, "2026-10-26", "18:16"))
    }

    @Test
    fun otro_dia_el_ingreso_no_es_valido() {
        val datos = leerContenidoQr(armarContenidoQr(reserva, "Juan Pérez"))!!

        assertEquals(EstadoIngreso.FUERA_DE_HORARIO, evaluarIngreso(datos, "2026-10-27", "18:00"))
    }

    @Test
    fun el_codigo_corto_tiene_la_forma_numero_guion_firma() {
        val codigo = armarCodigoCorto(reserva)

        assertTrue(codigo.startsWith("7-"))
        assertEquals(8, codigo.length)
    }

    @Test
    fun el_codigo_corto_se_lee_y_calza_con_su_reserva() {
        val datos = leerCodigoCorto(armarCodigoCorto(reserva))

        assertNotNull(datos)
        assertEquals(7, datos!!.reservaId)
        assertTrue(codigoCortoEsValido(reserva, datos.firma))
    }

    @Test
    fun si_cambia_la_reserva_el_codigo_corto_ya_no_calza() {
        val datos = leerCodigoCorto(armarCodigoCorto(reserva))!!

        assertFalse(codigoCortoEsValido(reserva.copy(horaInicio = "19:00"), datos.firma))
    }

    @Test
    fun el_codigo_corto_se_acepta_en_minusculas_y_con_espacios() {
        val codigo = armarCodigoCorto(reserva)

        assertNotNull(leerCodigoCorto("  " + codigo.lowercase() + " "))
    }

    @Test
    fun un_texto_sin_la_forma_del_codigo_corto_no_se_lee() {
        assertNull(leerCodigoCorto("hola"))
        assertNull(leerCodigoCorto("7-12"))
    }
}