package com.example.appjuntavecinos.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.ui.theme.JuntaTheme
import com.example.appjuntavecinos.utils.CurvaJunta
import com.example.appjuntavecinos.utils.DURACION_ENFASIS_MS
import com.example.appjuntavecinos.utils.DURACION_NORMAL_MS

// Piezas base del kit de diseño: tarjeta blanca con borde suave, ficha de ícono y etiqueta de estado con ícono.
// El estado ya no se dice solo con el color: lleva ícono y texto (accesibilidad, no depender del color).

@Composable
fun EtiquetaEstado(estado: String, texto: String, modifier: Modifier = Modifier) {
    val colores = JuntaTheme.colores
    val (fondo, tinta) = when (estado) {
        "PENDIENTE" -> colores.pendienteContenedor to colores.sobrePendiente
        "CONFIRMADA" -> colores.confirmadaContenedor to colores.sobreConfirmada
        "RECHAZADA" -> colores.rechazadaContenedor to colores.sobreRechazada
        // Cancelada: fondo neutro, pero con la tinta roja de Rechazada. Las dos son un "no" y así se distinguen
        // de Pendiente y Confirmada; el fondo las diferencia entre sí
        else -> colores.canceladaContenedor to colores.sobreRechazada
    }
    // Si la reserva cambia de estado (por ejemplo la Secretaría la confirma), el chip cambia de color con un fundido
    val fondoAnimado by animateColorAsState(fondo, tween(DURACION_NORMAL_MS), label = "fondoEstado")
    val tintaAnimada by animateColorAsState(tinta, tween(DURACION_NORMAL_MS), label = "tintaEstado")
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = fondoAnimado, contentColor = tintaAnimada) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when (estado) {
                "CONFIRMADA" -> Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                "PENDIENTE" -> Icon(painterResource(R.drawable.ic_reloj), contentDescription = null, modifier = Modifier.size(14.dp))
                else -> Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp))
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

// Un bloque con la foto de una sala de fondo y el contenido abajo, sobre un degradado oscuro que lo deja legible
// en cualquier foto. Sin foto, queda un fondo liso. Lo usan la tarjeta de la lista y el encabezado del detalle.
@Composable
fun BloqueConFoto(
    modifier: Modifier = Modifier,
    @DrawableRes foto: Int? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        if (foto != null) {
            // La foto es decorativa: el nombre de la sala está escrito encima
            Image(
                painter = painterResource(foto),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.matchParentSize().background(
                    Brush.verticalGradient(colors = listOf(Color(0x14000000), Color(0xE6000000)))
                )
            )
        }
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = contenido
        )
    }
}

// Tarjeta de una reserva en la lista.
@Composable
fun TarjetaReserva(
    nombre: String,
    detalle: String,
    estado: String,
    textoEstado: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes foto: Int? = null
) {
    val tinta = if (foto != null) Color.White else MaterialTheme.colorScheme.onSurface
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = tinta
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        BloqueConFoto(modifier = Modifier.fillMaxWidth().height(172.dp), foto = foto) {
            Text(nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = tinta)
            Text(detalle, style = MaterialTheme.typography.bodyMedium, color = tinta)
            EtiquetaEstado(estado, textoEstado, Modifier.padding(top = 2.dp))
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

// Barra de un gasto: crece desde cero hasta su valor. Cada barra arranca un poco después de la anterior, así crecen en
// secuencia (kit de diseño: "grafico_finanzas"). Con "Reducir animaciones" aparece directo en su valor.
// La clave hace que vuelva a crecer cuando cambia el mes.
@Composable
fun BarraDeGasto(proporcion: Float, indice: Int, clave: Any, modifier: Modifier = Modifier) {
    val animar = animacionesActivas()
    val avance = remember(clave) { Animatable(if (animar) 0f else proporcion) }
    LaunchedEffect(clave, proporcion) {
        if (animar) {
            avance.animateTo(proporcion, tween(DURACION_ENFASIS_MS, delayMillis = indice * 140, easing = CurvaJunta))
        } else {
            avance.snapTo(proporcion)
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(avance.value.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

// Ficha cuadrada con el ícono de una categoría de gasto. El color de fondo sale del tema, así sirve en claro y en oscuro.
@Composable
fun FichaDeGasto(categoria: CategoriaDeGasto, modifier: Modifier = Modifier) {
    val colores = JuntaTheme.colores
    val fondo = when (categoria) {
        CategoriaDeGasto.AGUA -> colores.pendienteContenedor
        CategoriaDeGasto.ELECTRICIDAD -> colores.avisoContenedor
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    Surface(modifier = modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = fondo) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(categoria.icono), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(26.dp))
        }
    }
}
