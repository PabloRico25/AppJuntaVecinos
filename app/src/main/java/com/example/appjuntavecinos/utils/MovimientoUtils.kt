package com.example.appjuntavecinos.utils

import androidx.compose.animation.core.CubicBezierEasing
import kotlin.math.PI
import kotlin.math.cos

// Los tiempos y las curvas del movimiento de Junta Zaror (kit de diseño, MOVIMIENTO.md). Las cuentas están
// aquí, separadas de Compose, para poder probarlas sin dibujar nada.

const val DURACION_NORMAL_MS = 320
const val DURACION_ENFASIS_MS = 560
const val DURACION_BUCLE_MS = 1800

val CurvaJunta = CubicBezierEasing(0.22f, 0.75f, 0.25f, 1f)

// Qué fracción (de 0 a 1) lleva una animación que empieza en inicioMs y dura duracionMs, cuando ya pasaron tiempoMs
fun progresoDeTramo(tiempoMs: Float, inicioMs: Float, duracionMs: Float): Float {
    if (duracionMs <= 0f) return if (tiempoMs >= inicioMs) 1f else 0f
    return ((tiempoMs - inicioMs) / duracionMs).coerceIn(0f, 1f)
}

// El "pop" de las insignias: crece de 0 a 1,15 (cuando va en el 65 %) y se asienta en 1
fun escalaPop(progreso: Float): Float {
    val p = progreso.coerceIn(0f, 1f)
    return if (p < 0.65f) 1.15f * (p / 0.65f) else 1.15f - 0.15f * ((p - 0.65f) / 0.35f)
}

// La sacudida de un ticket, una puerta o un campo con error: va y viene y termina quieta. Devuelve dp.
private val PUNTOS_SACUDIDA = listOf(0f, -7f, 7f, -5f, 4f, 0f)

fun desplazamientoSacudida(progreso: Float, puntos: List<Float> = PUNTOS_SACUDIDA): Float {
    val p = progreso.coerceIn(0f, 1f) * (puntos.size - 1)
    val tramo = p.toInt().coerceAtMost(puntos.size - 2)
    val dentro = p - tramo
    return puntos[tramo] + (puntos[tramo + 1] - puntos[tramo]) * dentro
}

// Una pulsación suave que se repite: 0 al inicio, 1 en la mitad y 0 al final de cada vuelta
fun pulso(tiempoMs: Float, inicioMs: Float, periodoMs: Float = DURACION_BUCLE_MS.toFloat()): Float {
    if (tiempoMs < inicioMs) return 0f
    val fase = ((tiempoMs - inicioMs) % periodoMs) / periodoMs
    return ((1f - cos(2f * PI.toFloat() * fase)) / 2f)
}

// Las tarjetas de una lista aparecen una tras otra, 40 ms entre cada una. Solo las primeras: más abajo
// aparecen quietas, para que al deslizar la lista no parpadee.
const val MAXIMO_ESCALONADAS = 8

fun debeAparecerEscalonado(indice: Int): Boolean = indice in 0..MAXIMO_ESCALONADAS

fun retrasoEscalonado(indice: Int, pasoMs: Int = 40): Long = if (debeAparecerEscalonado(indice)) indice * pasoMs.toLong() else 0L

// ---- Animación "sin conexión" (kit: sin_conexion). Todo en una escala de 240 x 240.

data class EstadoSinConexion(
    val opacidadNube: Float,
    val subidaNube: Float,      // cuánto falta por subir, en unidades del dibujo (24 al inicio, 0 al final)
    val progresoRaya: Float,    // la raya roja se dibuja de abajo a arriba
    val escalaNube: Float,      // la nube "respira" entre 1 y 1,04
    val opacidadPuntos: List<Float>
)

fun estadoSinConexion(tiempoMs: Float, animar: Boolean = true): EstadoSinConexion {
    if (!animar) return EstadoSinConexion(1f, 0f, 1f, 1f, listOf(1f, 1f, 1f))
    val entrada = CurvaJunta.transform(progresoDeTramo(tiempoMs, 0f, 560f))
    val raya = CurvaJunta.transform(progresoDeTramo(tiempoMs, 400f, 450f))
    val respiro = 1f + 0.04f * pulso(tiempoMs, 1000f)
    // Los tres puntos parpadean uno tras otro: arrancan a 1,0 s, 1,25 s y 1,5 s
    val puntos = listOf(1000f, 1250f, 1500f).map { 0.25f + 0.75f * pulso(tiempoMs, it) }
    return EstadoSinConexion(entrada, 24f * (1f - entrada), raya, respiro, puntos)
}

// ---- Animación "solicitud enviada" (kit: solicitud_enviada). 1550 ms, una vez.

const val DURACION_SOLICITUD_MS = 1550f

data class EstadoSolicitudEnviada(
    val opacidadCalendario: Float,
    val subidaCalendario: Float,
    val opacidadEstela: Float,
    val avionX: Float,
    val avionY: Float,
    val avionGrados: Float,
    val avionEscala: Float,
    val opacidadAvion: Float,
    val escalaInsignia: Float
)

private fun mezcla(a: Float, b: Float, t: Float) = a + (b - a) * t

fun estadoSolicitudEnviada(tiempoMs: Float, animar: Boolean = true): EstadoSolicitudEnviada {
    // Sin animación se ve el cuadro final: el calendario con su insignia, sin el avión
    if (!animar) return EstadoSolicitudEnviada(1f, 0f, 0f, 168f, 168f, 24f, 0.35f, 0f, 1f)

    val subida = CurvaJunta.transform(progresoDeTramo(tiempoMs, 0f, 500f))
    val vuelo = progresoDeTramo(tiempoMs, 400f, 700f)
    // El avión recorre tres puntos: sale abajo a la izquierda, pasa por el centro y se pierde en la esquina
    val x: Float; val y: Float; val grados: Float; val escala: Float; val opacidad: Float
    if (vuelo < 0.65f) {
        val e = CurvaJunta.transform(vuelo / 0.65f)
        x = mezcla(14f, 104f, e); y = mezcla(206f, 104f, e); grados = mezcla(0f, -12f, e); escala = 1f
        opacidad = (vuelo / 0.15f).coerceIn(0f, 1f)
    } else {
        val e = CurvaJunta.transform((vuelo - 0.65f) / 0.35f)
        x = mezcla(104f, 168f, e); y = mezcla(104f, 168f, e); grados = mezcla(-12f, 24f, e); escala = mezcla(1f, 0.35f, e)
        opacidad = 1f - e
    }
    val estela = progresoDeTramo(tiempoMs, 450f, 900f)
    val opacidadEstela = if (estela < 0.3f) estela / 0.3f else 1f - (estela - 0.3f) / 0.7f
    val insignia = escalaPop(progresoDeTramo(tiempoMs, 1050f, 500f))
    return EstadoSolicitudEnviada(subida, 24f * (1f - subida), opacidadEstela.coerceIn(0f, 1f), x, y, grados, escala, opacidad, insignia)
}
