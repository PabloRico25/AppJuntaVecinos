package com.example.appjuntavecinos.ui.components

import androidx.annotation.DrawableRes
import com.example.appjuntavecinos.R

// Los gastos que muestra Finanzas son consumos básicos. Cada categoría lleva el ícono del kit de diseño.
// Una categoría que el kit no trae usa el ícono genérico de "otro gasto".
enum class CategoriaDeGasto(@DrawableRes val icono: Int) {
    AGUA(R.drawable.ic_gasto_agua),
    ELECTRICIDAD(R.drawable.ic_gasto_electricidad),
    GAS(R.drawable.ic_gasto_gas),
    OTRO(R.drawable.ic_gasto_otro)
}

fun categoriaDeGasto(nombre: String): CategoriaDeGasto =
    CategoriaDeGasto.entries.firstOrNull { it.name.equals(nombre.trim(), ignoreCase = true) } ?: CategoriaDeGasto.OTRO
