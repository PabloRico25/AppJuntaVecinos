package com.example.appjuntavecinos.utils

enum class EstadoPuerta {
    ABIERTA,
    CERRADA
}

// Por qué la puerta abre o no abre. La pantalla elige con esto el ícono y el texto, para que el estado no se
// entienda solo por el color.
enum class MotivoPuerta {
    ABIERTA,
    YA_USADO,
    OTRA_SALA,
    FUERA_DE_HORARIO,
    NO_VALIDO,
    SIN_CONEXION,
    ERROR_SERVIDOR
}

data class ResultadoPuerta(
    val estado: EstadoPuerta,
    val mensaje: String,
    val reservaId: Int? = null,
    val motivo: MotivoPuerta = if (estado == EstadoPuerta.ABIERTA) MotivoPuerta.ABIERTA else MotivoPuerta.NO_VALIDO
)

fun evaluarPuerta(texto: String,salaDeLaPuerta:Int,fechaHoy:String,horaActual:String,
                  reservasUsadas:Set<Int>):ResultadoPuerta {val datos = leerContenidoQr(texto)?:
                  return ResultadoPuerta(EstadoPuerta.CERRADA,"Código no válido", motivo = MotivoPuerta.NO_VALIDO)

    if (datos.dependenciaId != salaDeLaPuerta) {
        return ResultadoPuerta(EstadoPuerta.CERRADA, "Este código es de otra sala", motivo = MotivoPuerta.OTRA_SALA)
    }

    if (evaluarIngreso(datos, fechaHoy, horaActual) != EstadoIngreso.VALIDO) {
        return ResultadoPuerta(EstadoPuerta.CERRADA, "Fuera del horario de la reserva", motivo = MotivoPuerta.FUERA_DE_HORARIO)
    }

    if (datos.reservaId in reservasUsadas) {
        return ResultadoPuerta(EstadoPuerta.CERRADA, "Este código ya se usó", motivo = MotivoPuerta.YA_USADO)
    }

    return ResultadoPuerta(EstadoPuerta.ABIERTA,"Bienvenido/a, " + datos.nombreVecino,
        datos.reservaId
    )
}