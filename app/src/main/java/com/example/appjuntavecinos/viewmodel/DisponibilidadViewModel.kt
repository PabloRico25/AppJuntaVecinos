package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.UsuarioEntity
import com.example.appjuntavecinos.data.remote.mensajeDeError
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.BloqueHorario
import com.example.appjuntavecinos.utils.calcularBloques
import com.example.appjuntavecinos.utils.fechaDeHoy
import com.example.appjuntavecinos.utils.fechaDesdeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DisponibilidadViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias.asStateFlow()

    private val _usuarios = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    val usuarios: StateFlow<List<UsuarioEntity>> = _usuarios.asStateFlow()

    private val _dependenciaId = MutableStateFlow(0)
    val dependenciaId: StateFlow<Int> = _dependenciaId.asStateFlow()

    private val _fecha = MutableStateFlow(fechaDeHoy())
    val fecha: StateFlow<String> = _fecha.asStateFlow()

    private val _bloques = MutableStateFlow<List<BloqueHorario>>(emptyList())
    val bloques: StateFlow<List<BloqueHorario>> = _bloques.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // La carga de bloques que está en curso; si el usuario cambia de sala o de fecha, se cancela
    private var cargaEnCurso: Job? = null

    init {
        viewModelScope.launch {
            val lista = repositorio.obtenerDependencias()
            _dependencias.value = lista
            _usuarios.value = repositorio.obtenerUsuarios()
            if (lista.isNotEmpty()) {
                _dependenciaId.value = lista.first().id
                cargarBloques()
            } else {
                _error.value = "No se pudieron cargar las salas. Revisa tu conexión"
            }
        }
    }

    fun onDependenciaChange(id: Int) {
        _dependenciaId.value = id
        cargarBloques()
    }

    fun onFechaChange(millis: Long?) {
        if (millis == null) return
        _fecha.value = fechaDesdeMillis(millis)
        cargarBloques()
    }

    // Pide al servidor los bloques ocupados de la sala y el día elegidos y arma los tramos libres
    private fun cargarBloques() {
        val dependenciaId = _dependenciaId.value
        val fecha = _fecha.value

        cargaEnCurso?.cancel()
        cargaEnCurso = viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            try {
                val ocupados = repositorio.obtenerBloquesOcupados(dependenciaId, fecha)
                _bloques.value = calcularBloques(ocupados)
            } catch (e: Exception) {
                // Si la carga se canceló porque el usuario cambió de sala o de fecha, no es un error
                currentCoroutineContext().ensureActive()
                // Se vacían los bloques: mostrar los de otra sala o fecha como si fueran los actuales engañaría
                _bloques.value = emptyList()
                _error.value = mensajeDeError(e)
            }
            _cargando.value = false
        }
    }
}