package com.example.appjuntavecinos.utils

// Los mensajes de error del servidor y de la app vienen en una sola frase, a veces con dos partes
// ("No se pudo conectar con el servidor. Revisa que esté encendido"). Los estados completos las muestran
// separadas: la primera como título y la segunda como explicación.
fun partirMensaje(mensaje: String): Pair<String, String?> {
    val corte = mensaje.indexOf(". ")
    if (corte <= 0) return mensaje to null
    return mensaje.substring(0, corte) to mensaje.substring(corte + 2)
}
