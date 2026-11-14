package com.example.appjuntavecinos.data.model

// Lo que la app envía al iniciar sesión. "dispositivo" solo se usa para activar la puerta ("PUERTA").
data class LoginRequest(
    val correo: String,
    val clave: String,
    val dispositivo: String? = null
)