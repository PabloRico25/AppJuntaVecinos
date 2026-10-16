package com.example.appjuntavecinos.utils

import java.text.SimpleDateFormat
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