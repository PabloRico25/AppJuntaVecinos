package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.IngresoEntity
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.data.repository.PuertaPreferencias
import com.example.appjuntavecinos.utils.EstadoPuerta
import com.example.appjuntavecinos.utils.ResultadoPuerta
import com.example.appjuntavecinos.utils.evaluarPuerta
import com.example.appjuntavecinos.utils.fechaDeHoy
import com.example.appjuntavecinos.utils.hashClave
import com.example.appjuntavecinos.utils.horaActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PuertaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)
    private val preferencias = PuertaPreferencias(application)

    // null mientras se lee lo guardado: así no se ve el Login un instante en una puerta
    val modoPuerta: StateFlow<Boolean?> = preferencias.modoPuerta
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val salaId: StateFlow<Int> = preferencias.salaId
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoPuerta?>(null)
    val resultado: StateFlow<ResultadoPuerta?> = _resultado.asStateFlow()

    // mientras se está validando un QR. El lector de la cámara puede entregar el
    // mismo QR varias veces seguidas, y así solo se atiende la primera.
    private var procesando = false

    fun cargarDependencias() {
        viewModelScope.launch {
            _dependencias.value = repositorio.obtenerDependencias()
        }
    }

    fun activar(salaId: Int, clave: String) {
        viewModelScope.launch {
            preferencias.activar(salaId, hashClave(clave))
        }
    }

    fun desactivar(clave: String, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val guardada = preferencias.claveHash.first()
            if (guardada.isNotEmpty() && hashClave(clave) == guardada) {
                preferencias.desactivar()
                onResultado(true)
            } else {
                onResultado(false)
            }
        }
    }

    fun validar(texto: String) {
        // Si ya hay un resultado en pantalla o se está validando otro QR, este se ignora.
        if (procesando || _resultado.value != null) return
        procesando = true

        viewModelScope.launch {
            try {
                val usadas = repositorio.obtenerReservasUsadas()
                val resultado = evaluarPuerta(texto, salaId.value, fechaDeHoy(), horaActual(), usadas)
                val reservaId = resultado.reservaId
                // Si la puerta se abre, se guarda que esa reserva ya entró.
                if (resultado.estado == EstadoPuerta.ABIERTA && reservaId != null) {
                    repositorio.registrarIngreso(IngresoEntity(reservaId, fechaDeHoy(), horaActual()))
                }
                _resultado.value = resultado
            } finally {
                // Pase lo que pase, se libera para poder atender el siguiente QR.
                procesando = false
            }
        }
    }

    fun avisarError(mensaje: String) {
        _resultado.value = ResultadoPuerta(EstadoPuerta.CERRADA, mensaje)
    }

    fun limpiarResultado() {
        _resultado.value = null
    }
}