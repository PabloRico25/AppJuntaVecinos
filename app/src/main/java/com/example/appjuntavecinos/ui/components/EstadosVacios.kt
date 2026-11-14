package com.example.appjuntavecinos.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Una ilustración del kit de diseño (05_ilustraciones). Todas miden 240 x 170, y llevan una versión para el modo oscuro.
// Es decorativa: el título y el mensaje que la acompañan dicen lo mismo con palabras.
@Composable
fun IlustracionDeKit(@DrawableRes recurso: Int, modifier: Modifier = Modifier, alto: Dp = 150.dp) {
    Image(
        painter = painterResource(recurso),
        contentDescription = null,
        modifier = modifier.size(width = alto * (240f / 170f), height = alto)
    )
}

// Un estado vacío: la ilustración, un título y, si hace falta, una línea que dice qué hacer.
@Composable
fun EstadoVacio(
    @DrawableRes ilustracion: Int,
    titulo: String,
    modifier: Modifier = Modifier,
    mensaje: String? = null
) {
    EstadoCompleto(
        icono = { IlustracionDeKit(ilustracion) },
        titulo = titulo,
        modifier = modifier,
        mensaje = mensaje,
        circulo = false
    )
}
