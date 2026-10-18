package com.example.appjuntavecinos.utils

import com.example.appjuntavecinos.data.model.ReservaEntity

const val HORA_APERTURA = "09:00"
const val HORA_CIERRE = "22:00"

data class Franja(
    val horaInicio: String,
    val horaFin: String,
    val tipo: String,
    val vecinoId: Int? = null
)

fun calcularFranjas(
    reservas: List<ReservaEntity>,
    dependenciaId: Int,
    fecha: String
): List<Franja> {
    val activas = reservas
        .filter { it.dependenciaId == dependenciaId && it.fecha == fecha }
        .filter { it.estado == "CONFIRMADA" || it.estado == "PENDIENTE" }
        .sortedBy { it.horaInicio }

    val franjas = mutableListOf<Franja>()
    var cursor = HORA_APERTURA
    for (reserva in activas) {
        if (reserva.horaInicio > cursor) {
            franjas.add(Franja(cursor, reserva.horaInicio, "LIBRE"))
        }
        if (reserva.horaFin > cursor) {
            cursor = reserva.horaFin
        }
        val tipo = if (reserva.estado == "CONFIRMADA") "CONFIRMADA" else "SOLICITADA"
        franjas.add(Franja(reserva.horaInicio, reserva.horaFin, tipo, reserva.vecinoId))
    }
    if (cursor < HORA_CIERRE) {
        franjas.add(Franja(cursor, HORA_CIERRE, "LIBRE"))
    }
    return franjas
}