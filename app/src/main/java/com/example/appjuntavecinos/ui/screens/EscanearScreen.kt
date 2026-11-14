package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.ui.components.BotonContorno
import com.example.appjuntavecinos.ui.components.BotonJunta
import com.example.appjuntavecinos.ui.components.IconoDeError
import com.example.appjuntavecinos.ui.components.IlustracionQrScan
import com.example.appjuntavecinos.ui.components.sacudirSiHayError
import com.example.appjuntavecinos.ui.theme.JuntaTheme
import com.example.appjuntavecinos.utils.EstadoIngreso
import com.example.appjuntavecinos.utils.fechaParaMostrar
import com.example.appjuntavecinos.viewmodel.EscanearViewModel
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
fun EscanearScreen(
    viewModel: EscanearViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val dependencias by viewModel.dependencias.collectAsState()
    val context = LocalContext.current

    // No visto en clase (lo integramos con permiso del docente): lector de códigos de Google (ML Kit).
    fun abrirLector() {
        GmsBarcodeScanning.getClient(context)
            .startScan()
            .addOnSuccessListener { codigo ->
                val texto = codigo.rawValue
                if (texto != null) {
                    viewModel.validar(texto)
                }
            }
            .addOnFailureListener {
                viewModel.avisarError("No se pudo abrir el lector. Escribe el código a mano")
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // No visto en clase (lo integramos con permiso del docente): verticalScroll deja recorrer la pantalla hacia abajo.
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Escanear QR", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("Escanea el código del vecino para ver si puede entrar.")

        // Mientras no hay nada que mostrar, el marco con el QR del diseño (kit: sin_resultados_qr)
        if (estado.resultado == null) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                IlustracionQrScan(tamano = 150.dp)
            }
        }

        BotonJunta(
            onClick = { abrirLector() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Escanear con la cámara")
        }

        OutlinedTextField(
            value = estado.codigo,
            onValueChange = { viewModel.onCodigoChange(it) },
            label = { Text("Código corto o texto del QR") },
            isError = estado.error != null,
            trailingIcon = { if (estado.error != null) IconoDeError() },
            supportingText = { estado.error?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth().sacudirSiHayError(estado.error)
        )
        BotonContorno(
            onClick = { viewModel.validarEscrito() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Validar código")
        }

        estado.resultado?.let { resultado ->
            val colores = JuntaTheme.colores
            val colorFondo = when (resultado.estado) {
                EstadoIngreso.VALIDO -> colores.confirmadaContenedor
                EstadoIngreso.FUERA_DE_HORARIO -> colores.avisoContenedor
                EstadoIngreso.NO_VALIDO -> colores.rechazadaContenedor
            }
            val colorTexto = when (resultado.estado) {
                EstadoIngreso.VALIDO -> colores.sobreConfirmada
                EstadoIngreso.FUERA_DE_HORARIO -> colores.sobreAviso
                EstadoIngreso.NO_VALIDO -> colores.sobreRechazada
            }
            val icono = when (resultado.estado) {
                EstadoIngreso.VALIDO -> R.drawable.ic_escaner_permitido
                EstadoIngreso.FUERA_DE_HORARIO -> R.drawable.ic_escaner_horario
                EstadoIngreso.NO_VALIDO -> R.drawable.ic_escaner_no_valido
            }
            val titulo = when (resultado.estado) {
                EstadoIngreso.VALIDO -> "Acceso permitido"
                EstadoIngreso.FUERA_DE_HORARIO -> "Fuera de horario"
                EstadoIngreso.NO_VALIDO -> "Código no válido"
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorFondo,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(icono),
                            contentDescription = null,
                            tint = colorTexto,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = titulo,
                            style = MaterialTheme.typography.titleLarge,
                            color = colorTexto,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val datos = resultado.datos
                    if (datos != null) {
                        Text("Vecino: ${datos.nombreVecino}")
                        Text("Sala: ${dependencias.firstOrNull { it.id == datos.dependenciaId }?.nombre ?: ""}")
                        Text("${fechaParaMostrar(datos.fecha)}, de ${datos.horaInicio} a ${datos.horaFin}")
                    } else {
                        Text(resultado.motivo ?: "No es un QR de la junta o fue alterado.")
                    }
                }
            }
        }
    }
}