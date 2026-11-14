package com.example.appjuntavecinos.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// Una lista que ya estaba abierta no se entera sola de lo que cambió en el servidor (por ejemplo, que la Secretaría
// confirmó una reserva desde otro teléfono). Esto pide los datos cada vez que la pantalla se muestra: al abrirla, al
// regresar a su pestaña y al volver a la app desde otra. Al cambiar de pestaña Compose vuelve a crear la pantalla pero
// conserva su ViewModel, que por eso no puede ser quien cargue los datos: lo hace esta función.
// No visto en clase: LifecycleEventObserver avisa de los cambios del ciclo de vida (aquí, ON_RESUME) a quien lo pide.
@Composable
fun ActualizarAlVolver(accion: () -> Unit) {
    val dueno = LocalLifecycleOwner.current
    val accionActual by rememberUpdatedState(accion)
    DisposableEffect(dueno) {
        val vigilante = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) accionActual()
        }
        dueno.lifecycle.addObserver(vigilante)
        onDispose { dueno.lifecycle.removeObserver(vigilante) }
    }
}
