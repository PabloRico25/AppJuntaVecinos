package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.appjuntavecinos.ui.components.EstadoDeError

// Lo que ve la persona cuando el servidor rechaza su sesión: se le explica y se le lleva al login.
@Composable
fun SesionVencidaScreen(onIniciarSesion: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        EstadoDeError(
            mensaje = "Tu sesión venció. Inicia sesión de nuevo",
            textoBoton = "Iniciar sesión",
            iconoBoton = null,
            onReintentar = onIniciarSesion
        )
    }
}
