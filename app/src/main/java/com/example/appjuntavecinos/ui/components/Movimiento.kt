package com.example.appjuntavecinos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.utils.CurvaJunta
import com.example.appjuntavecinos.utils.DURACION_NORMAL_MS
import com.example.appjuntavecinos.utils.debeAparecerEscalonado
import com.example.appjuntavecinos.utils.desplazamientoSacudida
import com.example.appjuntavecinos.utils.retrasoEscalonado
import kotlinx.coroutines.delay

// La tarjeta aparece subiendo 12 dp con un fundido, y cada una un poco después que la anterior.
// Solo las primeras de la lista; sin animaciones (ajuste del teléfono) aparece directo.
@Composable
fun Modifier.aparecerEscalonado(indice: Int): Modifier {
    if (!animacionesActivas() || !debeAparecerEscalonado(indice)) return this
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(retrasoEscalonado(indice))
        progreso.animateTo(1f, tween(DURACION_NORMAL_MS, easing = CurvaJunta))
    }
    val subida = with(LocalDensity.current) { 12.dp.toPx() }
    return this.graphicsLayer {
        alpha = progreso.value
        translationY = (1f - progreso.value) * subida
    }
}

// No visto en clase: graphicsLayer mueve un elemento (translationX) sin volver a medir la pantalla.
// Un campo que acaba de quedar con error se sacude una vez, 6 dp a cada lado en 320 ms (kit de diseño: "Campo con
// error"). La clave es el mensaje: cuando aparece o cambia, corre de nuevo. Sin error no se mueve, y con
// "Reducir animaciones" tampoco.
@Composable
fun Modifier.sacudirSiHayError(mensaje: String?): Modifier {
    val animar = animacionesActivas()
    val avance = remember { Animatable(1f) }
    LaunchedEffect(mensaje) {
        if (mensaje != null && animar) {
            avance.snapTo(0f)
            avance.animateTo(1f, tween(DURACION_NORMAL_MS, easing = LinearEasing))
        }
    }
    val densidad = LocalDensity.current.density
    return this.graphicsLayer {
        translationX = desplazamientoSacudida(avance.value, listOf(0f, -6f, 6f, -4f, 3f, 0f)) * densidad
    }
}
