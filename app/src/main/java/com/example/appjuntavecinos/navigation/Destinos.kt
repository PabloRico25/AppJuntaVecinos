package com.example.appjuntavecinos.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class Destino(val ruta: String, val titulo: String, val icono: ImageVector)

val destinosVecino = listOf(
    Destino(Rutas.MIS_RESERVAS, "Mis reservas", Icons.Default.Home),
    Destino(Rutas.DISPONIBILIDAD, "Disponibilidad", Icons.Default.DateRange),
    Destino(Rutas.FINANZAS, "Finanzas", Icons.Default.Info)
)
val destinosDirectiva = listOf(
    Destino(Rutas.RESERVAS, "Reservas", Icons.Default.Home),
    Destino(Rutas.PAGOS, "Pagos", Icons.AutoMirrored.Filled.List),
    Destino(Rutas.RESUMEN, "Resumen", Icons.Default.Info),
    Destino(Rutas.ESCANEAR, "Escanear", Icons.Default.Check)
)
