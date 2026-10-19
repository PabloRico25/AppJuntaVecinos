package com.example.appjuntavecinos.utils

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun anchoDeVentana(): WindowWidthSizeClass {
    val actividad = LocalActivity.current ?: return WindowWidthSizeClass.Compact
    return calculateWindowSizeClass(actividad).widthSizeClass
}