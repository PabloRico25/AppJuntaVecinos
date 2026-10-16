package com.example.appjuntavecinos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.appjuntavecinos.ui.components.LectorQrFrontal
import com.example.appjuntavecinos.ui.theme.ErrorRojo
import com.example.appjuntavecinos.ui.theme.Green
import com.example.appjuntavecinos.ui.theme.TextoPrincipal
import com.example.appjuntavecinos.utils.EstadoPuerta
import com.example.appjuntavecinos.viewmodel.PuertaViewModel
import kotlinx.coroutines.delay

// Pantalla del modo puerta: muestra la cámara frontal y lee los QR sola.
// Solo se sale con el gesto escondido (7 toques al título) y la clave elegida al activarla.
@Composable
fun PuertaScreen(
    viewModel: PuertaViewModel
) {
    val salaId by viewModel.salaId.collectAsState()
    val dependencias by viewModel.dependencias.collectAsState()
    val resultado by viewModel.resultado.collectAsState()
    val contexto = LocalContext.current
    val vista = LocalView.current

    // Estado del gesto de salida: cuántos toques van y cuándo fue el último
    var toques by remember { mutableIntStateOf(0) }
    var ultimoToque by remember { mutableLongStateOf(0L) }
    var mostrarSalida by remember { mutableStateOf(false) }
    var claveSalida by remember { mutableStateOf("") }
    var errorSalida by remember { mutableStateOf<String?>(null) }

    // Permiso de la cámara: se pregunta al sistema si ya está concedido
    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    // El lanzador que muestra el cuadro de permiso de Android (como en Registrar pago)
    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermiso = concedido
    }

    // Al abrir la pantalla: se cargan los nombres de las salas y, si falta, se pide el permiso
    LaunchedEffect(Unit) {
        viewModel.cargarDependencias()
        if (!tienePermiso) {
            pedirPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    // La pantalla de la puerta no se apaga mientras está abierta
    DisposableEffect(Unit) {
        vista.keepScreenOn = true
        onDispose { vista.keepScreenOn = false }
    }

    // El botón atrás no hace nada: solo se sale con el gesto y la clave
    BackHandler(enabled = true) { }

    // El resultado se quita solo a los 4 segundos
    LaunchedEffect(resultado) {
        if (resultado != null) {
            delay(4000)
            viewModel.limpiarResultado()
        }
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // El título es el gesto escondido para salir: 7 toques seguidos
            Text(
                text = "Acerque su QR",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    val ahora = System.currentTimeMillis()
                    // Si pasaron más de 2 segundos desde el último toque, se cuenta de nuevo
                    toques = if (ahora - ultimoToque > 2000) 1 else toques + 1
                    ultimoToque = ahora
                    if (toques >= 7) {
                        toques = 0
                        claveSalida = ""
                        errorSalida = null
                        mostrarSalida = true
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Puerta: " + (dependencias.firstOrNull { it.id == salaId }?.nombre ?: ""),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(24.dp))

            if (tienePermiso) {
                // La cámara frontal, en un recuadro con esquinas redondeadas.
                // activo = resultado == null: mientras se muestra un resultado, no se lee.
                LectorQrFrontal(
                    activo = resultado == null,
                    onQrLeido = { texto -> viewModel.validar(texto) },
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                // Sin permiso no hay cámara: se explica y se ofrece pedirlo de nuevo
                Text("La puerta necesita la cámara para leer los códigos QR.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { pedirPermiso.launch(Manifest.permission.CAMERA) }) {
                    Text("Dar permiso de cámara")
                }
            }
        }

        // El resultado cubre toda la pantalla: verde si abre, rojo si no
        resultado?.let { r ->
            val abierta = r.estado == EstadoPuerta.ABIERTA
            val colorTexto = if (abierta) TextoPrincipal else Color.White
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (abierta) Green else ErrorRojo)
                    .clickable { viewModel.limpiarResultado() }
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (abierta) "Puerta abierta" else "Puerta cerrada",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = r.mensaje,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorTexto
                )
            }
        }
    }

    // Diálogo para salir del modo puerta: pide la clave elegida al activarlo
    if (mostrarSalida) {
        AlertDialog(
            onDismissRequest = { mostrarSalida = false },
            title = { Text("Salir del modo puerta") },
            text = {
                Column {
                    Text("Escribe la clave que se eligió al activarlo.")
                    OutlinedTextField(
                        value = claveSalida,
                        onValueChange = {
                            claveSalida = it
                            errorSalida = null
                        },
                        label = { Text("Clave") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    errorSalida?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.desactivar(claveSalida) { correcta ->
                            if (correcta) {
                                mostrarSalida = false
                            } else {
                                errorSalida = "Clave incorrecta"
                            }
                        }
                    }
                ) {
                    Text("Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSalida = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}