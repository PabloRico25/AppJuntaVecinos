package com.example.appjuntavecinos.utils

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable

// Guía 9: Window Size Classes. Compact (menos de 600 dp), Medium (600 a 840) y Expanded
// (más de 840 dp). calculateWindowSizeClass necesita la Activity de la pantalla.
// LocalActivity la entrega ya resuelta (más seguro que convertir el Context a mano),
// y se llama una vez por cada pantalla que se quiera adaptar.
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun anchoDeVentana(): WindowWidthSizeClass {
    val actividad = LocalActivity.current ?: return WindowWidthSizeClass.Compact
    return calculateWindowSizeClass(actividad).widthSizeClass
}