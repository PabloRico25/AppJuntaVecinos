package com.example.appjuntavecinos.data.model

// Un tramo de horario ocupado en una sala. El vecinoId solo llega si quien consulta es de la directiva.
data class BloqueDto(
    val horaInicio: String,
    val horaFin: String,
    val estado: String,
    val vecinoId: Int?
)