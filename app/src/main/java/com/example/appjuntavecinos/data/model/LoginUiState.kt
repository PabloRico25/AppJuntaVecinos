package com.example.appjuntavecinos.data.model

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errores: LoginErrores = LoginErrores()
)

data class LoginErrores(val correo: String? = null,val clave: String? = null,
                        val general: String? = null)