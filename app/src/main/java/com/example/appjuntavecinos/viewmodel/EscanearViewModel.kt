package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EscanearViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = JuntaRepository(application)
    private val _estado = MutableStateFlow(EscanearUiState())
    val estado: StateFlow<EscanearUiState> = _estado
    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias
    init {
        viewModelScope.launch {
            try { _dependencias.value = repositorio.obtenerDependencias() }
            catch (e: Exception) {
                if (e is CancellationException) throw e
                avisarError("No se pudieron cargar las dependencias")
            }
        }
    }
    fun onCodigoChange(valor: String) {
        if (!_estado.value.validando) _estado.update { it.copy(codigo = valor.take(2000), error = null, resultado = null) }
    }
    fun onDependenciaChange(id: Int) {
        if (!_estado.value.validando) _estado.update { it.copy(dependenciaId = id, error = null, resultado = null) }
    }
    fun avisarError(mensaje: String) { _estado.update { it.copy(error = mensaje, resultado = null) } }
    fun validarEscrito() = validar(_estado.value.codigo)
    fun validar(texto: String) {
        if (_estado.value.validando) return
        val sala = _estado.value.dependenciaId
        if (sala == null) { avisarError("Elige la dependencia donde estás"); return }
        if (texto.isBlank()) { avisarError("Escanea o escribe un código"); return }
        _estado.update { it.copy(validando = true, resultado = null, error = null) }
        viewModelScope.launch {
            try {
                val datos = if (texto.trim().startsWith("JV1|")) leerContenidoQr(texto) else {
                    val corto = leerCodigoCorto(texto)
                    val reserva = corto?.let { repositorio.buscarReserva(it.reservaId) }
                    if (corto != null && reserva != null && reserva.estado == "CONFIRMADA" &&
                        repositorio.reservaPagada(reserva.id) && codigoCortoEsValido(reserva, corto.firma)) {
                        val nombre = repositorio.obtenerUsuarios().find { it.id == reserva.vecinoId }?.nombre.orEmpty()
                        leerContenidoQr(armarContenidoQr(reserva, nombre))
                    } else null
                }
                val resultado = when {
                    datos == null -> ResultadoEscaneo(EstadoIngreso.NO_VALIDO, null, "Código no válido o no disponible en este dispositivo")
                    datos.dependenciaId != sala -> ResultadoEscaneo(EstadoIngreso.NO_VALIDO, datos, "El ticket es de otra dependencia")
                    else -> ResultadoEscaneo(evaluarIngreso(datos, fechaDeHoy(), horaActual()), datos)
                }
                _estado.update { it.copy(resultado = resultado) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                avisarError("No se pudo comprobar el código")
            } finally { _estado.update { it.copy(validando = false) } }
        }
    }
}
