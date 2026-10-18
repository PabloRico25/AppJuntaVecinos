package com.example.appjuntavecinos.viewmodel

import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.utils.*

fun validarDatosPago(datos: RegistrarPagoUiState, hoy: String = fechaDeHoy()): RegistrarPagoErrores =
    RegistrarPagoErrores(
        tipo = if (datos.tipo !in listOf("ARRIENDO", "CONSUMO_BASICO")) "Elige el tipo de pago" else null,
        reserva = if (datos.tipo == "ARRIENDO" && datos.reserva?.estado != "CONFIRMADA") "Elige la reserva que se paga" else null,
        categoria = if (datos.tipo == "CONSUMO_BASICO" && datos.categoria !in listOf("ELECTRICIDAD", "AGUA", "GAS"))
            "Elige la categoría del gasto" else null,
        monto = when {
            datos.monto.isBlank() -> "Escribe el monto"
            datos.monto.toIntOrNull() == null || datos.monto.length > 9 -> "Usa hasta 9 dígitos, sin puntos"
            datos.monto.toInt() <= 0 -> "El monto debe ser mayor que 0"
            else -> null
        },
        fecha = when {
            datos.fecha.isBlank() -> "Elige la fecha del pago"
            !fechaEsValida(datos.fecha) -> "Elige una fecha válida"
            datos.fecha > hoy -> "La fecha del pago no puede ser futura"
            else -> null
        },
        comprobante = if (datos.adjuntarComprobante && datos.comprobante.isNullOrBlank())
            "Elige una imagen o desactiva el comprobante" else null,
        confirma = if (!datos.confirma) "Debes confirmar que es un registro informativo" else null
    )
