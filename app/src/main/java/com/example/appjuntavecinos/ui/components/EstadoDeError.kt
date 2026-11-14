package com.example.appjuntavecinos.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.appjuntavecinos.R
import com.example.appjuntavecinos.utils.partirMensaje

// El estado completo de un error, armado a partir del mensaje que ya produce la app: el ícono se elige según
// de qué trata (conexión, sesión, servidor) y el mensaje se parte en título y explicación.
// Sin onReintentar no lleva botón.
@Composable
fun EstadoDeError(
    mensaje: String,
    modifier: Modifier = Modifier,
    textoBoton: String = "Reintentar",
    iconoBoton: Int? = R.drawable.ic_reintentar,
    onReintentar: (() -> Unit)? = null
) {
    val (titulo, detalle) = partirMensaje(mensaje)
    val icono = when {
        mensaje.contains("conectar") -> R.drawable.ic_nube_tachada
        mensaje.contains("sesión") -> R.drawable.ic_sesion_vencida
        mensaje.contains("servidor") -> R.drawable.ic_servidor
        else -> R.drawable.ic_alerta
    }
    val sinConexion = mensaje.contains("conectar")
    EstadoCompleto(
        icono = { if (sinConexion) IlustracionSinConexion() else IconoGrande(icono) },
        titulo = titulo,
        modifier = modifier,
        mensaje = detalle,
        textoBoton = if (onReintentar != null) textoBoton else null,
        iconoBoton = iconoBoton,
        circulo = !sinConexion,
        onBoton = onReintentar
    )
}
