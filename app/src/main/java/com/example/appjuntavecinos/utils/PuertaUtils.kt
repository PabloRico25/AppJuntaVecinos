package com.example.appjuntavecinos.utils

data class ResultadoPuerta(val estado: String, val mensaje: String, val datos: DatosQr? = null)

fun evaluarPuerta(texto: String, salaDeLaPuerta: Int, fechaHoy: String, horaActual: String,
                  reservasUsadas: List<Int>): ResultadoPuerta {
    val datos = leerContenidoQr(texto) ?: return ResultadoPuerta("CERRADA", "Ticket no válido")
    if (datos.dependenciaId != salaDeLaPuerta) return ResultadoPuerta("CERRADA", "El ticket es de otra sala", datos)
    if (evaluarIngreso(datos, fechaHoy, horaActual) != EstadoIngreso.VALIDO)
        return ResultadoPuerta("CERRADA", "Fuera de horario", datos)
    if (datos.reservaId in reservasUsadas) return ResultadoPuerta("CERRADA", "Este ticket ya fue utilizado", datos)
    return ResultadoPuerta("ABIERTA", "Puerta abierta", datos)
}
