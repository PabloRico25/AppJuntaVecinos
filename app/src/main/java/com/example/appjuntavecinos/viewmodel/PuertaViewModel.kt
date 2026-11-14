package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.IngresoDto
import com.example.appjuntavecinos.data.remote.SesionToken
import com.example.appjuntavecinos.data.remote.mensajeDeError
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.data.repository.PuertaPreferencias
import com.example.appjuntavecinos.utils.EstadoPuerta
import com.example.appjuntavecinos.utils.MotivoPuerta
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
import java.io.IOException

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

    private val _activando = MutableStateFlow(false)
    val activando: StateFlow<Boolean> = _activando.asStateFlow()

    private val _errorActivacion = MutableStateFlow<String?>(null)
    val errorActivacion: StateFlow<String?> = _errorActivacion.asStateFlow()

    // mientras se está validando un QR. El lector de la cámara puede entregar el
    // mismo QR varias veces seguidas, y así solo se atiende la primera.
    private var procesando = false

    // Lo mismo, pero visible para la pantalla: mientras es true, la puerta muestra el indicador de carga
    private val _validando = MutableStateFlow(false)
    val validando: StateFlow<Boolean> = _validando.asStateFlow()

    init {
        // Si esta puerta ya estaba activada, se recupera el token de 7 días guardado:
        // nadie inicia sesión en una puerta, así que sin esto no podría hablar con el servidor.
        viewModelScope.launch {
            val guardado = preferencias.token.first()
            if (guardado.isNotEmpty()) {
                SesionToken.token = guardado
            }
        }
    }

    fun cargarDependencias() {
        viewModelScope.launch {
            _dependencias.value = repositorio.obtenerDependencias()
        }
    }

    // El servidor decide si se puede: solo la directiva activa una puerta (si no, responde 403).
    // La clave se guarda con hash y sirve después para salir del modo puerta.
    fun activar(salaId: Int, correo: String, clave: String, onListo: () -> Unit) {
        viewModelScope.launch {
            _activando.value = true
            _errorActivacion.value = null
            try {
                val token = repositorio.activarPuerta(correo, clave)
                SesionToken.token = token
                preferencias.activar(salaId, hashClave(clave), token)
                onListo()
            } catch (e: Exception) {
                _errorActivacion.value = mensajeDeError(e)
            } finally {
                _activando.value = false
            }
        }
    }

    fun limpiarErrorActivacion() {
        _errorActivacion.value = null
    }

    fun desactivar(clave: String, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val guardada = preferencias.claveHash.first()
            if (guardada.isNotEmpty() && hashClave(clave) == guardada) {
                preferencias.desactivar()
                SesionToken.token = null
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
        _validando.value = true

        viewModelScope.launch {
            try {
                // Los accesos los lleva el servidor. Si no hay conexión o el token venció, la puerta no abre.
                val usadas = repositorio.obtenerReservasUsadas()
                val resultado = evaluarPuerta(texto, salaId.value, fechaDeHoy(), horaActual(), usadas)
                val reservaId = resultado.reservaId
                // Si la puerta se abre, se guarda que esa reserva ya entró.
                if (resultado.estado == EstadoPuerta.ABIERTA && reservaId != null) {
                    repositorio.registrarIngreso(IngresoDto(reservaId, fechaDeHoy(), horaActual()))
                }
                _resultado.value = resultado
            } catch (e: Exception) {
                // Sin red no se puede saber si el código ya se usó, así que la puerta no abre
                val motivo = if (e is IOException) MotivoPuerta.SIN_CONEXION else MotivoPuerta.ERROR_SERVIDOR
                _resultado.value = ResultadoPuerta(EstadoPuerta.CERRADA, mensajeDeError(e), motivo = motivo)
            } finally {
                // Pase lo que pase, se libera para poder atender el siguiente QR.
                procesando = false
                _validando.value = false
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