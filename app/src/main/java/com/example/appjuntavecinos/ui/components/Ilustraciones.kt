package com.example.appjuntavecinos.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Animatable
import com.example.appjuntavecinos.utils.DURACION_SOLICITUD_MS
import com.example.appjuntavecinos.utils.estadoSinConexion
import com.example.appjuntavecinos.utils.estadoSolicitudEnviada

// No visto en clase: Canvas dibuja formas por código y PathParser convierte un trazo escrito como texto (el del SVG) en una figura.
// Dos ilustraciones del kit de diseño (sin_conexion y solicitud_enviada) dibujadas con Canvas, sin librerías
// extra. Todo está en una escala de 240 x 240 y se amplía al tamaño pedido. Los tiempos de cada pieza están en
// MovimientoUtils.kt. Con "Reducir animaciones" se ve el cuadro final, quieto.

private fun trazo(d: String): Path = PathParser().parsePathString(d).toPath()

private class Paleta(
    val tinta: Color, val cuerpo: Color, val cabecera: Color, val celda: Color,
    val azul: Color, val bordeAzul: Color, val raya: Color, val halo: Color, val punto: Color
)

private val PaletaClara = Paleta(
    tinta = Color(0xFF3A2A1A), cuerpo = Color(0xFFFFFFFF), cabecera = Color(0xFFFF7900), celda = Color(0xFFFFE2C2),
    azul = Color(0xFF5A91C7), bordeAzul = Color(0xFFFFF9F2), raya = Color(0xFFC95745), halo = Color(0xFFFFF9F2),
    punto = Color(0xFFE8D7C2)
)

private val PaletaOscura = Paleta(
    tinta = Color(0xFFF6E9D8), cuerpo = Color(0xFF241A13), cabecera = Color(0xFFFF8A1F), celda = Color(0xFF4A3020),
    azul = Color(0xFF9CC6F0), bordeAzul = Color(0xFF17110C), raya = Color(0xFFFF8F7F), halo = Color(0xFF17110C),
    punto = Color(0xFF4A3A2D)
)

@Composable
private fun paleta(): Paleta = if (isSystemInDarkTheme()) PaletaOscura else PaletaClara

// El tiempo, en milisegundos, que lleva una animación. En bucle no se detiene; sin animaciones queda fijo.
@Composable
private fun tiempoEnBucle(animar: Boolean): State<Float> {
    if (!animar) return remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    return rememberInfiniteTransition(label = "tiempo").animateFloat(
        initialValue = 0f,
        targetValue = 3_600_000f,
        animationSpec = infiniteRepeatable(tween(3_600_000, easing = LinearEasing), RepeatMode.Restart),
        label = "tiempo"
    )
}

@Composable
private fun tiempoUnaVez(animar: Boolean, duracionMs: Float): State<Float> {
    val tiempo = remember { Animatable(if (animar) 0f else duracionMs) }
    LaunchedEffect(Unit) {
        if (animar) tiempo.animateTo(duracionMs, tween(duracionMs.toInt(), easing = LinearEasing))
    }
    return tiempo.asState()
}

@Composable
fun IlustracionSinConexion(modifier: Modifier = Modifier, tamano: Dp = 170.dp) {
    val p = paleta()
    val animar = animacionesActivas()
    val tiempo = tiempoEnBucle(animar)
    val nube = remember { trazo("M72 164h100a36 36 0 0 0 6-71.5 50 50 0 0 0-95-12A42 42 0 0 0 72 164z") }

    Canvas(modifier = modifier.size(tamano)) {
        val e = estadoSinConexion(tiempo.value, animar)
        scale(size.width / 240f, size.width / 240f, pivot = Offset.Zero) {
            // La nube sube con un fundido y después "respira"
            translate(top = e.subidaNube) {
                scale(e.escalaNube, e.escalaNube, pivot = Offset(125f, 122f)) {
                    drawPath(nube, p.cuerpo.copy(alpha = e.opacidadNube))
                    drawPath(nube, p.tinta.copy(alpha = e.opacidadNube), style = Stroke(5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
            // La raya se dibuja de abajo a arriba, con un halo del color del fondo para separarla de la nube
            val inicio = Offset(62f, 186f)
            val fin = Offset(62f + 120f * e.progresoRaya, 186f - 134f * e.progresoRaya)
            if (e.progresoRaya > 0f) {
                drawLine(p.halo, inicio, fin, strokeWidth = 18f, cap = StrokeCap.Round)
                drawLine(p.raya, inicio, fin, strokeWidth = 8f, cap = StrokeCap.Round)
            }
            listOf(96f, 120f, 144f).forEachIndexed { i, x ->
                drawCircle(p.punto.copy(alpha = e.opacidadPuntos[i]), radius = 6f, center = Offset(x, 204f))
            }
        }
    }
}

private fun DrawScope.celdas(color: Color) {
    for (x in listOf(50f, 82f, 114f, 146f, 178f)) {
        for (y in listOf(104f, 132f, 160f)) {
            drawRoundRect(color, Offset(x, y), Size(16f, 14f), CornerRadius(4f))
        }
    }
}

@Composable
fun IlustracionSolicitudEnviada(modifier: Modifier = Modifier, tamano: Dp = 190.dp) {
    val p = paleta()
    val animar = animacionesActivas()
    val tiempo = tiempoUnaVez(animar, DURACION_SOLICITUD_MS)
    val cabecera = remember { trazo("M34 72a22 22 0 0 1 22-22h128a22 22 0 0 1 22 22v18H34z") }
    val estela = remember { trazo("M30 214C70 190 100 130 122 112s40 30 54 62") }
    val avion = remember { trazo("M-22 -4 22 -18 6 22 0 6z") }
    val ala = remember { trazo("M22 -18 0 6") }
    val manecilla = remember { trazo("M0 -14V0l10 6") }
    val redondo = StrokeCap.Round

    Canvas(modifier = modifier.size(tamano)) {
        val e = estadoSolicitudEnviada(tiempo.value, animar)
        scale(size.width / 240f, size.width / 240f, pivot = Offset.Zero) {
            translate(top = e.subidaCalendario) {
                val a = e.opacidadCalendario
                drawRoundRect(p.cuerpo.copy(alpha = a), Offset(34f, 50f), Size(172f, 150f), CornerRadius(22f))
                drawRoundRect(p.tinta.copy(alpha = a), Offset(34f, 50f), Size(172f, 150f), CornerRadius(22f), style = Stroke(4f, cap = redondo, join = StrokeJoin.Round))
                drawPath(cabecera, p.cabecera.copy(alpha = a))
                drawPath(cabecera, p.tinta.copy(alpha = a), style = Stroke(4f, cap = redondo, join = StrokeJoin.Round))
                drawLine(p.tinta.copy(alpha = a), Offset(82f, 44f), Offset(82f, 62f), strokeWidth = 6f, cap = redondo)
                drawLine(p.tinta.copy(alpha = a), Offset(158f, 44f), Offset(158f, 62f), strokeWidth = 6f, cap = redondo)
                celdas(p.celda.copy(alpha = a))
            }
            if (e.opacidadEstela > 0f) {
                drawPath(estela, p.azul.copy(alpha = e.opacidadEstela), style = Stroke(3f, cap = redondo, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 9f))))
            }
            if (e.opacidadAvion > 0f) {
                translate(e.avionX, e.avionY) {
                    rotate(e.avionGrados, pivot = Offset.Zero) {
                        scale(e.avionEscala, e.avionEscala, pivot = Offset.Zero) {
                            drawPath(avion, p.cuerpo.copy(alpha = e.opacidadAvion))
                            drawPath(avion, p.tinta.copy(alpha = e.opacidadAvion), style = Stroke(4f, cap = redondo, join = StrokeJoin.Round))
                            drawPath(ala, p.tinta.copy(alpha = e.opacidadAvion), style = Stroke(3f, cap = redondo, join = StrokeJoin.Round))
                        }
                    }
                }
            }
            // La insignia azul con el reloj, que aparece con un pequeño rebote
            if (e.escalaInsignia > 0f) {
                translate(176f, 176f) {
                    scale(e.escalaInsignia, e.escalaInsignia, pivot = Offset.Zero) {
                        // El centro por omisión es el del lienzo; aquí hace falta el origen de la insignia
                        drawCircle(p.azul, radius = 32f, center = Offset.Zero)
                        drawCircle(p.bordeAzul, radius = 32f, center = Offset.Zero, style = Stroke(6f))
                        drawPath(manecilla, Color.White, style = Stroke(6f, cap = redondo, join = StrokeJoin.Round))
                    }
                }
            }
        }
    }
}
