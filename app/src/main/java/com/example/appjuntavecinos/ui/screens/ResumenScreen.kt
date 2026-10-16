package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.appjuntavecinos.viewmodel.ResumenViewModel

@Composable
fun ResumenScreen(
    viewModel: ResumenViewModel = viewModel()
) {
    val resumen by viewModel.resumen.collectAsState(
        initial = ResumenFinanciero(0, 0, 0, emptyMap())
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Resumen financiero", style = MaterialTheme.typography.headlineMedium)
        Text(viewModel.nombreMes)

        TarjetaMonto(
            titulo = "Ingresos (arriendos)",
            monto = resumen.ingresos,
            colorMonto = IngresoVerde,
            colorFondo = MaterialTheme.colorScheme.surfaceVariant,
            estilo = MaterialTheme.typography.titleLarge
        )
        TarjetaMonto(
            titulo = "Gastos (consumos básicos)",
            monto = resumen.gastos,
            colorMonto = ErrorRojo,
            colorFondo = MaterialTheme.colorScheme.surfaceVariant,
            estilo = MaterialTheme.typography.titleLarge
        )
        TarjetaMonto(
            titulo = "Saldo",
            monto = resumen.saldo,
            colorMonto = if (resumen.saldo < 0) ErrorRojo else IngresoVerde,
            colorFondo = FondoSaldo,
            estilo = MaterialTheme.typography.headlineMedium
        )

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
                    Text("Este mes no hay gastos.")
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
private fun TarjetaMonto(
    titulo: String,
    monto: Int,
    colorMonto: Color,
    colorFondo: Color,
    estilo: TextStyle
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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