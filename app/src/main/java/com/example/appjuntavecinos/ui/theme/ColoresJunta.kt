package com.example.appjuntavecinos.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// No visto en clase: MaterialTheme solo trae los colores básicos (primario, error, fondo...). Los colores de
// estado de la junta (confirmada, pendiente, rechazada...) no existen ahí, así que se guardan en una clase
// propia y se reparten con un CompositionLocal, que es la forma que tiene Compose de pasar un valor a todas
// las pantallas sin pasarlo de una en una. Se leen con JuntaTheme.colores.
@Immutable
data class ColoresJunta(
    val confirmadaContenedor: Color,
    val sobreConfirmada: Color,
    val pendienteContenedor: Color,
    val sobrePendiente: Color,
    val rechazadaContenedor: Color,
    val sobreRechazada: Color,
    val canceladaContenedor: Color,
    val sobreCancelada: Color,
    val avisoContenedor: Color,
    val sobreAviso: Color,
    val puertaAbierta: Color,
    val puertaCerrada: Color,
    val sobrePuerta: Color,
    val esqueleto: Color,
    val brilloEsqueleto: Color,
    val franjaContenedor: Color,
    val sobreFranja: Color
)

val ColoresJuntaClaros = ColoresJunta(
    confirmadaContenedor = Color(0xFFDDEBCB),
    sobreConfirmada = Color(0xFF1B6032),
    pendienteContenedor = Color(0xFFDCEBFA),
    sobrePendiente = Color(0xFF1A559A),
    rechazadaContenedor = Color(0xFFF5DDD6),
    sobreRechazada = Color(0xFF8F2418),
    canceladaContenedor = Color(0xFFECE4DA),
    sobreCancelada = Color(0xFF6A5848),
    avisoContenedor = Color(0xFFFFF0C8),
    sobreAviso = Color(0xFF6B4A00),
    puertaAbierta = Color(0xFF2A7A3E),
    puertaCerrada = Color(0xFF9E2F22),
    sobrePuerta = Color(0xFFFFFFFF),
    esqueleto = Color(0xFFF0E2D0),
    brilloEsqueleto = Color(0x8CFFFFFF),
    franjaContenedor = Color(0xFFF3E4D0),
    sobreFranja = Color(0xFF5E4636)
)

val ColoresJuntaOscuros = ColoresJunta(
    confirmadaContenedor = Color(0xFF2A3A1E),
    sobreConfirmada = Color(0xFF8CC26A),
    pendienteContenedor = Color(0xFF1F3347),
    sobrePendiente = Color(0xFF9CC6F0),
    rechazadaContenedor = Color(0xFF4A2420),
    sobreRechazada = Color(0xFFFFB4AB),
    canceladaContenedor = Color(0xFF2E2620),
    sobreCancelada = Color(0xFFBBA58F),
    avisoContenedor = Color(0xFF3A2E12),
    sobreAviso = Color(0xFFF0D58A),
    // La puerta se ve igual en claro y en oscuro: es un aviso a distancia
    puertaAbierta = Color(0xFF2A7A3E),
    puertaCerrada = Color(0xFF9E2F22),
    sobrePuerta = Color(0xFFFFFFFF),
    esqueleto = Color(0xFF3A2B20),
    brilloEsqueleto = Color(0x12FFFFFF),
    franjaContenedor = Color(0xFF3A2B20),
    sobreFranja = Color(0xFFCDB8A0)
)

val LocalColoresJunta = staticCompositionLocalOf { ColoresJuntaClaros }

// Para escribir JuntaTheme.colores.confirmadaContenedor, igual que MaterialTheme.colorScheme.primary
object JuntaTheme {
    val colores: ColoresJunta
        @Composable
        @ReadOnlyComposable
        get() = LocalColoresJunta.current
}
