package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
    activando: Boolean,
    errorServidor: String?,
    onActivar: (salaId: Int, correo: String, clave: String) -> Unit,
    onCancelar: () -> Unit
) {
    var sala by remember { mutableStateOf<DependenciaComunitariaEntity?>(null) }
    var correo by remember { mutableStateOf("") }
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
                    value = correo,
                    onValueChange = {
                        correo = it
                        error = null
                    },
                    label = { Text("Correo de la directiva") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = clave,
                    onValueChange = {
                        clave = it
                        error = null
                    },
                    label = { Text("Clave (también sirve para salir)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                // El error de esta pantalla (falta un dato) o el que responde el servidor
                (error ?: errorServidor)?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            BotonTexto(
                enabled = !activando,
                onClick = {
                    val elegida = sala
                    when {
                        elegida == null -> error = "Elige la sala de esta puerta"
                        correo.isBlank() -> error = "Escribe el correo de la directiva"
                        clave.length < 4 -> error = "La clave debe tener al menos 4 caracteres"
                        else -> onActivar(elegida.id, correo.trim(), clave)
                    }
                }
            ) {
                Text(if (activando) "Activando..." else "Activar")
            }
        },
        dismissButton = {
            BotonTexto(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}