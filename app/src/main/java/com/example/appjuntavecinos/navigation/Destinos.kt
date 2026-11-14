package com.example.appjuntavecinos.navigation

import androidx.annotation.DrawableRes
import com.example.appjuntavecinos.R

data class Destino(
    val ruta: String,
    val etiqueta: String,
    @param:DrawableRes val icono: Int
)

val destinosVecino = listOf(
    Destino(Rutas.MIS_RESERVAS, "Mis reservas", R.drawable.ic_nav_calendario),
    Destino(Rutas.DISPONIBILIDAD, "Disponibilidad", R.drawable.ic_reloj),
    Destino(Rutas.FINANZAS, "Finanzas", R.drawable.ic_nav_barras)
)

val destinosDirectiva = listOf(
    Destino(Rutas.RESERVAS, "Reservas", R.drawable.ic_nav_calendario),
    Destino(Rutas.PAGOS, "Pagos", R.drawable.ic_nav_pagos),
    Destino(Rutas.RESUMEN, "Resumen", R.drawable.ic_nav_barras),
    Destino(Rutas.ESCANEAR, "Escanear", R.drawable.ic_nav_escaner)
)
