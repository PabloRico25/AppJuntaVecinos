package com.example.appjuntavecinos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.appjuntavecinos.R

// No visto en clase: Nunito Sans es la tipografía del kit de diseño. Es una fuente variable: un solo archivo
// (res/font/nunito_sans.ttf) trae todos los pesos, y FontVariation elige el que corresponde a cada uno.
// Licencia SIL Open Font License, guardada en assets/licencias. En Android 7 (que no maneja fuentes variables)
// se ve en un solo peso; desde Android 8 se ven los cuatro.
@OptIn(ExperimentalTextApi::class)
private fun nunito(peso: FontWeight) = Font(
    resId = R.font.nunito_sans,
    weight = peso,
    variationSettings = FontVariation.Settings(FontVariation.weight(peso.weight))
)

val NunitoSans = FontFamily(
    nunito(FontWeight.Normal),
    nunito(FontWeight.SemiBold),
    nunito(FontWeight.Bold),
    nunito(FontWeight.ExtraBold)
)

// No visto en clase: Poppins es la letra del nombre del logo y solo se usa ahí (kit de diseño, design_tokens.json).
// Va en dos pesos: Bold para "JUNTA ZAROR" y Medium para las dos líneas de abajo. Licencia SIL OFL en assets/licencias.
val Poppins = FontFamily(
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private val Base = Typography()

// Los 15 estilos de Material usan Nunito Sans; el cuerpo grande conserva su tamaño y su interlineado.
// Material pide los botones y las etiquetas en peso medio (500), que esta fuente no trae en los cuatro pesos
// elegidos: por eso esos estilos piden SemiBold o Bold, que es como los dibuja el kit.
val Typography = Typography(
    displayLarge = Base.displayLarge.copy(fontFamily = NunitoSans),
    displayMedium = Base.displayMedium.copy(fontFamily = NunitoSans),
    displaySmall = Base.displaySmall.copy(fontFamily = NunitoSans),
    headlineLarge = Base.headlineLarge.copy(fontFamily = NunitoSans),
    headlineMedium = Base.headlineMedium.copy(fontFamily = NunitoSans),
    headlineSmall = Base.headlineSmall.copy(fontFamily = NunitoSans),
    titleLarge = Base.titleLarge.copy(fontFamily = NunitoSans),
    titleMedium = Base.titleMedium.copy(fontFamily = NunitoSans, fontWeight = FontWeight.SemiBold),
    titleSmall = Base.titleSmall.copy(fontFamily = NunitoSans, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = Base.bodyMedium.copy(fontFamily = NunitoSans),
    bodySmall = Base.bodySmall.copy(fontFamily = NunitoSans),
    labelLarge = Base.labelLarge.copy(fontFamily = NunitoSans, fontWeight = FontWeight.Bold),
    labelMedium = Base.labelMedium.copy(fontFamily = NunitoSans, fontWeight = FontWeight.SemiBold),
    labelSmall = Base.labelSmall.copy(fontFamily = NunitoSans, fontWeight = FontWeight.SemiBold)
)
