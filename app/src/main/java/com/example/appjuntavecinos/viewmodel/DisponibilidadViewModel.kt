package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.data.repository.horariosSeCruzan
import com.example.appjuntavecinos.utils.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

fun calcularFranjas(reservas: List<ReservaEntity>): List<FranjaDisponibilidad> =
    (9 until 22).map { hora ->
        val inicio = "%02d:00".format(hora)
        val fin = "%02d:00".format(hora + 1)
        val coinciden = reservas.filter {
            it.estado in listOf("CONFIRMADA", "PENDIENTE") &&
                horariosSeCruzan(it.horaInicio, it.horaFin, inicio, fin)
        }
        FranjaDisponibilidad(inicio, fin, when {
            coinciden.any { it.estado == "CONFIRMADA" } -> "OCUPADO"
            coinciden.any { it.estado == "PENDIENTE" } -> "SOLICITADO"
            else -> "LIBRE"
        }, coinciden)
    }

class DisponibilidadViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = JuntaRepository(application)
    private val _seleccion = MutableStateFlow(DisponibilidadUiState())
    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias
    private val _usuarios = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    val usuarios: StateFlow<List<UsuarioEntity>> = _usuarios

    val estado = combine(_seleccion, repositorio.observarTodasLasReservas()) { seleccion, reservas ->
        seleccion.copy(franjas = if (seleccion.dependencia == null) emptyList() else calcularFranjas(
            reservas.filter { it.dependenciaId == seleccion.dependencia.id && it.fecha == seleccion.fecha }
        ))
    }.catch { emit(_seleccion.value.copy(error = "No se pudo consultar la disponibilidad")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DisponibilidadUiState())

    init {
        viewModelScope.launch {
            try {
                _dependencias.value = repositorio.obtenerDependencias()
                _usuarios.value = repositorio.obtenerUsuarios()
                _seleccion.update { it.copy(dependencia = _dependencias.value.firstOrNull()) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _seleccion.update { it.copy(error = "No se pudieron cargar las dependencias") }
            }
        }
    }
    fun onDependenciaChange(valor: DependenciaComunitariaEntity) { _seleccion.update { it.copy(dependencia = valor) } }
    fun onFechaChange(millis: Long?) {
        if (millis != null) _seleccion.update { it.copy(fecha = fechaDesdeMillis(millis)) }
    }
}
