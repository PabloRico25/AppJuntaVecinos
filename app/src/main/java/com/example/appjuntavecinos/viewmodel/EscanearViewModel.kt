package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.EscanearUiState
import com.example.appjuntavecinos.data.model.ResultadoEscaneo
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.DatosQr
import com.example.appjuntavecinos.utils.EstadoIngreso
import com.example.appjuntavecinos.utils.codigoCortoEsValido
import com.example.appjuntavecinos.utils.evaluarIngreso
import com.example.appjuntavecinos.utils.fechaDeHoy
import com.example.appjuntavecinos.utils.horaActual
import com.example.appjuntavecinos.utils.leerCodigoCorto
import com.example.appjuntavecinos.utils.leerContenidoQr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EscanearViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    private val _estado = MutableStateFlow(EscanearUiState())
    val estado: StateFlow<EscanearUiState> = _estado.asStateFlow()

    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias.asStateFlow()

    init {
        viewModelScope.launch {
            _dependencias.value = repositorio.obtenerDependencias()
        }
    }

    fun onCodigoChange(valor: String) {
        _estado.update { it.copy(codigo = valor, error = null) }
    }

    fun validarEscrito() {
        val texto = _estado.value.codigo
        if (texto.isBlank()) {
            _estado.update { it.copy(error = "Escribe o pega el código") }
            return
        }
        validar(texto)
    }

    fun validar(texto: String) {
        if (texto.trim().startsWith("JV1|")) {
            validarQr(texto)
        } else {
            validarCodigoCorto(texto)
        }
    }

    fun avisarError(mensaje: String) {
        _estado.update { it.copy(error = mensaje) }
    }

    private fun validarQr(texto: String) {
        val datos = leerContenidoQr(texto)
        if (datos == null) {
            mostrar(noValido("No es un QR de la junta o fue alterado."))
            return
        }
        mostrar(ResultadoEscaneo(evaluarIngreso(datos, fechaDeHoy(), horaActual()), datos))
    }

    private fun validarCodigoCorto(texto: String) {
        val corto = leerCodigoCorto(texto)
        if (corto == null) {
            mostrar(noValido("No tiene la forma de un QR ni de un código corto."))
            return
        }

        viewModelScope.launch {
            val reserva = repositorio.buscarReserva(corto.reservaId)
            val resultado = when {
                reserva == null -> noValido("Esa reserva no existe en este equipo. Usa el QR.")
                !codigoCortoEsValido(reserva, corto.firma) -> noValido("El código no calza con la reserva.")
                reserva.estado != "CONFIRMADA" -> noValido("La reserva no está confirmada.")
                !repositorio.reservaPagada(reserva.id) -> noValido("La reserva no tiene el pago de arriendo registrado.")
                else -> {
                    val nombre = repositorio.obtenerUsuarios()
                        .firstOrNull { it.id == reserva.vecinoId }?.nombre ?: ""
                    val datos = DatosQr(
                        reserva.id,
                        reserva.dependenciaId,
                        reserva.fecha,
                        reserva.horaInicio,
                        reserva.horaFin,
                        nombre
                    )
                    ResultadoEscaneo(evaluarIngreso(datos, fechaDeHoy(), horaActual()), datos)
                }
            }
            mostrar(resultado)
        }
    }

    private fun noValido(motivo: String): ResultadoEscaneo {
        return ResultadoEscaneo(EstadoIngreso.NO_VALIDO, null, motivo)
    }

    private fun mostrar(resultado: ResultadoEscaneo) {
        _estado.update { it.copy(resultado = resultado, error = null) }
    }
}