package com.example.appjuntavecinos.utils

import com.example.appjuntavecinos.data.model.ReservaEntity

private const val MARCA = "JV1"
private const val CLAVE_FIRMA = "JuntaZaror-2026"

data class DatosQr(
    val reservaId: Int,
    val dependenciaId: Int,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val nombreVecino: String
)

data class DatosCodigoCorto(
    val reservaId: Int,
    val firma: String
)

enum class EstadoIngreso {
    VALIDO,
    FUERA_DE_HORARIO,
    NO_VALIDO
}

fun armarContenidoQr(reserva: ReservaEntity, nombreVecino: String): String {
    val cuerpo = listOf(
        MARCA,
        reserva.id.toString(),
        reserva.dependenciaId.toString(),
        reserva.fecha,
        reserva.horaInicio,
        reserva.horaFin,
        nombreVecino.replace("|", " ")
    ).joinToString("|")
    return cuerpo + "|" + firmar(cuerpo)
}

fun leerContenidoQr(texto: String): DatosQr? {
    val partes = texto.trim().split("|")
    if (partes.size != 8 || partes[0] != MARCA) return null

    val cuerpo = partes.take(7).joinToString("|")
    if (partes[7] != firmar(cuerpo)) return null

    val reservaId = partes[1].toIntOrNull() ?: return null
    val dependenciaId = partes[2].toIntOrNull() ?: return null
    return DatosQr(reservaId, dependenciaId, partes[3], partes[4], partes[5], partes[6])
}

fun armarCodigoCorto(reserva: ReservaEntity): String {
    return reserva.id.toString() + "-" + firmaCorta(reserva)
}

fun leerCodigoCorto(texto: String): DatosCodigoCorto? {
    val partes = texto.trim().uppercase().split("-")
    if (partes.size != 2) return null

    val reservaId = partes[0].toIntOrNull() ?: return null
    if (partes[1].length != 6) return null
    return DatosCodigoCorto(reservaId, partes[1])
}

fun codigoCortoEsValido(reserva: ReservaEntity, firma: String): Boolean {
    return firmaCorta(reserva) == firma
}

fun evaluarIngreso(datos: DatosQr, fechaHoy: String, horaActual: String): EstadoIngreso {
    if (datos.fecha != fechaHoy) return EstadoIngreso.FUERA_DE_HORARIO

    val inicio = aMinutos(datos.horaInicio)
    val ahora = aMinutos(horaActual)
    return if (ahora >= inicio - 10 && ahora <= inicio + 15) {
        EstadoIngreso.VALIDO
    } else {
        EstadoIngreso.FUERA_DE_HORARIO
    }
}

fun ventanaIngreso(horaInicio: String): String {
    val inicio = aMinutos(horaInicio)
    return "de " + aHora(inicio - 10) + " a " + aHora(inicio + 15)
}

private fun firmar(cuerpo: String): String {
    return hashClave(cuerpo + CLAVE_FIRMA).take(16)
}

private fun firmaCorta(reserva: ReservaEntity): String {
    val cuerpo = listOf(
        reserva.id.toString(),
        reserva.dependenciaId.toString(),
        reserva.fecha,
        reserva.horaInicio,
        reserva.horaFin
    ).joinToString("|")
    return hashClave(cuerpo + CLAVE_FIRMA).take(6).uppercase()
}

private fun aMinutos(hora: String): Int {
    val partes = hora.split(":")
    return partes[0].toInt() * 60 + partes[1].toInt()
}

private fun aHora(minutos: Int): String {
    return "%02d:%02d".format(minutos / 60, minutos % 60)
}