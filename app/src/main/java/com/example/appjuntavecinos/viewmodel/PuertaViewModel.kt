package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.PuertaErrores
import com.example.appjuntavecinos.data.repository.*
import com.example.appjuntavecinos.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PuertaViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencias = PuertaPreferencias(application)
    private val repositorio = JuntaRepository(application)
    private val _configuracion = MutableStateFlow(ConfiguracionPuerta())
    val configuracion: StateFlow<ConfiguracionPuerta> = _configuracion
    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val _errores = MutableStateFlow(PuertaErrores())
    val errores: StateFlow<PuertaErrores> = _errores
    private val _resultado = MutableStateFlow<ResultadoPuerta?>(null)
    val resultado: StateFlow<ResultadoPuerta?> = _resultado
    private val _procesando = MutableStateFlow(false)
    val procesando: StateFlow<Boolean> = _procesando
    private var leyendo = false
    private var ultimoCodigo: String? = null

    init {
        viewModelScope.launch {
            try {
                repositorio.cargarDatosDeEjemplo()
                _dependencias.value = repositorio.obtenerDependencias()
                preferencias.configuracion.collect { _configuracion.value = it }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _error.value = "No se pudo abrir la configuración. Cierra y vuelve a abrir la app."
            }
        }
    }
    fun limpiarError() { _error.value = null; _errores.value = PuertaErrores() }
    fun activar(salaId: Int?, clave: String, onExito: () -> Unit) {
        if (_procesando.value) return
        _errores.value = PuertaErrores(
            sala = if (_dependencias.value.none { it.id == salaId }) "Elige la sala de esta puerta" else null,
            clave = if (clave.length < 4 || clave != clave.trim()) "Usa una clave de al menos 4 caracteres, sin espacios al inicio o final" else null
        )
        if (_errores.value != PuertaErrores()) return
        _procesando.value = true
        viewModelScope.launch {
            try {
                preferencias.activar(salaId!!, hashClave(clave))
                _error.value = null; onExito()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _error.value = "No se pudo activar el modo puerta"
            } finally { _procesando.value = false }
        }
    }
    fun desactivar(clave: String, onExito: () -> Unit) {
        if (_procesando.value) return
        if (hashClave(clave) != _configuracion.value.claveHash) {
            _errores.value = PuertaErrores(clave = "Clave incorrecta"); return
        }
        _procesando.value = true
        viewModelScope.launch {
            try {
                preferencias.desactivar()
                _error.value = null; _resultado.value = null; ultimoCodigo = null; onExito()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _error.value = "No se pudo salir del modo puerta"
            } finally { _procesando.value = false }
        }
    }
    fun validar(texto: String?) {
        if (texto == null) { ultimoCodigo = null; return }
        if (leyendo || !_configuracion.value.modoPuerta || texto == ultimoCodigo) return
        ultimoCodigo = texto
        leyendo = true
        viewModelScope.launch {
            try {
                _resultado.value = repositorio.registrarIngreso(texto, _configuracion.value.salaId)
                delay(4000)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _resultado.value = ResultadoPuerta("CERRADA", "No se pudo registrar el ingreso")
                delay(4000)
            } finally { _resultado.value = null; leyendo = false }
        }
    }
}
