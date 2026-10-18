package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.UsuarioEntity
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.Franja
import com.example.appjuntavecinos.utils.calcularFranjas
import com.example.appjuntavecinos.utils.fechaDeHoy
import com.example.appjuntavecinos.utils.fechaDesdeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    // No visto en clase (lo integramos con permiso del docente): combine junta varios Flow en uno.
    val franjas: Flow<List<Franja>> = combine(
        repositorio.observarTodasLasReservas(),
        _dependenciaId,
        _fecha
    ) { reservas, dependenciaId, fecha ->
        calcularFranjas(reservas, dependenciaId, fecha)
    }

    init {
        viewModelScope.launch {
            val lista = repositorio.obtenerDependencias()
            _dependencias.value = lista
            _usuarios.value = repositorio.obtenerUsuarios()
            if (lista.isNotEmpty()) {
                _dependenciaId.value = lista.first().id
            }
        }
    }

    fun onDependenciaChange(id: Int) {
        _dependenciaId.value = id
    }

    fun onFechaChange(millis: Long?) {
        if (millis == null) return
        _fecha.value = fechaDesdeMillis(millis)
    }
}