package com.example.appjuntavecinos.data.model

// Forma de todos los errores del servidor. Lo que se muestra al usuario es "message".
data class ErrorApi(
    val status: Int = 0,
    val message: String = ""
)