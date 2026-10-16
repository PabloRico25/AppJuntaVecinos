package com.example.appjuntavecinos.data.model

import com.example.appjuntavecinos.utils.fechaDeHoy

data class FranjaDisponibilidad(
    val inicio: String,
    val fin: String,
    val estado: String,
    val reservas: List<ReservaEntity> = emptyList()
)

data class DisponibilidadUiState(
    val dependencia: DependenciaComunitariaEntity? = null,
    val fecha: String = fechaDeHoy(),
    val franjas: List<FranjaDisponibilidad> = emptyList(),
    val error: String? = null
)
