package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.ui.theme.JuntaTheme

// Lo que ve el vecino al enviar una reserva: confirma que salió y explica qué pasa ahora.
// La reserva queda pendiente hasta que Secretaría la confirme.
@Composable
fun SolicitudEnviadaScreen(onVerMisReservas: () -> Unit) {
    val colores = JuntaTheme.colores
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_solicitud_enviada),
            contentDescription = null,
            modifier = Modifier.size(190.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Solicitud enviada",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Secretaría la revisará. Tu reserva queda pendiente hasta que la confirme.",
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .background(colores.pendienteContenedor)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reloj),
                contentDescription = null,
                tint = colores.sobrePendiente,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Pendiente",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colores.sobrePendiente
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onVerMisReservas,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Ver mis reservas", fontWeight = FontWeight.Bold)
        }
    }
}
