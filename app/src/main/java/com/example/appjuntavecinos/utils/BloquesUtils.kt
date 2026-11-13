package com.example.appjuntavecinos.utils

import com.example.appjuntavecinos.data.model.BloqueDto
import com.example.appjuntavecinos.data.model.ReservaEntity

const val HORA_APERTURA = "09:00"
const val HORA_CIERRE = "22:00"

data class BloqueHorario(
    val horaInicio: String,
    val horaFin: String,
    val tipo: String,
    val vecinoId: Int? = null
)

// Con las reservas guardadas en Room (la directiva las tiene todas; un vecino solo las suyas).
fun calcularBloques(
    reservas: List<ReservaEntity>,
    dependenciaId: Int,
    fecha: String
): List<BloqueHorario> {
    val ocupados = reservas
        .filter { it.dependenciaId == dependenciaId && it.fecha == fecha }
        .map { BloqueDto(it.horaInicio, it.horaFin, it.estado, it.vecinoId) }
    return calcularBloques(ocupados)
}

// Con los bloques ocupados que entrega el servidor para una sala y un día. El servidor ve las reservas
// de todos, por eso es la fuente correcta para la disponibilidad.
fun calcularBloques(ocupados: List<BloqueDto>): List<BloqueHorario> {
    val activos = ocupados
        .filter { it.estado == "CONFIRMADA" || it.estado == "PENDIENTE" }
        .sortedBy { it.horaInicio }

    val bloques = mutableListOf<BloqueHorario>()
    var cursor = HORA_APERTURA
    for (ocupado in activos) {
        if (ocupado.horaInicio > cursor) {
            bloques.add(BloqueHorario(cursor, ocupado.horaInicio, "LIBRE"))
        }
        if (ocupado.horaFin > cursor) {
            cursor = ocupado.horaFin
        }
        val tipo = if (ocupado.estado == "CONFIRMADA") "CONFIRMADA" else "SOLICITADA"
        bloques.add(BloqueHorario(ocupado.horaInicio, ocupado.horaFin, tipo, ocupado.vecinoId))
    }
    if (cursor < HORA_CIERRE) {
        bloques.add(BloqueHorario(cursor, HORA_CIERRE, "LIBRE"))
    }
    return bloques
}