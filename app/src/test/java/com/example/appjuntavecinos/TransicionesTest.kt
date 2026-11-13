package com.example.appjuntavecinos

import com.example.appjuntavecinos.navigation.Rutas
import com.example.appjuntavecinos.navigation.TipoTransicion
import com.example.appjuntavecinos.navigation.tipoAlAvanzar
import com.example.appjuntavecinos.navigation.tipoAlVolver
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TransicionesTest : StringSpec({

    "ir a una pantalla más adentro entra desde la derecha" {
        tipoAlAvanzar(Rutas.MIS_RESERVAS, Rutas.NUEVA_RESERVA) shouldBe TipoTransicion.PROFUNDA
        tipoAlAvanzar(Rutas.MIS_RESERVAS, Rutas.DETALLE_RESERVA) shouldBe TipoTransicion.PROFUNDA
        tipoAlAvanzar(Rutas.DETALLE_RESERVA, Rutas.TICKET_QR) shouldBe TipoTransicion.PROFUNDA
        tipoAlAvanzar(Rutas.PAGOS, Rutas.REGISTRAR_PAGO) shouldBe TipoTransicion.PROFUNDA
    }

    "cambiar de pestaña es un fundido cruzado, sin desplazamiento" {
        tipoAlAvanzar(Rutas.MIS_RESERVAS, Rutas.DISPONIBILIDAD) shouldBe TipoTransicion.PESTANAS
        tipoAlAvanzar(Rutas.RESERVAS, Rutas.PAGOS) shouldBe TipoTransicion.PESTANAS
    }

    "tocar una pestaña desde una pantalla interna no se anima como si fuera un avance" {
        tipoAlAvanzar(Rutas.DETALLE_RESERVA, Rutas.MIS_RESERVAS) shouldBe TipoTransicion.PESTANAS
    }

    "el login, la sesión vencida y la solicitud enviada usan el fundido lento, de entrada y de salida" {
        tipoAlAvanzar(Rutas.LOGIN, Rutas.MIS_RESERVAS) shouldBe TipoTransicion.FUNDIDO_LARGO
        tipoAlAvanzar(Rutas.MIS_RESERVAS, Rutas.SESION_VENCIDA) shouldBe TipoTransicion.FUNDIDO_LARGO
        tipoAlAvanzar(Rutas.NUEVA_RESERVA, Rutas.SOLICITUD_ENVIADA) shouldBe TipoTransicion.FUNDIDO_LARGO
        tipoAlVolver(Rutas.SOLICITUD_ENVIADA, Rutas.MIS_RESERVAS) shouldBe TipoTransicion.FUNDIDO_LARGO
    }

    "volver desde una pantalla interna se anima al revés" {
        tipoAlVolver(Rutas.DETALLE_RESERVA, Rutas.MIS_RESERVAS) shouldBe TipoTransicion.PROFUNDA
        tipoAlVolver(Rutas.TICKET_QR, Rutas.DETALLE_RESERVA) shouldBe TipoTransicion.PROFUNDA
    }

    "volver entre pestañas es un fundido" {
        tipoAlVolver(Rutas.DISPONIBILIDAD, Rutas.MIS_RESERVAS) shouldBe TipoTransicion.PESTANAS
    }

    "una ruta desconocida no rompe nada y se trata como pestaña" {
        tipoAlAvanzar(null, null) shouldBe TipoTransicion.PESTANAS
        tipoAlVolver(null, Rutas.MIS_RESERVAS) shouldBe TipoTransicion.PESTANAS
    }
})
