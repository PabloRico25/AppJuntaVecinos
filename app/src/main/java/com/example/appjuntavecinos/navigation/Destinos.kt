package com.example.appjuntavecinos.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

data class Destino(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector
)

val destinosVecino = listOf(
    Destino(Rutas.MIS_RESERVAS, "Mis reservas", Icons.Filled.DateRange)
)

val destinosDirectiva = listOf(
    Destino(Rutas.RESERVAS, "Reservas", Icons.Filled.DateRange),
    Destino(Rutas.PAGOS, "Pagos", Icons.Filled.ShoppingCart)
)