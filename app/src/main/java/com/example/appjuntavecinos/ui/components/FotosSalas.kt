package com.example.appjuntavecinos.ui.components

import androidx.annotation.DrawableRes
import com.example.appjuntavecinos.R

// La foto de cada sala, por su nombre. Si aparece una sala nueva sin foto, la tarjeta queda con fondo liso.
@DrawableRes
fun fotoDeSala(nombre: String): Int? = when {
    nombre.contains("Principal", ignoreCase = true) -> R.drawable.foto_salon_principal
    nombre.contains("Multiuso", ignoreCase = true) -> R.drawable.foto_sala_multiuso
    else -> null
}
