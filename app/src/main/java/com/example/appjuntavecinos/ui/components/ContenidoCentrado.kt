package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp


// Limita el ancho del contenido y lo centra, para que en pantallas grandes los formularios no se estiren.
// No visto en clase: widthIn pone un ancho máximo al contenido.
@Composable
fun ContenidoCentrado(
    anchoMaximo: Dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(modifier = Modifier.fillMaxHeight().widthIn(max = anchoMaximo)) {
            content()
        }
    }
}