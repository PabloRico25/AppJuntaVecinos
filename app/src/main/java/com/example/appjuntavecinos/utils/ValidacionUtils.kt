package com.example.appjuntavecinos.utils

// Un correo tiene la forma nombre@dominio.extensión. Está en utils/ y no en el ViewModel para poder
// probarlo sin Android.
private val FORMA_DE_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

fun correoEsValido(correo: String): Boolean {
    return FORMA_DE_CORREO.matches(correo)
}
