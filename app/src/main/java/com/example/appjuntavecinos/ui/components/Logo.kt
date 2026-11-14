package com.example.appjuntavecinos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.ui.theme.Poppins
import com.example.appjuntavecinos.utils.CurvaJunta
import com.example.appjuntavecinos.utils.DURACION_ENFASIS_MS
import kotlinx.coroutines.delay

// El logo de Junta Zaror: el símbolo (la C, la torre J, la Z, el avión y la casa con la puerta de tres cuadritos)
// y su nombre en tres líneas. El símbolo es un dibujo vectorial (res/drawable/ic_marca_simbolo.xml, con otra
// versión en drawable-night para el modo oscuro) y el texto se escribe con la fuente de la app, así que no hay
// ninguna imagen: todo se ve nítido a cualquier tamaño. Al abrir sube 24 dp con un fundido, y el texto entra un
// instante después. Con "Reducir animaciones" aparece directo.
@Composable
fun LogoJuntaZaror(modifier: Modifier = Modifier) {
    val animar = animacionesActivas()
    val simbolo = remember { Animatable(if (animar) 0f else 1f) }
    val texto = remember { Animatable(if (animar) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (animar) {
            simbolo.animateTo(1f, tween(DURACION_ENFASIS_MS, easing = CurvaJunta))
        }
    }
    LaunchedEffect(Unit) {
        if (animar) {
            delay(250)
            texto.animateTo(1f, tween(DURACION_ENFASIS_MS, easing = CurvaJunta))
        }
    }
    val subida = with(LocalDensity.current) { 24.dp.toPx() }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // El símbolo es decorativo: el nombre está escrito justo debajo
        Image(
            painter = painterResource(R.drawable.ic_marca_simbolo),
            contentDescription = null,
            modifier = Modifier
                .size(170.dp)
                .graphicsLayer {
                    alpha = simbolo.value
                    translationY = (1f - simbolo.value) * subida
                }
        )
        Spacer(Modifier.height(8.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                alpha = texto.value
                translationY = (1f - texto.value) * subida / 2f
            }
        ) {
            Text(
                text = "JUNTA ZAROR",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = Poppins,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
            // La raya naranja bajo el nombre, como en el tablero del logo
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "JUNTA DE VECINOS",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = Poppins,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.6.sp
            )
            Text(
                text = "POBLACIÓN ZAROR",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = Poppins,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.6.sp
            )
        }
    }
}
