package com.example.appjuntavecinos.ui.theme

import androidx.compose.material3.MaterialTheme
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

// Sin colores dinámicos del teléfono ni modo oscuro: la app usa siempre esta paleta.
@Composable
fun AppJuntaVecinosTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}