package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity

@Composable
fun ActivarPuertaDialog(
    dependencias: List<DependenciaComunitariaEntity>,
    onActivar: (salaId: Int, clave: String) -> Unit,
    onCancelar: () -> Unit
) {
    var sala by remember { mutableStateOf<DependenciaComunitariaEntity?>(null) }
    var clave by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Activar modo puerta") },
        text = {
            Column {
                MenuDesplegable(
                    etiqueta = "Sala de esta puerta",
                    valor = sala?.nombre ?: "",
                    opciones = dependencias.map { it.nombre },
                    onSeleccion = { sala = dependencias[it] },
                    error = null,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = clave,
                    onValueChange = {
                        clave = it
                        error = null
                    },
                    label = { Text("Clave para salir") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val elegida = sala
                    when {
                        elegida == null -> error = "Elige la sala de esta puerta"
                        clave.length < 4 -> error = "La clave debe tener al menos 4 caracteres"
                        else -> onActivar(elegida.id, clave)
                    }
                }
            ) {
                Text("Activar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}