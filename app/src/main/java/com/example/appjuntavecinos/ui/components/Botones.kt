package com.example.appjuntavecinos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.utils.CurvaJunta

// Material pinta por defecto el texto de estos dos botones con el color primario, que aquí es naranja:
// el naranja sobre el fondo crema no se lee (contraste 2,4 a 1). Por eso la app usa estas dos versiones,
// con el texto en el color normal y el borde en el color de los campos. Aceptan lo mismo que los originales.

@Composable
fun BotonContorno(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val borde = MaterialTheme.colorScheme.outline
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        border = BorderStroke(1.dp, if (enabled) borde else borde.copy(alpha = 0.38f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        content = content
    )
}

@Composable
fun BotonTexto(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        content = content
    )
}

// No visto en clase: MutableInteractionSource y collectIsPressedAsState dicen cuándo un botón está presionado, y graphicsLayer
// lo escala sin volver a medir la pantalla.
// Mientras se mantiene presionado, un botón se achica a 0,97 y vuelve al soltarlo, en 120 ms (kit de diseño: "Botón
// principal al presionar"). Con "Reducir animaciones" no se mueve. Devuelve la fuente de eventos y la escala.
@Composable
private fun escalaAlPresionar(): Pair<MutableInteractionSource, State<Float>> {
    val fuente = remember { MutableInteractionSource() }
    val presionado by fuente.collectIsPressedAsState()
    val animar = animacionesActivas()
    val escala = animateFloatAsState(
        targetValue = if (presionado && animar) 0.97f else 1f,
        animationSpec = tween(120, easing = CurvaJunta),
        label = "escalaBoton"
    )
    return fuente to escala
}

// El botón principal de la app: un Button normal que se achica al presionarlo.
@Composable
fun BotonJunta(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable RowScope.() -> Unit
) {
    val (fuente, escala) = escalaAlPresionar()
    Button(
        onClick = onClick,
        modifier = modifier.graphicsLayer { scaleX = escala.value; scaleY = escala.value },
        enabled = enabled,
        colors = colors,
        interactionSource = fuente,
        content = content
    )
}
