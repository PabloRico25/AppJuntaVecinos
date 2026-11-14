package com.example.appjuntavecinos

import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.utils.EstadoIngreso
import com.example.appjuntavecinos.utils.armarCodigoCorto
import com.example.appjuntavecinos.utils.armarContenidoQr
import com.example.appjuntavecinos.utils.codigoCortoEsValido
import com.example.appjuntavecinos.utils.evaluarIngreso
import com.example.appjuntavecinos.utils.leerCodigoCorto
import com.example.appjuntavecinos.utils.leerContenidoQr
import com.example.appjuntavecinos.utils.ventanaIngreso
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith

class QrUtilsTest : StringSpec({

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

    "el QR se arma y se lee con los mismos datos" {
        val datos = leerContenidoQr(armarContenidoQr(reserva, "Juan Pérez"))

        datos.shouldNotBeNull()
        datos.reservaId shouldBe 7
        datos.dependenciaId shouldBe 1
        datos.fecha shouldBe "2026-10-26"
        datos.horaInicio shouldBe "18:00"
        datos.horaFin shouldBe "21:00"
        datos.nombreVecino shouldBe "Juan Pérez"
    }

    "si se cambia un dato la firma ya no calza" {
        val alterado = armarContenidoQr(reserva, "Juan Pérez").replace("2026-10-26", "2026-10-27")

        leerContenidoQr(alterado).shouldBeNull()
    }

    "un texto cualquiera no es un QR válido" {
        leerContenidoQr("hola").shouldBeNull()
    }

    "dentro de la ventana el ingreso es válido" {
        val datos = leerContenidoQr(armarContenidoQr(reserva, "Juan Pérez"))!!

        evaluarIngreso(datos, "2026-10-26", "17:50") shouldBe EstadoIngreso.VALIDO
        evaluarIngreso(datos, "2026-10-26", "18:00") shouldBe EstadoIngreso.VALIDO
        evaluarIngreso(datos, "2026-10-26", "18:15") shouldBe EstadoIngreso.VALIDO
    }

    "fuera de la ventana el ingreso no es válido" {
        val datos = leerContenidoQr(armarContenidoQr(reserva, "Juan Pérez"))!!

        evaluarIngreso(datos, "2026-10-26", "17:49") shouldBe EstadoIngreso.FUERA_DE_HORARIO
        evaluarIngreso(datos, "2026-10-26", "18:16") shouldBe EstadoIngreso.FUERA_DE_HORARIO
    }

    "otro día el ingreso no es válido" {
        val datos = leerContenidoQr(armarContenidoQr(reserva, "Juan Pérez"))!!

        evaluarIngreso(datos, "2026-10-27", "18:00") shouldBe EstadoIngreso.FUERA_DE_HORARIO
    }

    "el código corto tiene la forma número, guion y firma" {
        val codigo = armarCodigoCorto(reserva)

        codigo shouldStartWith "7-"
        codigo.length shouldBe 8
    }

    "el código corto se lee y calza con su reserva" {
        val datos = leerCodigoCorto(armarCodigoCorto(reserva))

        datos.shouldNotBeNull()
        datos.reservaId shouldBe 7
        codigoCortoEsValido(reserva, datos.firma) shouldBe true
    }

    "si cambia la reserva el código corto ya no calza" {
        val datos = leerCodigoCorto(armarCodigoCorto(reserva))!!

        codigoCortoEsValido(reserva.copy(horaInicio = "19:00"), datos.firma) shouldBe false
    }

    "el código corto se acepta en minúsculas y con espacios" {
        val codigo = armarCodigoCorto(reserva)

        leerCodigoCorto("  " + codigo.lowercase() + " ").shouldNotBeNull()
    }

    "un texto sin la forma del código corto no se lee" {
        leerCodigoCorto("hola").shouldBeNull()
        leerCodigoCorto("7-12").shouldBeNull()
    }

    "la ventana de ingreso va de diez minutos antes a quince después" {
        ventanaIngreso("18:00") shouldBe "de 17:50 a 18:15"
        ventanaIngreso("09:00") shouldBe "de 08:50 a 09:15"
    }
})
