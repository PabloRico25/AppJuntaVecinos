package com.example.appjuntavecinos.data.model

data class NuevaReservaErrores(
    val dependencia: String? = null,
    val fecha: String? = null,
    val horaInicio: String? = null,
    val horaFin: String? = null,
    val reglamento: String? = null,
    val general: String? = null
)

data class NuevaReservaUiState(
    val dependencia: DependenciaComunitariaEntity? = null,
    val fecha: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val aceptaReglamento: Boolean = false,
    val errores: NuevaReservaErrores = NuevaReservaErrores(),
    val guardando: Boolean = false
)
