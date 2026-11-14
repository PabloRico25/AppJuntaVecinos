package com.example.appjuntavecinos.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// No visto en clase: un SharedFlow es un canal por el que una parte de la app avisa algo y otra lo recibe, sin
// que se conozcan. Aquí el interceptor de red avisa que el servidor rechazó el token (la sesión venció) y la
// navegación, que está en otro lugar, lo recibe y lleva a la persona a iniciar sesión de nuevo.
object EventosSesion {
    private val _vencida = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val vencida: SharedFlow<Unit> = _vencida.asSharedFlow()

    fun avisarVencida() {
        _vencida.tryEmit(Unit)
    }
}
