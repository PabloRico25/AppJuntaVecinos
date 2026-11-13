package com.example.appjuntavecinos.data.remote

// Guarda en memoria el token de la sesión actual. Se llena al iniciar sesión (B3) y se vacía al cerrarla.
object SesionToken {
    @Volatile
    var token: String? = null
}