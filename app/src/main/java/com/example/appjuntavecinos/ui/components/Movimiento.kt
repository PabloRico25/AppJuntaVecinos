package com.example.appjuntavecinos.ui.components

import androidx.compose.animation.core.Animatable
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
