package com.example.appjuntavecinos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

// Un campo con su botón al lado (por ejemplo, la fecha y "Elegir fecha"). Cuando la persona tiene la letra del teléfono
// muy grande, el botón se come el ancho y el campo queda tan angosto que parte las palabras letra por letra. En ese
// caso el botón pasa debajo del campo, a todo el ancho. Cada pieza recibe el Modifier que le toca.
// No visto en clase: LocalDensity.fontScale es el tamaño de letra que eligió la persona en los ajustes del teléfono.
@Composable
fun FilaCampoYBoton(
    campo: @Composable (Modifier) -> Unit,
    boton: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    if (LocalDensity.current.fontScale > 1.3f) {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            campo(Modifier.fillMaxWidth())
            boton(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            campo(Modifier.weight(1f))
            boton(Modifier.padding(top = 8.dp))
        }
    }
}
