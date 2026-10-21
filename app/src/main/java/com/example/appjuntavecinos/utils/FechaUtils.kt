package com.example.appjuntavecinos.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun fechaDeHoy(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

fun horaActual(): String {
    return SimpleDateFormat("HH:mm", Locale.US).format(Date())
}

fun fechaDesdeMillis(millis: Long): String {
    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    formato.timeZone = TimeZone.getTimeZone("UTC")
    return formato.format(Date(millis))
}

fun sumarMeses(mes: String, cambio: Int): String {
    val formato = SimpleDateFormat("yyyy-MM", Locale.US)
    val calendario = Calendar.getInstance()
    calendario.time = formato.parse(mes)!!
    calendario.add(Calendar.MONTH, cambio)
    return formato.format(calendario.time)
}

fun nombreDelMes(mes: String): String {
    val primerDia = SimpleDateFormat("yyyy-MM", Locale.US).parse(mes)!!
    return SimpleDateFormat("MMMM yyyy", Locale("es", "CL"))
        .format(primerDia)
        .replaceFirstChar { it.uppercase() }
}

fun fechaParaMostrar(fecha: String): String {
    val partes = fecha.split("-")
    return if (partes.size == 3) "${partes[2]}-${partes[1]}-${partes[0]}" else fecha
}