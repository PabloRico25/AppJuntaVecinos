package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.EscanearUiState
import com.example.appjuntavecinos.data.model.ResultadoEscaneo
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.EstadoIngreso
import com.example.appjuntavecinos.utils.evaluarIngreso
import com.example.appjuntavecinos.utils.fechaDeHoy
import com.example.appjuntavecinos.utils.horaActual
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
            _estado.update { it.copy(error = "Escribe o pega el código del QR") }
            return
        }
        validar(texto)
    }

    fun validar(texto: String) {
        val datos = leerContenidoQr(texto)
        val resultado = if (datos == null) {
            ResultadoEscaneo(EstadoIngreso.NO_VALIDO, null)
        } else {
            ResultadoEscaneo(evaluarIngreso(datos, fechaDeHoy(), horaActual()), datos)
        }
        _estado.update { it.copy(resultado = resultado, error = null) }
    }

    fun avisarError(mensaje: String) {
        _estado.update { it.copy(error = mensaje) }
    }
}