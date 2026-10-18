package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.repository.JuntaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MisReservasViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias.asStateFlow()

    val reservasPagadas: Flow<List<Int>> = repositorio.observarReservasPagadas()

    init {
        viewModelScope.launch {
            _dependencias.value = repositorio.obtenerDependencias()
        }
    }

    fun reservas(vecinoId: Int): Flow<List<ReservaEntity>> {
        return repositorio.observarReservas(vecinoId)
    }

    fun cancelar(reserva: ReservaEntity) {
        if (reserva.estado != "PENDIENTE") return

        viewModelScope.launch {
            repositorio.cancelarReserva(reserva)
        }
    }
}