package com.example.appjuntavecinos

import com.example.appjuntavecinos.utils.sumarMeses
import org.junit.Assert.assertEquals
import org.junit.Test

class FechaUtilsTest {

    @Test
    fun suma_un_mes() {
        assertEquals("2026-11", sumarMeses("2026-10", 1))
    }

    @Test
    fun resta_un_mes() {
        assertEquals("2026-09", sumarMeses("2026-10", -1))
    }

    @Test
    fun al_restar_desde_enero_pasa_al_diciembre_del_año_anterior() {
        assertEquals("2025-12", sumarMeses("2026-01", -1))
    }

    @Test
    fun al_sumar_desde_diciembre_pasa_al_enero_del_año_siguiente() {
        assertEquals("2027-01", sumarMeses("2026-12", 1))
    }
}