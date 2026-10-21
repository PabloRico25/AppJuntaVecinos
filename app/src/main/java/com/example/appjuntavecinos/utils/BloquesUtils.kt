package com.example.appjuntavecinos.utils

import com.example.appjuntavecinos.data.model.ReservaEntity

const val HORA_APERTURA = "09:00"
const val HORA_CIERRE = "22:00"

data class BloqueHorario(
    val horaInicio: String,
    val horaFin: String,
    val tipo: String,
    val vecinoId: Int? = null
)

fun calcularBloques(
    reservas: List<ReservaEntity>,
    dependenciaId: Int,
    fecha: String
): List<BloqueHorario> {
    val activas = reservas
        .filter { it.dependenciaId == dependenciaId && it.fecha == fecha }
        .filter { it.estado == "CONFIRMADA" || it.estado == "PENDIENTE" }
        .sortedBy { it.horaInicio }

    val bloques = mutableListOf<BloqueHorario>()
    var cursor = HORA_APERTURA
    for (reserva in activas) {
        if (reserva.horaInicio > cursor) {
            bloques.add(BloqueHorario(cursor, reserva.horaInicio, "LIBRE"))
        }
        if (reserva.horaFin > cursor) {
            cursor = reserva.horaFin
        }
        val tipo = if (reserva.estado == "CONFIRMADA") "CONFIRMADA" else "SOLICITADA"
        bloques.add(BloqueHorario(reserva.horaInicio, reserva.horaFin, tipo, reserva.vecinoId))
    }
    if (cursor < HORA_CIERRE) {
        bloques.add(BloqueHorario(cursor, HORA_CIERRE, "LIBRE"))
    }
    return bloques
}