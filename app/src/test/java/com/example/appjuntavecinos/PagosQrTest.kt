package com.example.appjuntavecinos

import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.utils.*
import com.example.appjuntavecinos.viewmodel.validarDatosPago
import org.junit.Assert.*
import org.junit.Test

class PagosQrTest {
    private val reserva = ReservaEntity(42, 2, 1, "2026-10-20", "18:00", "20:00", "CONFIRMADA", "2026-10-17", 4)
    private val pago = RegistrarPagoUiState(tipo = "ARRIENDO", reserva = reserva, monto = "15000",
        fecha = "2026-10-17", confirma = true)

    @Test fun formularioVacioIndicaLosCamposObligatorios() {
        val errores = validarDatosPago(RegistrarPagoUiState(), "2026-10-17")
        assertNotNull(errores.tipo)
        assertNotNull(errores.monto)
        assertNotNull(errores.fecha)
        assertNotNull(errores.confirma)
    }
    @Test fun arriendoValidoNoExigeCategoriaNiComprobante() {
        assertEquals(RegistrarPagoErrores(), validarDatosPago(pago, "2026-10-17"))
        assertNotNull(validarDatosPago(pago.copy(reserva = reserva.copy(estado = "PENDIENTE"))).reserva)
    }
    @Test fun consumoExigeCategoriaYComprobanteSoloSiSeActiva() {
        val gasto = pago.copy(tipo = "CONSUMO_BASICO", reserva = null, categoria = "AGUA")
        assertEquals(RegistrarPagoErrores(), validarDatosPago(gasto, "2026-10-17"))
        assertNotNull(validarDatosPago(gasto.copy(categoria = "")).categoria)
        assertNotNull(validarDatosPago(gasto.copy(adjuntarComprobante = true)).comprobante)
    }
    @Test fun rechazaMontosInvalidosSinDesbordarse() {
        listOf("0", "-5", "1.5", "abc", "99999999999999999999").forEach {
            assertNotNull(it, validarDatosPago(pago.copy(monto = it)).monto)
        }
        assertNull(validarDatosPago(pago.copy(monto = "999999999")).monto)
    }
    @Test fun rechazaFechasImposiblesYFuturas() {
        assertNotNull(validarDatosPago(pago.copy(fecha = "2026-02-30")).fecha)
        assertNotNull(validarDatosPago(pago.copy(fecha = "2026-10-18"), "2026-10-17").fecha)
    }
    @Test fun ticketConservaTildesYDetectaAlteraciones() {
        val texto = armarContenidoQr(reserva, "Ana Muñoz")
        assertEquals("Ana Muñoz", leerContenidoQr(texto)?.nombreVecino)
        assertNull(leerContenidoQr(texto.replace("18:00", "19:00")))
        assertNull(leerContenidoQr(texto.replace("Muñoz", "Perez")))
        listOf("", "42-ABC123", "JV1|roto", "x".repeat(2001)).forEach { assertNull(leerContenidoQr(it)) }
    }
    @Test fun ventanaIncluyeSusDosLimites() {
        val datos = leerContenidoQr(armarContenidoQr(reserva, "Juan"))!!
        listOf("17:50", "18:00", "18:15").forEach {
            assertEquals(EstadoIngreso.VALIDO, evaluarIngreso(datos, reserva.fecha, it))
        }
        listOf("17:49", "18:16", "20:00").forEach {
            assertEquals(EstadoIngreso.FUERA_DE_HORARIO, evaluarIngreso(datos, reserva.fecha, it))
        }
        assertEquals(EstadoIngreso.FUERA_DE_HORARIO, evaluarIngreso(datos, "2026-10-21", "18:00"))
        assertEquals(EstadoIngreso.NO_VALIDO, evaluarIngreso(datos, reserva.fecha, "25:00"))
    }
    @Test fun codigoCortoCorrespondeALaReservaYHorario() {
        val codigo = leerCodigoCorto(armarCodigoCorto(reserva).lowercase())!!
        assertEquals(reserva.id, codigo.reservaId)
        assertTrue(codigoCortoEsValido(reserva, codigo.firma))
        assertFalse(codigoCortoEsValido(reserva.copy(horaInicio = "19:00"), codigo.firma))
        assertNull(leerCodigoCorto("-2-ABC123"))
        assertNull(leerCodigoCorto("42-GHIJKL"))
    }
    @Test fun puertaAceptaSoloSalaHorarioYPrimerUso() {
        val texto = armarContenidoQr(reserva, "Juan")
        assertEquals("ABIERTA", evaluarPuerta(texto, 2, reserva.fecha, "18:00", emptyList()).estado)
        assertEquals("CERRADA", evaluarPuerta(texto, 1, reserva.fecha, "18:00", emptyList()).estado)
        assertEquals("CERRADA", evaluarPuerta(texto, 2, reserva.fecha, "18:16", emptyList()).estado)
        assertEquals("CERRADA", evaluarPuerta(texto, 2, reserva.fecha, "18:00", listOf(42)).estado)
        assertEquals("CERRADA", evaluarPuerta("inventado", 2, reserva.fecha, "18:00", emptyList()).estado)
    }
}
