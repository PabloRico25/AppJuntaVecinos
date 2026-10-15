package com.example.appjuntavecinos.data.model

data class RegistrarPagoUiState(
    val tipo: String = "",
    val reserva: ReservaEntity? = null,
    val categoria: String = "",
    val monto: String = "",
    val fecha: String = "",
    val descripcion: String = "",
    val adjuntarComprobante: Boolean = false,
    val comprobante: String? = null,
    val confirma: Boolean = false,
    val errores: RegistrarPagoErrores = RegistrarPagoErrores()
)

data class RegistrarPagoErrores(
    val tipo: String? = null,
    val reserva: String? = null,
    val categoria: String? = null,
    val monto: String? = null,
    val fecha: String? = null,
    val comprobante: String? = null,
    val confirma: String? = null,
    val general: String? = null
)