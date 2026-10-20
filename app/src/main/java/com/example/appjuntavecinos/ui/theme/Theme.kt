package com.example.appjuntavecinos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = TextoBoton,
    primaryContainer = LightBlue,
    secondary = Green,
    secondaryContainer = LightBlue,
    onSecondaryContainer = TextoPrincipal,
    background = Fondo,
    onBackground = TextoPrincipal,
    surface = Fondo,
    onSurface = TextoPrincipal,
    surfaceContainer = FondoBarra,
    onSurfaceVariant = TextoSecundario,
    outline = BordeCampo,
    error = ErrorRojo
)

// No visto en clase: darkColorScheme define la paleta oscura e isSystemInDarkTheme() elige cuál usar según el modo del teléfono.
private val DarkColorScheme = darkColorScheme(
    primary = Blue,
    onPrimary = TextoBoton,
    primaryContainer = FondoPrimarioOscuro,
    onPrimaryContainer = TextoSobreOscuro,
    secondary = Green,
    onSecondary = TextoBoton,
    secondaryContainer = LightBlue,
    onSecondaryContainer = TextoPrincipal,
    background = FondoOscuro,
    onBackground = TextoSobreOscuro,
    surface = FondoOscuro,
    onSurface = TextoSobreOscuro,
    surfaceVariant = FondoTarjetaOscuro,
    onSurfaceVariant = TextoSecundarioOscuro,
    surfaceContainer = FondoBarraOscuro,
    surfaceContainerHigh = FondoDialogoOscuro,
    surfaceContainerHighest = FondoTarjetaOscuro,
    outline = BordeCampoOscuro,
    outlineVariant = BordeCampo,
    error = ErrorRojoOscuro,
    onError = TextoSobreErrorOscuro
)

// Sin colores dinámicos del teléfono: la app usa siempre estas dos paletas, la clara o la oscura según el modo del sistema.
@Composable
fun AppJuntaVecinosTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}