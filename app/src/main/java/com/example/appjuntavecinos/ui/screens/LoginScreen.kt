package com.example.appjuntavecinos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.ui.components.ActivarPuertaDialog
import com.example.appjuntavecinos.ui.components.IconoDeError
import com.example.appjuntavecinos.viewmodel.LoginViewModel
import com.example.appjuntavecinos.viewmodel.PuertaViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    puertaViewModel: PuertaViewModel,
    onLoginValido: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val dependencias by puertaViewModel.dependencias.collectAsState()
    var toques by remember { mutableIntStateOf(0) }
    var ultimoToque by remember { mutableLongStateOf(0L) }
    var mostrarActivacion by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Inicia sesión",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                val ahora = System.currentTimeMillis()
                toques = if (ahora - ultimoToque > 2000) 1 else toques + 1
                ultimoToque = ahora
                if (toques >= 7) {
                    toques = 0
                    puertaViewModel.cargarDependencias()
                    mostrarActivacion = true
                }
            }
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = estado.correo,
            onValueChange = { viewModel.onCorreoChange(it) },
            label = { Text("Correo") },
            singleLine = true,
            isError = estado.errores.correo != null,
            supportingText = { estado.errores.correo?.let { Text(it) } },
            trailingIcon = { if (estado.errores.correo != null) IconoDeError() },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = estado.clave,
            onValueChange = { viewModel.onClaveChange(it) },
            label = { Text("Clave") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = estado.errores.clave != null,
            supportingText = { estado.errores.clave?.let { Text(it) } },
            trailingIcon = { if (estado.errores.clave != null) IconoDeError() },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        estado.errores.general?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.iniciarSesion(onLoginValido) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }
    }


    if (mostrarActivacion) {
        ActivarPuertaDialog(
            dependencias = dependencias,
            onActivar = { salaId, clave ->
                puertaViewModel.activar(salaId, clave)
                mostrarActivacion = false
            },
            onCancelar = { mostrarActivacion = false }
        )
    }
}