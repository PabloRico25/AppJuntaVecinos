package com.example.appjuntavecinos

import com.example.appjuntavecinos.navigation.Rutas
import com.example.appjuntavecinos.navigation.debeVolverAlInicioDeSesion
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RedireccionSesionTest : StringSpec({
    "sin sesión y en una pantalla de la app, se vuelve al inicio de sesión" {
        debeVolverAlInicioDeSesion(haySesion = false, ruta = Rutas.MIS_RESERVAS, modoPuerta = false) shouldBe true
        debeVolverAlInicioDeSesion(haySesion = false, ruta = Rutas.FINANZAS, modoPuerta = false) shouldBe true
        debeVolverAlInicioDeSesion(haySesion = false, ruta = Rutas.REGISTRAR_PAGO, modoPuerta = false) shouldBe true
        debeVolverAlInicioDeSesion(haySesion = false, ruta = Rutas.SOLICITUD_ENVIADA, modoPuerta = false) shouldBe true
    }

    "con sesión no se redirige" {
        debeVolverAlInicioDeSesion(haySesion = true, ruta = Rutas.MIS_RESERVAS, modoPuerta = false) shouldBe false
    }

    "en el login y en sesión vencida no se redirige, aunque no haya sesión" {
        debeVolverAlInicioDeSesion(haySesion = false, ruta = Rutas.LOGIN, modoPuerta = false) shouldBe false
        debeVolverAlInicioDeSesion(haySesion = false, ruta = Rutas.SESION_VENCIDA, modoPuerta = false) shouldBe false
    }

    "mientras no se sabe si es una puerta, o si lo es, no se redirige" {
        debeVolverAlInicioDeSesion(haySesion = false, ruta = Rutas.MIS_RESERVAS, modoPuerta = null) shouldBe false
        debeVolverAlInicioDeSesion(haySesion = false, ruta = Rutas.MIS_RESERVAS, modoPuerta = true) shouldBe false
    }

    "sin ruta todavía no se decide nada" {
        debeVolverAlInicioDeSesion(haySesion = false, ruta = null, modoPuerta = false) shouldBe false
    }
})
