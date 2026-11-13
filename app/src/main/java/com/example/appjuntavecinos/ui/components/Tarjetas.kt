package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
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

// Fila de la ficha de detalle: ícono, etiqueta pequeña y valor en negrita.
@Composable
fun FilaDato(icono: Int, etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        Column {
            Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// Etiqueta de un bloque de horario: "Libre" va con contorno, los demás con su color de estado.
@Composable
fun EtiquetaBloque(tipo: String, modifier: Modifier = Modifier) {
    when (tipo) {
        "CONFIRMADA" -> EtiquetaEstado("CONFIRMADA", "Confirmada", modifier)
        "SOLICITADA" -> EtiquetaEstado("PENDIENTE", "Solicitada", modifier)
        else -> Surface(
            modifier = modifier,
            shape = RoundedCornerShape(50),
            color = Color.Transparent,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text("Libre", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Tarjeta blanca con borde suave: base de las fichas de Detalle, Disponibilidad y Finanzas.
@Composable
fun TarjetaBlanca(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) { contenido() }
}

// Franja de aviso con candado: se usa cuando el cargo de la persona no permite una acción.
@Composable
fun AvisoInformativo(texto: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = JuntaTheme.colores.canceladaContenedor,
        contentColor = JuntaTheme.colores.sobreCancelada
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(texto, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
