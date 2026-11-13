package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.ui.theme.JuntaTheme

// Piezas base del kit de diseño: tarjeta blanca con borde suave, ficha de ícono y etiqueta de estado con ícono.
// El estado ya no se dice solo con el color: lleva ícono y texto (accesibilidad, no depender del color).

@Composable
fun EtiquetaEstado(estado: String, texto: String, modifier: Modifier = Modifier) {
    val colores = JuntaTheme.colores
    val (fondo, tinta) = when (estado) {
        "PENDIENTE" -> colores.pendienteContenedor to colores.sobrePendiente
        "CONFIRMADA" -> colores.confirmadaContenedor to colores.sobreConfirmada
        "RECHAZADA" -> colores.rechazadaContenedor to colores.sobreRechazada
        else -> colores.canceladaContenedor to colores.sobreCancelada
    }
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = fondo, contentColor = tinta) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when (estado) {
                "CONFIRMADA" -> Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                "PENDIENTE" -> Icon(painterResource(R.drawable.ic_reloj), contentDescription = null, modifier = Modifier.size(14.dp))
                "RECHAZADA" -> Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                else -> Text("−", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Text(texto, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FichaIcono(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primaryContainer, contenido: @Composable () -> Unit) {
    Surface(
        modifier = modifier.size(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = color,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) { contenido() }
    }
}

@Composable
fun TarjetaReserva(
    nombre: String,
    detalle: String,
    estado: String,
    textoEstado: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FichaIcono { Icon(Icons.Filled.Home, contentDescription = null) }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(detalle, style = MaterialTheme.typography.bodyMedium)
                EtiquetaEstado(estado, textoEstado)
            }
        }
    }
}
