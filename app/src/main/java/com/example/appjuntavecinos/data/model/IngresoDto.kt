package com.example.appjuntavecinos.data.model

// El ingreso que se registra en el servidor cuando la puerta se abre: qué reserva entró, y cuándo
data class IngresoDto(
    val reservaId: Int,
    val fecha: String,
    val hora: String
)