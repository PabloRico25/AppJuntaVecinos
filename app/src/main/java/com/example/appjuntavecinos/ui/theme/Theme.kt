package com.example.appjuntavecinos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Paleta de la app: la del ejemplo AppMenu2 del docente (Blue, LightBlue, Gray, Green)
// más los colores de fondo, texto y error de nuestro diseño. Todos están en Color.kt.
private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = TextoBoton,
    primaryContainer = LightBlue,
    secondary = Green,
    background = Fondo,
    onBackground = TextoPrincipal,
    surface = Fondo,
    onSurface = TextoPrincipal,
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