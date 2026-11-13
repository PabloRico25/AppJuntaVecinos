package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.ui.components.BotonTexto
import com.example.appjuntavecinos.ui.components.TarjetaBlanca
import com.example.appjuntavecinos.ui.theme.JuntaTheme
import com.example.appjuntavecinos.utils.ResumenFinanciero
import com.example.appjuntavecinos.utils.anchoDeVentana
import com.example.appjuntavecinos.utils.nombreDelMes
import com.example.appjuntavecinos.viewmodel.ResumenViewModel

@Composable
fun ResumenScreen(
    titulo: String,
    viewModel: ResumenViewModel = viewModel()
) {
    val resumen by viewModel.resumen.collectAsState(
        initial = ResumenFinanciero(0, 0, 0, emptyMap())
    )
    val mes by viewModel.mes.collectAsState()
    val ancho = anchoDeVentana()

    Column(
        modifier = Modifier
            .fillMaxSize()
            // No visto en clase (lo integramos con permiso del docente): verticalScroll deja recorrer la pantalla hacia abajo.
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonTexto(
                onClick = { viewModel.cambiarMes(-1) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Text("Anterior", fontWeight = FontWeight.Bold)
            }
            Text(
                text = nombreDelMes(mes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            BotonTexto(
                onClick = { viewModel.cambiarMes(1) },
                enabled = mes < viewModel.mesActual,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Text("Siguiente", fontWeight = FontWeight.Bold)
            }
        }


        when (ancho) {
            WindowWidthSizeClass.Expanded -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TarjetaIngresos(resumen, Modifier.weight(1f))
                    TarjetaGastos(resumen, Modifier.weight(1f))
                    TarjetaSaldo(resumen, Modifier.weight(1f))
                }
            }
            else -> {
                TarjetaSaldo(resumen, Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TarjetaIngresos(resumen, Modifier.weight(1f))
                    TarjetaGastos(resumen, Modifier.weight(1f))
                }
            }
        }

        TarjetaBlanca(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Gastos por categoría",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                if (resumen.gastosPorCategoria.isEmpty()) {
                    Text("No hay gastos en este mes.")
                } else {
                    val mayor = resumen.gastosPorCategoria.values.maxOrNull()?.coerceAtLeast(1) ?: 1
                    resumen.gastosPorCategoria.forEach { (categoria, monto) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(categoria.lowercase().replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold)
                                Text(formatoPesos(monto), fontWeight = FontWeight.ExtraBold)
                            }
                            LinearProgressIndicator(
                                progress = { monto.toFloat() / mayor },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant,
                                strokeCap = StrokeCap.Round,
                                gapSize = 0.dp,
                                drawStopIndicator = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaIngresos(resumen: ResumenFinanciero, modifier: Modifier = Modifier) {
    TarjetaFlujo(
        titulo = "Ingresos (arriendos)",
        monto = resumen.ingresos,
        icono = R.drawable.ic_flecha_arriba,
        fondoIcono = JuntaTheme.colores.confirmadaContenedor,
        tintaIcono = JuntaTheme.colores.sobreConfirmada,
        modifier = modifier
    )
}

@Composable
private fun TarjetaGastos(resumen: ResumenFinanciero, modifier: Modifier = Modifier) {
    TarjetaFlujo(
        titulo = "Gastos (consumos básicos)",
        monto = resumen.gastos,
        icono = R.drawable.ic_flecha_abajo,
        fondoIcono = MaterialTheme.colorScheme.primaryContainer,
        tintaIcono = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
    )
}

// Tarjeta del saldo: oscura, como en el kit. Si el saldo es negativo, la cifra va en el color de error sobre fondo claro.
@Composable
private fun TarjetaSaldo(resumen: ResumenFinanciero, modifier: Modifier = Modifier) {
    val negativo = resumen.saldo < 0
    val fondo = if (negativo) MaterialTheme.colorScheme.errorContainer else Color(0xFF6B3F27)
    val texto = if (negativo) MaterialTheme.colorScheme.onErrorContainer else Color(0xFFFFF4E8)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = texto)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = texto.copy(alpha = 0.18f), contentColor = texto) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_nav_pagos), contentDescription = null, modifier = Modifier.size(26.dp))
                }
            }
            Column {
                Text("Saldo", style = MaterialTheme.typography.bodyMedium)
                Text(formatoPesos(resumen.saldo), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun TarjetaFlujo(
    titulo: String,
    monto: Int,
    icono: Int,
    fondoIcono: Color,
    tintaIcono: Color,
    modifier: Modifier = Modifier
) {
    TarjetaBlanca(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(10.dp), color = fondoIcono, contentColor = tintaIcono) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
            Text(titulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatoPesos(monto), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }
    }
}
