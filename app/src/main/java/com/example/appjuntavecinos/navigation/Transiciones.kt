package com.example.appjuntavecinos.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.example.appjuntavecinos.utils.CurvaJunta
import com.example.appjuntavecinos.utils.DURACION_ENFASIS_MS
import com.example.appjuntavecinos.utils.DURACION_NORMAL_MS

// Cómo se mueve la app al cambiar de pantalla (kit de diseño, MOVIMIENTO.md):
//  - PROFUNDA: ir a una pantalla más adentro (lista, detalle, ticket): entra desde la derecha con un fundido
//    y la anterior se desvanece y se corre un poco a la izquierda. Volver es lo contrario.
//  - PESTANAS: cambiar de pestaña de la barra inferior: solo un fundido cruzado, sin desplazamiento.
//  - FUNDIDO_LARGO: entrar o salir de pantallas aparte (login, sesión vencida, solicitud enviada): fundido lento.
enum class TipoTransicion { PROFUNDA, PESTANAS, FUNDIDO_LARGO }

private val RUTAS_INTERNAS = setOf(Rutas.NUEVA_RESERVA, Rutas.DETALLE_RESERVA, Rutas.TICKET_QR, Rutas.REGISTRAR_PAGO)
private val RUTAS_APARTE = setOf(Rutas.LOGIN, Rutas.SESION_VENCIDA, Rutas.SOLICITUD_ENVIADA)

// Al avanzar manda la pantalla de destino: si es interna, se "entra" a ella
fun tipoAlAvanzar(desde: String?, hacia: String?): TipoTransicion = when {
    desde in RUTAS_APARTE || hacia in RUTAS_APARTE -> TipoTransicion.FUNDIDO_LARGO
    hacia in RUTAS_INTERNAS -> TipoTransicion.PROFUNDA
    else -> TipoTransicion.PESTANAS
}

// Al volver manda la pantalla de la que se sale: si era interna, se "sale" de ella
fun tipoAlVolver(desde: String?, hacia: String?): TipoTransicion = when {
    desde in RUTAS_APARTE || hacia in RUTAS_APARTE -> TipoTransicion.FUNDIDO_LARGO
    desde in RUTAS_INTERNAS -> TipoTransicion.PROFUNDA
    else -> TipoTransicion.PESTANAS
}

// Con "Reducir animaciones" activado en el teléfono, no hay movimiento: se cambia de pantalla directo
fun entradaAlAvanzar(tipo: TipoTransicion, animar: Boolean, desplazamientoPx: Int): EnterTransition {
    if (!animar) return EnterTransition.None
    return when (tipo) {
        TipoTransicion.PROFUNDA ->
            slideInHorizontally(tween(DURACION_NORMAL_MS, easing = CurvaJunta)) { desplazamientoPx } +
                fadeIn(tween(DURACION_NORMAL_MS, easing = CurvaJunta))
        TipoTransicion.PESTANAS -> fadeIn(tween(DURACION_NORMAL_MS))
        TipoTransicion.FUNDIDO_LARGO -> fadeIn(tween(DURACION_ENFASIS_MS))
    }
}

fun salidaAlAvanzar(tipo: TipoTransicion, animar: Boolean, desplazamientoPx: Int): ExitTransition {
    if (!animar) return ExitTransition.None
    return when (tipo) {
        TipoTransicion.PROFUNDA ->
            slideOutHorizontally(tween(DURACION_NORMAL_MS, easing = CurvaJunta)) { -desplazamientoPx / 2 } +
                fadeOut(tween(DURACION_NORMAL_MS, easing = CurvaJunta))
        TipoTransicion.PESTANAS -> fadeOut(tween(DURACION_NORMAL_MS))
        TipoTransicion.FUNDIDO_LARGO -> fadeOut(tween(DURACION_ENFASIS_MS))
    }
}

fun entradaAlVolver(tipo: TipoTransicion, animar: Boolean, desplazamientoPx: Int): EnterTransition {
    if (!animar) return EnterTransition.None
    return when (tipo) {
        TipoTransicion.PROFUNDA ->
            slideInHorizontally(tween(DURACION_NORMAL_MS, easing = CurvaJunta)) { -desplazamientoPx / 2 } +
                fadeIn(tween(DURACION_NORMAL_MS, easing = CurvaJunta))
        TipoTransicion.PESTANAS -> fadeIn(tween(DURACION_NORMAL_MS))
        TipoTransicion.FUNDIDO_LARGO -> fadeIn(tween(DURACION_ENFASIS_MS))
    }
}

fun salidaAlVolver(tipo: TipoTransicion, animar: Boolean, desplazamientoPx: Int): ExitTransition {
    if (!animar) return ExitTransition.None
    return when (tipo) {
        TipoTransicion.PROFUNDA ->
            slideOutHorizontally(tween(DURACION_NORMAL_MS, easing = CurvaJunta)) { desplazamientoPx } +
                fadeOut(tween(DURACION_NORMAL_MS, easing = CurvaJunta))
        TipoTransicion.PESTANAS -> fadeOut(tween(DURACION_NORMAL_MS))
        TipoTransicion.FUNDIDO_LARGO -> fadeOut(tween(DURACION_ENFASIS_MS))
    }
}
