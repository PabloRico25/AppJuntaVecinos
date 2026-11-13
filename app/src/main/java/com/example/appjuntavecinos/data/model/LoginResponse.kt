package com.example.appjuntavecinos.data.model

data class LoginResponse(
    val token: String,
    val usuario: UsuarioDto
)