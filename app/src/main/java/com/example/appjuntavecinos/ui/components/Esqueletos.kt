package com.example.appjuntavecinos.ui.components

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.ui.theme.JuntaTheme

// No visto en clase: Android tiene un ajuste, "Reducir animaciones", que pone la escala de duración en 0.
// Las animaciones de la app lo respetan: sin ese movimiento se muestra directo el estado final.
@Composable
fun animacionesActivas(): Boolean {
    val contexto = LocalContext.current
    return remember {
        Settings.Global.getFloat(contexto.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
}

// Un rectángulo gris que ocupa el lugar de un contenido que todavía no llegó. Una franja clara lo cruza de
// izquierda a derecha cada 1,8 segundos para que se note que algo está cargando.
@Composable
fun BloqueEsqueleto(modifier: Modifier = Modifier, radio: Dp = 8.dp) {
    val colores = JuntaTheme.colores
    val progreso = if (animacionesActivas()) {
        rememberInfiniteTransition(label = "brillo").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
            label = "brillo"
        )
    } else {
        null
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radio))
            .background(colores.esqueleto)
            .drawWithContent {
                drawContent()
                if (progreso != null) {
                    val inicio = -size.width + progreso.value * 2f * size.width
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, colores.brilloEsqueleto, Color.Transparent),
                            startX = inicio,
                            endX = inicio + size.width
                        )
                    )
                }
            }
    )
}

@Composable
private fun TarjetaDeEsqueleto(contenido: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth()
    ) {
        contenido()
    }
}

// La lista de Mis reservas mientras carga
@Composable
fun EsqueletoListaReservas(modifier: Modifier = Modifier, cantidad: Int = 4) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(cantidad) {
            TarjetaDeEsqueleto {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    BloqueEsqueleto(Modifier.size(48.dp), radio = 16.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BloqueEsqueleto(Modifier.width(160.dp).height(14.dp))
                        BloqueEsqueleto(Modifier.width(220.dp).height(12.dp))
                        BloqueEsqueleto(Modifier.width(72.dp).height(18.dp), radio = 11.dp)
                    }
                }
            }
        }
    }
}

// El Detalle de una reserva mientras carga
@Composable
fun EsqueletoDetalle(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TarjetaDeEsqueleto {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BloqueEsqueleto(Modifier.size(56.dp), radio = 16.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BloqueEsqueleto(Modifier.width(150.dp).height(16.dp))
                        BloqueEsqueleto(Modifier.width(80.dp).height(18.dp), radio = 11.dp)
                    }
                }
                repeat(4) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BloqueEsqueleto(Modifier.size(18.dp), radio = 6.dp)
                        Spacer(Modifier.width(12.dp))
                        BloqueEsqueleto(Modifier.width(190.dp).height(12.dp))
                    }
                }
            }
        }
        BloqueEsqueleto(Modifier.fillMaxWidth().height(52.dp), radio = 26.dp)
    }
}

// Las franjas de Disponibilidad mientras cargan
@Composable
fun EsqueletoDisponibilidad(modifier: Modifier = Modifier, cantidad: Int = 4) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(cantidad) {
            TarjetaDeEsqueleto {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BloqueEsqueleto(Modifier.width(110.dp).height(14.dp))
                    BloqueEsqueleto(Modifier.width(64.dp).height(20.dp), radio = 11.dp)
                }
            }
        }
    }
}
