package com.example.appjuntavecinos

import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.data.repository.horariosSeCruzan
import com.example.appjuntavecinos.utils.*
import com.example.appjuntavecinos.viewmodel.*
import org.junit.Assert.*
import org.junit.Test
import java.util.TimeZone

class ReglasReservaTest {
    private val hoy = "2026-10-16"
    private fun formulario() = NuevaReservaUiState(
        dependencia = DependenciaComunitariaEntity(1, "Salón Principal"), fecha = hoy,
        horaInicio = "09:00", horaFin = "22:00", aceptaReglamento = true
    )

    @Test fun horarioCompletoDentroDeLaSedeEsValido() {
        assertEquals(NuevaReservaErrores(), validarDatosReserva(formulario(), hoy))
    }
    @Test fun camposVaciosTienenErroresIndividuales() {
        val errores = validarDatosReserva(NuevaReservaUiState(), hoy)
        assertNotNull(errores.dependencia); assertNotNull(errores.fecha)
        assertNotNull(errores.horaInicio); assertNotNull(errores.horaFin); assertNotNull(errores.reglamento)
    }
    @Test fun rechazaFechaPasadaOInexistente() {
        assertNotNull(validarDatosReserva(formulario().copy(fecha = "2026-10-15"), hoy).fecha)
        assertNotNull(validarDatosReserva(formulario().copy(fecha = "2027-02-30"), hoy).fecha)
    }
    @Test fun rechazaIntervaloInvertidoYFueraDeHorario() {
        assertNotNull(validarDatosReserva(formulario().copy(horaFin = "09:00"), hoy).horaFin)
        assertNotNull(validarDatosReserva(formulario().copy(horaInicio = "08:00"), hoy).horaInicio)
        assertNotNull(validarDatosReserva(formulario().copy(horaFin = "23:00"), hoy).horaFin)
    }
    @Test fun crucesIncluyenContencionPeroNoHorariosContiguos() {
        assertTrue(horariosSeCruzan("10:00", "14:00", "11:00", "12:00"))
        assertTrue(horariosSeCruzan("11:00", "12:00", "10:00", "14:00"))
        assertFalse(horariosSeCruzan("10:00", "12:00", "12:00", "14:00"))
    }
    @Test fun disponibilidadDistinguePendienteConfirmadaYCancelada() {
        val pendiente = ReservaEntity(1, 1, 1, hoy, "10:00", "11:00", "PENDIENTE", hoy)
        val confirmada = pendiente.copy(id = 2, horaFin = "12:00", estado = "CONFIRMADA")
        val cancelada = pendiente.copy(id = 3, horaInicio = "13:00", horaFin = "14:00", estado = "CANCELADA")
        val franjas = calcularFranjas(listOf(pendiente, cancelada))
        assertEquals("SOLICITADO", franjas.first { it.inicio == "10:00" }.estado)
        assertEquals("LIBRE", franjas.first { it.inicio == "13:00" }.estado)
        assertEquals("OCUPADO", calcularFranjas(listOf(pendiente, confirmada)).first { it.inicio == "10:00" }.estado)
    }
    @Test fun fechaDelCalendarioNoRetrocedeEnChile() {
        val anterior = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Santiago"))
            assertEquals(hoy, fechaDesdeMillis(millisDesdeFecha(hoy)!!))
        } finally { TimeZone.setDefault(anterior) }
    }
    @Test fun resumenFiltraElMesYCalculaSaldo() {
        val pagos = listOf(
            PagoEntity(tipo = "ARRIENDO", monto = 60000.0, fecha = hoy, registradoPorId = 5),
            PagoEntity(tipo = "CONSUMO_BASICO", categoria = "AGUA", monto = 24200.0, fecha = hoy, registradoPorId = 5),
            PagoEntity(tipo = "ARRIENDO", monto = 999.0, fecha = "2026-09-01", registradoPorId = 5)
        )
        val resumen = calcularResumen(pagos, "2026-10")
        assertEquals(35800.0, resumen.saldo, 0.01)
        assertEquals(2, resumen.movimientos.size)
    }
}
