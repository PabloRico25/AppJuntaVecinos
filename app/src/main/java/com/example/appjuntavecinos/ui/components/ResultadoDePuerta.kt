package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.ui.theme.JuntaTheme
import com.example.appjuntavecinos.utils.EstadoPuerta
import com.example.appjuntavecinos.utils.MotivoPuerta
import com.example.appjuntavecinos.utils.ResultadoPuerta

// Lo que muestra la puerta después de leer un código: pantalla completa, verde si abre y roja si no. Se lee desde
// lejos, por eso el estado no va solo en el color: lleva un ícono según el motivo y el texto. Tocarla la cierra.
@Composable
fun ResultadoDePuerta(resultado: ResultadoPuerta, onToque: () -> Unit) {
    val colores = JuntaTheme.colores
    val abierta = resultado.estado == EstadoPuerta.ABIERTA
    val fondo = if (abierta) colores.puertaAbierta else colores.puertaCerrada
    val decoracion = if (abierta) colores.puertaAbiertaDeco else colores.puertaCerradaDeco
    val icono = when (resultado.motivo) {
        MotivoPuerta.ABIERTA -> R.drawable.ic_check
        MotivoPuerta.YA_USADO -> R.drawable.ic_ticket_tachado
        MotivoPuerta.OTRA_SALA -> R.drawable.ic_puerta_flecha
        MotivoPuerta.FUERA_DE_HORARIO -> R.drawable.ic_reloj
        MotivoPuerta.NO_VALIDO -> R.drawable.ic_qr_error
        MotivoPuerta.SIN_CONEXION -> R.drawable.ic_nube_tachada
        MotivoPuerta.ERROR_SERVIDOR -> R.drawable.ic_servidor
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .clickable(onClick = onToque)
            // Un lector de pantalla anuncia el resultado apenas aparece
            .semantics { liveRegion = LiveRegionMode.Assertive }
    ) {
        // Dos círculos decorativos, uno arriba a la derecha y otro abajo a la izquierda
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 90.dp, y = (-90).dp)
                .size(240.dp)
                .clip(CircleShape)
                .background(decoracion)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-110).dp, y = 60.dp)
                .size(260.dp)
                .clip(CircleShape)
                .background(decoracion)
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(132.dp).clip(CircleShape).background(colores.sobrePuerta),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icono),
                    contentDescription = null,
                    tint = fondo,
                    modifier = Modifier.size(76.dp)
                )
            }
            Spacer(Modifier.height(28.dp))
            Text(
                text = if (abierta) "Puerta abierta" else "Puerta cerrada",
                color = colores.sobrePuerta,
                fontSize = 38.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = resultado.mensaje,
                color = colores.sobrePuerta,
                fontSize = 19.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
