package com.example.appjuntavecinos.data.model

// El vecino, el estado y la fecha de solicitud los completa el servidor.
data class NuevaReservaRequest(
    val dependenciaId: Int,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String
)