package com.example.appjuntavecinos.utils

// El servidor guarda el cargo en mayúsculas y sin tilde (SECRETARIA, TESORERIA). Para mostrarlo en la cabecera
// se escribe como en el kit de diseño. Un cargo vacío es un vecino. Un cargo desconocido se muestra con inicial mayúscula.
fun nombreDelCargo(cargo: String?): String {
    return when (cargo?.trim()?.uppercase()) {
        null, "" -> "Vecino"
        "SECRETARIA" -> "Secretaría"
        "TESORERIA" -> "Tesorería"
        "PRESIDENCIA", "PRESIDENTE", "PRESIDENTA" -> "Presidencia"
        else -> cargo.trim().lowercase().replaceFirstChar { it.uppercase() }
    }
}
