package com.example.appjuntavecinos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp


private val LightColorScheme = lightColorScheme(
    primary = AcentoClaro,
    onPrimary = SobreAcento,
    primaryContainer = ContenedorAcentoClaro,
    onPrimaryContainer = SobreContenedorAcentoClaro,
    secondary = SecundarioClaro,
    onSecondary = SuperficieClara,
    secondaryContainer = DecoracionClara,
    onSecondaryContainer = TextoClaro,
    background = FondoClaro,
    onBackground = TextoClaro,
    surface = FondoClaro,
    onSurface = TextoClaro,
    surfaceVariant = SuperficieVariantClara,
    onSurfaceVariant = TextoSecundarioClaro,
    // Las tarjetas, los menús, los diálogos y la barra inferior son blancos sobre el fondo crema
    surfaceContainer = SuperficieClara,
    surfaceContainerHigh = SuperficieClara,
    surfaceContainerHighest = SuperficieClara,
    outline = BordeCampoClaro,
    outlineVariant = BordeDecorativoClaro,
    error = ErrorClaro,
    onError = SobreErrorClaro
)

// No visto en clase: darkColorScheme define la paleta oscura e isSystemInDarkTheme() dice si el teléfono está en modo oscuro.
private val DarkColorScheme = darkColorScheme(
    primary = AcentoOscuro,
    onPrimary = SobreAcento,
    primaryContainer = ContenedorAcentoOscuro,
    onPrimaryContainer = SobreContenedorAcentoOscuro,
    secondary = SobreContenedorAcentoOscuro,
    onSecondary = SobreAcento,
    secondaryContainer = ContenedorAcentoOscuro,
    onSecondaryContainer = TextoOscuro,
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = FondoOscuro,
    onSurface = TextoOscuro,
    surfaceVariant = SuperficieOscura,
    onSurfaceVariant = TextoSecundarioOscuro,
    surfaceContainer = SuperficieOscura,
    surfaceContainerHigh = SuperficieAltaOscura,
    surfaceContainerHighest = SuperficieOscura,
    outline = BordeCampoOscuro,
    outlineVariant = BordeDecorativoOscuro,
    error = ErrorOscuro,
    onError = SobreErrorOscuro
)

@Composable
fun AppJuntaVecinosTheme(
    content: @Composable () -> Unit
) {
    val oscuro = isSystemInDarkTheme()
    CompositionLocalProvider(
        LocalColoresJunta provides if (oscuro) ColoresJuntaOscuros else ColoresJuntaClaros
    ) {
        MaterialTheme(
            colorScheme = if (oscuro) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = Shapes(extraSmall = RoundedCornerShape(16.dp)),
            content = content
        )
    }
}
