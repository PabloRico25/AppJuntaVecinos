package com.example.appjuntavecinos.data.model

// reservaId solo en los arriendos; categoria solo en los consumos.
data class NuevoPagoRequest(
    val reservaId: Int? = null,
    val tipo: String,
    val categoria: String = "",
    val monto: Int,
    val fecha: String,
    val comprobanteUri: String? = null,
    val descripcion: String = ""
)