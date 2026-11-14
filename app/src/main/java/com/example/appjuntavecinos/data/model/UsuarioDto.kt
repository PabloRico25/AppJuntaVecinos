package com.example.appjuntavecinos.data.model

// El usuario tal como lo entrega el servidor: nunca incluye la clave de nadie.
data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: String,
    val cargo: String
)