package com.example.appjuntavecinos.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.appjuntavecinos.ui.components.*
import com.example.appjuntavecinos.ui.theme.*
import com.example.appjuntavecinos.viewmodel.PuertaViewModel

@Composable
fun PuertaScreen(viewModel: PuertaViewModel) {
    val config by viewModel.configuracion.collectAsState()
    val dependencias by viewModel.dependencias.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val error by viewModel.error.collectAsState()
    val errores by viewModel.errores.collectAsState()
    val procesando by viewModel.procesando.collectAsState()
    val context = LocalContext.current
    val vista = LocalView.current
    var permisoCamara by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var errorCamara by remember { mutableStateOf<String?>(null) }
    var salir by rememberSaveable { mutableStateOf(false) }
    var clave by rememberSaveable { mutableStateOf("") }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permisoCamara = it
        errorCamara = if (it) null else "Se necesita permiso de cámara para leer el QR."
    }
    DisposableEffect(vista) {
        val antes = vista.keepScreenOn
        vista.keepScreenOn = true
        onDispose { vista.keepScreenOn = antes }
    }
    BackHandler { salir = true; viewModel.limpiarError() }
    val abierta = resultado?.estado == "ABIERTA"
    Box(Modifier.fillMaxSize().background(
        if (resultado == null) MaterialTheme.colorScheme.background else if (abierta) Green else Rechazada)) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            TituloConSieteToques("Acerque su QR") { salir = true; viewModel.limpiarError() }
            Text(dependencias.find { it.id == config.salaId }?.nombre.orEmpty(), style = MaterialTheme.typography.titleLarge)
            if (permisoCamara) {
                LectorQrFrontal(Modifier.weight(1f).fillMaxWidth(), onCodigo = { if (!salir) viewModel.validar(it) },
                    onError = { errorCamara = it })
            } else {
                Spacer(Modifier.weight(1f))
                Button(onClick = {
                    permisoCamara = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    if (!permisoCamara) permiso.launch(Manifest.permission.CAMERA)
                }) { Text("Permitir cámara") }
                TextButton(colors = coloresBotonTexto(), onClick = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }) {
                    Text("Abrir ajustes de permisos")
                }
                Spacer(Modifier.weight(1f))
            }
            MensajeError(errorCamara)
            Text("Puerta simulada · Un ingreso por ticket en este aparato", style = MaterialTheme.typography.bodySmall)
        }
        resultado?.let {
            Column(Modifier.fillMaxSize().background(if (abierta) Green else Rechazada).safeDrawingPadding().padding(32.dp),
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (abierta) "✓" else "×", style = MaterialTheme.typography.displayLarge, color = TextoPrincipal)
                Text(it.mensaje, style = MaterialTheme.typography.headlineLarge, color = TextoPrincipal)
                it.datos?.let { datos -> Text(datos.nombreVecino, style = MaterialTheme.typography.titleLarge, color = TextoPrincipal) }
            }
        }
    }
    if (salir) AlertDialog(onDismissRequest = { if (!procesando) { salir = false; clave = "" } },
        title = { Text("Salir del modo puerta") },
        text = {
            Column {
                OutlinedTextField(value = clave, onValueChange = { clave = it; viewModel.limpiarError() },
                    colors = coloresCampo(),
                    label = { Text("Clave de la puerta") }, singleLine = true, enabled = !procesando,
                    isError = errores.clave != null,
                    supportingText = { errores.clave?.let { Text(it) } },
                    trailingIcon = { if (errores.clave != null) Icon(Icons.Default.Warning, "Error") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password,
                        autoCorrectEnabled = false, capitalization = KeyboardCapitalization.None))
                MensajeError(error)
            }
        },
        confirmButton = { TextButton(colors = coloresBotonTexto(), onClick = { viewModel.desactivar(clave) { salir = false; clave = "" } }, enabled = !procesando) { Text("Salir") } },
        dismissButton = { TextButton(colors = coloresBotonTexto(), onClick = { salir = false; clave = "" }, enabled = !procesando) { Text("Cancelar") } })
}
