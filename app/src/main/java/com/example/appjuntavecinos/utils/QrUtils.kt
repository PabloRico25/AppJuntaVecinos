package com.example.appjuntavecinos.utils

import com.example.appjuntavecinos.data.model.ReservaEntity
import java.util.Locale

private const val CLAVE_FIRMA = "JuntaZaror-2026"

data class DatosQr(
    val reservaId: Int, val dependenciaId: Int, val fecha: String,
    val horaInicio: String, val horaFin: String, val nombreVecino: String
)
data class DatosCodigoCorto(val reservaId: Int, val firma: String)
enum class EstadoIngreso { VALIDO, FUERA_DE_HORARIO, NO_VALIDO }

fun armarContenidoQr(reserva: ReservaEntity, nombreVecino: String): String {
    val cuerpo = listOf("JV1", reserva.id.toString(), reserva.dependenciaId.toString(), reserva.fecha,
        reserva.horaInicio, reserva.horaFin, nombreVecino.replace("|", " ").replace("\n", " ")).joinToString("|")
    return "$cuerpo|${hashClave(cuerpo + CLAVE_FIRMA).take(16)}"
}

fun minutosDeHora(hora: String): Int? {
    if (!Regex("([01]\\d|2[0-3]):[0-5]\\d").matches(hora)) return null
    return hora.take(2).toInt() * 60 + hora.takeLast(2).toInt()
}

fun leerContenidoQr(texto: String): DatosQr? {
    if (texto.length > 2000) return null
    val partes = texto.trim().split("|")
    if (partes.size != 8 || partes[0] != "JV1") return null
    val firma = hashClave(partes.take(7).joinToString("|") + CLAVE_FIRMA).take(16)
    if (partes[7] != firma) return null
    val reservaId = partes[1].toIntOrNull()?.takeIf { it > 0 } ?: return null
    val salaId = partes[2].toIntOrNull()?.takeIf { it > 0 } ?: return null
    val inicio = minutosDeHora(partes[4]) ?: return null
    val fin = minutosDeHora(partes[5]) ?: return null
    if (!fechaEsValida(partes[3]) || inicio >= fin || inicio < 540 || fin > 1320 || partes[6].isBlank()) return null
    return DatosQr(reservaId, salaId, partes[3], partes[4], partes[5], partes[6])
}

fun evaluarIngreso(datos: DatosQr, fechaHoy: String, horaActual: String): EstadoIngreso {
    val inicio = minutosDeHora(datos.horaInicio) ?: return EstadoIngreso.NO_VALIDO
    val ahora = minutosDeHora(horaActual) ?: return EstadoIngreso.NO_VALIDO
    if (!fechaEsValida(datos.fecha)) return EstadoIngreso.NO_VALIDO
    return if (datos.fecha == fechaHoy && ahora in (inicio - 10)..(inicio + 15)) EstadoIngreso.VALIDO
    else EstadoIngreso.FUERA_DE_HORARIO
}

private fun firmaCorta(reserva: ReservaEntity): String =
    hashClave(listOf(reserva.id, reserva.dependenciaId, reserva.fecha, reserva.horaInicio, reserva.horaFin)
        .joinToString("|") + CLAVE_FIRMA).take(6).uppercase(Locale.ROOT)

fun armarCodigoCorto(reserva: ReservaEntity): String = "${reserva.id}-${firmaCorta(reserva)}"
fun codigoCortoEsValido(reserva: ReservaEntity, firma: String): Boolean = firmaCorta(reserva) == firma.uppercase(Locale.ROOT)
fun leerCodigoCorto(texto: String): DatosCodigoCorto? {
    val partes = texto.trim().uppercase(Locale.ROOT).split("-")
    if (partes.size != 2 || !Regex("[0-9A-F]{6}").matches(partes[1])) return null
    return DatosCodigoCorto(partes[0].toIntOrNull()?.takeIf { it > 0 } ?: return null, partes[1])
}

fun ventanaIngreso(datos: DatosQr): String {
    val inicio = minutosDeHora(datos.horaInicio) ?: return ""
    fun formato(minutos: Int) = String.format(Locale.ROOT, "%02d:%02d", minutos / 60, minutos % 60)
    return "${formato(inicio - 10)} a ${formato(inicio + 15)}"
}
