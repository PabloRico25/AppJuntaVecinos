package com.example.appjuntavecinos.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun fechaDeHoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
fun horaActual(): String = SimpleDateFormat("HH:mm", Locale.ROOT).format(Date())

fun fechaEn(dias: Int): String {
    val calendario = Calendar.getInstance()
    calendario.add(Calendar.DAY_OF_YEAR, dias)
    return SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendario.time)
}

// DatePicker entrega medianoche UTC: convertir con la zona del teléfono cambiaría el día.
fun fechaDesdeMillis(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(millis))

fun millisDesdeFecha(fecha: String): Long? = runCatching {
    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }.parse(fecha)?.time
}.getOrNull()

fun fechaEsValida(fecha: String): Boolean =
    Regex("\\d{4}-\\d{2}-\\d{2}").matches(fecha) && millisDesdeFecha(fecha) != null

fun fechaParaMostrar(fecha: String): String =
    if (fechaEsValida(fecha)) "${fecha.substring(8, 10)}/${fecha.substring(5, 7)}/${fecha.take(4)}" else fecha

fun mesAnterior(mes: String, cambio: Int): String {
    val formato = SimpleDateFormat("yyyy-MM", Locale.ROOT)
    val calendario = Calendar.getInstance()
    calendario.time = formato.parse(mes) ?: Date()
    calendario.add(Calendar.MONTH, cambio)
    return formato.format(calendario.time)
}

fun mesParaMostrar(mes: String): String {
    val fecha = SimpleDateFormat("yyyy-MM", Locale.ROOT).parse(mes) ?: return mes
    return SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-CL")).format(fecha)
}
