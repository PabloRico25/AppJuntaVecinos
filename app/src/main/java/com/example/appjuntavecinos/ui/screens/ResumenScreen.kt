package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.ui.theme.ErrorRojo
import com.example.appjuntavecinos.ui.theme.FondoSaldo
import com.example.appjuntavecinos.ui.theme.IngresoVerde
import com.example.appjuntavecinos.ui.theme.TextoPrincipal
import com.example.appjuntavecinos.utils.ResumenFinanciero
import com.example.appjuntavecinos.utils.anchoDeVentana
import com.example.appjuntavecinos.utils.nombreDelMes
import com.example.appjuntavecinos.viewmodel.ResumenViewModel

@Composable
fun ResumenScreen(
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
        Text("Resumen financiero", style = MaterialTheme.typography.headlineMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { viewModel.cambiarMes(-1) },
                colors = ButtonDefaults.textButtonColors(contentColor = TextoPrincipal)
            ) {
                Text("Anterior", fontWeight = FontWeight.Bold)
            }
            Text(
                text = nombreDelMes(mes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = { viewModel.cambiarMes(1) },
                enabled = mes < viewModel.mesActual,
                colors = ButtonDefaults.textButtonColors(contentColor = TextoPrincipal)
            ) {
                Text("Siguiente", fontWeight = FontWeight.Bold)
            }
        }

        // Guía 9: un diseño distinto para Compact, Medium y Expanded.
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
            WindowWidthSizeClass.Medium -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TarjetaIngresos(resumen, Modifier.weight(1f))
                    TarjetaGastos(resumen, Modifier.weight(1f))
                }
                TarjetaSaldo(resumen, Modifier.fillMaxWidth())
            }
            else -> {
                TarjetaIngresos(resumen, Modifier.fillMaxWidth())
                TarjetaGastos(resumen, Modifier.fillMaxWidth())
                TarjetaSaldo(resumen, Modifier.fillMaxWidth())
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = TextoPrincipal
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Gastos por categoría",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (resumen.gastosPorCategoria.isEmpty()) {
                    Text("No hay gastos en este mes.")
                } else {
                    resumen.gastosPorCategoria.forEach { (categoria, monto) ->
                        Text("${categoria.lowercase().replaceFirstChar { it.uppercase() }}: ${formatoPesos(monto)}")
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaIngresos(resumen: ResumenFinanciero, modifier: Modifier = Modifier) {
    TarjetaMonto(
        titulo = "Ingresos (arriendos)",
        monto = resumen.ingresos,
        colorMonto = IngresoVerde,
        colorFondo = MaterialTheme.colorScheme.surfaceVariant,
        estilo = MaterialTheme.typography.titleLarge,
        modifier = modifier
    )
}

@Composable
private fun TarjetaGastos(resumen: ResumenFinanciero, modifier: Modifier = Modifier) {
    TarjetaMonto(
        titulo = "Gastos (consumos básicos)",
        monto = resumen.gastos,
        colorMonto = ErrorRojo,
        colorFondo = MaterialTheme.colorScheme.surfaceVariant,
        estilo = MaterialTheme.typography.titleLarge,
        modifier = modifier
    )
}

@Composable
private fun TarjetaSaldo(resumen: ResumenFinanciero, modifier: Modifier = Modifier) {
    TarjetaMonto(
        titulo = "Saldo",
        monto = resumen.saldo,
        colorMonto = if (resumen.saldo < 0) ErrorRojo else IngresoVerde,
        colorFondo = FondoSaldo,
        estilo = MaterialTheme.typography.headlineMedium,
        modifier = modifier
    )
}

@Composable
private fun TarjetaMonto(
    titulo: String,
    monto: Int,
    colorMonto: Color,
    colorFondo: Color,
    estilo: TextStyle,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colorFondo,
            contentColor = TextoPrincipal
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo)
            Text(
                text = formatoPesos(monto),
                style = estilo,
                color = colorMonto,
                fontWeight = FontWeight.Bold
            )
        }
    }
}