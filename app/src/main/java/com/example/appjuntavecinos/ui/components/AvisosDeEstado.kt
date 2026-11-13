package com.example.appjuntavecinos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.ui.theme.JuntaTheme
import com.example.appjuntavecinos.utils.DURACION_NORMAL_MS
import com.example.appjuntavecinos.utils.desplazamientoSacudida

// Las cuatro piezas que comunican lo que pasa con el servidor. Los textos cambian según el caso;
// el aspecto es siempre el mismo (kit de diseño, entrega de estados con servidor).

// Un error que no es de un campo del formulario sino de la respuesta del servidor
// (por ejemplo "Ese horario ya está ocupado por una reserva confirmada"). Va justo encima del botón principal.
@Composable
fun BannerError(texto: String, modifier: Modifier = Modifier) {
    val colores = JuntaTheme.colores
    val forma = RoundedCornerShape(14.dp)
    // Al aparecer, o al cambiar de mensaje, el banner se sacude una vez: 6 dp a cada lado, en 320 ms
    val sacudida = remember { Animatable(0f) }
    val animar = animacionesActivas()
    LaunchedEffect(texto) {
        if (animar) {
            sacudida.snapTo(0f)
            sacudida.animateTo(1f, tween(DURACION_NORMAL_MS, easing = LinearEasing))
        }
    }
    val dp = with(LocalDensity.current) { 1.dp.toPx() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = desplazamientoSacudida(sacudida.value, listOf(0f, -6f, 6f, -4f, 3f, 0f)) * dp
            }
            .clip(forma)
            .background(colores.rechazadaContenedor)
            .border(BorderStroke(1.5.dp, colores.sobreRechazada.copy(alpha = 0.4f)), forma)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            // Un lector de pantalla anuncia el error cuando aparece
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_error_circulo),
            contentDescription = null,
            tint = colores.sobreRechazada,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = texto,
            color = colores.sobreRechazada,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Franja fina bajo el título de una lista: la app no pudo actualizar y muestra lo que tenía guardado.
// Es información, no una falla grave, por eso el tono es neutro.
@Composable
fun FranjaAviso(
    texto: String,
    modifier: Modifier = Modifier,
    onReintentar: (() -> Unit)? = null
) {
    val colores = JuntaTheme.colores
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colores.franjaContenedor)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_nube_tachada),
            contentDescription = null,
            tint = colores.sobreFranja,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = texto,
            color = colores.sobreFranja,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        if (onReintentar != null) {
            Text(
                text = "Reintentar",
                color = colores.sobreFranja,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onReintentar)
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            )
        }
    }
}

// El dibujo grande de un estado completo: un ícono de 56 dp del kit
@Composable
fun IconoGrande(recurso: Int, color: Color = MaterialTheme.colorScheme.onPrimaryContainer) {
    Icon(
        painter = painterResource(recurso),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(56.dp)
    )
}

// Pantalla completa para un estado: sin conexión, sesión vencida, error del servidor...
// Ilustración o ícono en un círculo, título, una línea de explicación y un botón opcional.
@Composable
fun EstadoCompleto(
    icono: @Composable () -> Unit,
    titulo: String,
    modifier: Modifier = Modifier,
    mensaje: String? = null,
    textoBoton: String? = null,
    iconoBoton: Int? = R.drawable.ic_reintentar,
    circulo: Boolean = true,
    onBoton: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (circulo) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                icono()
            }
        } else {
            // Una ilustración grande va directo sobre el fondo, sin círculo
            icono()
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = titulo,
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (mensaje != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = mensaje,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (textoBoton != null && onBoton != null) {
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onBoton,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (iconoBoton != null) {
                    Icon(
                        painter = painterResource(iconoBoton),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(textoBoton, fontWeight = FontWeight.Bold)
            }
        }
    }
}
