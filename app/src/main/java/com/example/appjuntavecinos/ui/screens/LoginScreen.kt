package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.data.model.LoginUiState
import com.example.appjuntavecinos.ui.components.*
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme
import com.example.appjuntavecinos.viewmodel.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginValido: () -> Unit, modifier: Modifier = Modifier, onActivarPuerta: () -> Unit = {}) {
    val estado by viewModel.estado.collectAsState()
    LoginFormulario(estado, viewModel::onCorreoChange, viewModel::onClaveChange,
        { viewModel.iniciarSesion(onLoginValido) }, modifier, onActivarPuerta)
}

// Recibe datos y eventos: la Preview puede dibujarlo sin abrir una base de datos.
@Composable
private fun LoginFormulario(
    estado: LoginUiState, onCorreoChange: (String) -> Unit,
    onClaveChange: (String) -> Unit, onIngresar: () -> Unit, modifier: Modifier = Modifier, onActivarPuerta: () -> Unit = {}
) {
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    val foco = LocalFocusManager.current
    ContenidoFormulario(modifier) {
        Spacer(Modifier.height(16.dp))
        Text("Junta de Vecinos Zaror", style = MaterialTheme.typography.headlineSmall)
        TituloConSieteToques("Inicia sesión", onActivarPuerta)
        Text("Entra con tu correo y tu clave. Datos ficticios.", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            colors = coloresCampo(),
            value = estado.correo, onValueChange = onCorreoChange,
            label = { Text("Correo") }, singleLine = true, enabled = !estado.cargando,
            leadingIcon = { Icon(Icons.Default.Email, null) },
            isError = estado.errores.correo != null,
            trailingIcon = { if (estado.errores.correo != null) Icon(Icons.Default.Warning, "Error en correo") },
            supportingText = { estado.errores.correo?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            colors = coloresCampo(),
            value = estado.clave, onValueChange = onClaveChange,
            label = { Text("Clave") }, singleLine = true, enabled = !estado.cargando,
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            isError = estado.errores.clave != null,
            trailingIcon = { if (estado.errores.clave != null) Icon(Icons.Default.Warning, "Error en clave") },
            supportingText = { estado.errores.clave?.let { Text(it) } },
            visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { foco.clearFocus(); onIngresar() }),
            modifier = Modifier.fillMaxWidth()
        )
        Row {
            Checkbox(checked = mostrarClave, onCheckedChange = { mostrarClave = it })
            Text("Mostrar clave", Modifier.padding(top = 12.dp))
        }
        MensajeError(estado.errores.general)
        Button(onClick = { foco.clearFocus(); onIngresar() }, enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(if (estado.cargando) "Cargando…" else "Ingresar")
        }
        if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    AppJuntaVecinosTheme {
        LoginFormulario(LoginUiState(), {}, {}, {})
    }
}
