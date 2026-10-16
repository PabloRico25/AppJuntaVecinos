package com.example.appjuntavecinos.viewmodel

import com.example.appjuntavecinos.data.model.NuevaReservaErrores
import com.example.appjuntavecinos.data.model.NuevaReservaUiState
import com.example.appjuntavecinos.utils.fechaEsValida

// Función de lógica usada por validarFormulario; no depende de Compose.
fun validarDatosReserva(estado: NuevaReservaUiState, hoy: String): NuevaReservaErrores {
    val formatoHora = Regex("([01]\\d|2[0-3]):[0-5]\\d")
    val inicioValido = formatoHora.matches(estado.horaInicio)
    val finValido = formatoHora.matches(estado.horaFin)
    return NuevaReservaErrores(
        dependencia = if (estado.dependencia == null) "Elige una dependencia" else null,
        fecha = when {
            estado.fecha.isBlank() -> "Elige una fecha"
            !fechaEsValida(estado.fecha) -> "Elige una fecha válida"
            estado.fecha < hoy -> "La fecha no puede ser pasada"
            else -> null
        },
        horaInicio = when {
            !inicioValido -> "Elige la hora de inicio"
            estado.horaInicio < "09:00" || estado.horaInicio >= "22:00" -> "Fuera del horario de la sede (09:00 a 22:00)"
            else -> null
        },
        horaFin = when {
            !finValido -> "Elige la hora de fin"
            estado.horaFin > "22:00" || estado.horaFin <= "09:00" -> "Fuera del horario de la sede (09:00 a 22:00)"
            inicioValido && estado.horaFin <= estado.horaInicio -> "La hora de fin debe ser mayor que la de inicio"
            else -> null
        },
        reglamento = if (!estado.aceptaReglamento) "Debes aceptar el reglamento de la sede" else null
    )
}
