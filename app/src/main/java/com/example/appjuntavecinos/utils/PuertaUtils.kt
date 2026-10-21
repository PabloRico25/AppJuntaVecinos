package com.example.appjuntavecinos.utils

enum class EstadoPuerta {
    ABIERTA,
    CERRADA
}

data class ResultadoPuerta(val estado: EstadoPuerta,val mensaje: String,val reservaId: Int? = null)

fun evaluarPuerta(texto: String,salaDeLaPuerta:Int,fechaHoy:String,horaActual:String,
                  reservasUsadas:Set<Int>):ResultadoPuerta {val datos = leerContenidoQr(texto)?:
                  return ResultadoPuerta(EstadoPuerta.CERRADA,"Código no válido")

    if (datos.dependenciaId != salaDeLaPuerta) {
        return ResultadoPuerta(EstadoPuerta.CERRADA, "Este código es de otra sala")
    }

    if (evaluarIngreso(datos, fechaHoy, horaActual) != EstadoIngreso.VALIDO) {
        return ResultadoPuerta(EstadoPuerta.CERRADA, "Fuera del horario de la reserva")
    }

    if (datos.reservaId in reservasUsadas) {
        return ResultadoPuerta(EstadoPuerta.CERRADA, "Este código ya se usó")
    }

    return ResultadoPuerta(EstadoPuerta.ABIERTA,"Bienvenido/a, " + datos.nombreVecino,
        datos.reservaId
    )
}