package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.model.UsuarioEntity
import com.example.appjuntavecinos.data.repository.JuntaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservasDirectivaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    val reservas: Flow<List<ReservaEntity>> = repositorio.observarTodasLasReservas()

    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias.asStateFlow()

    private val _usuarios = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    val usuarios: StateFlow<List<UsuarioEntity>> = _usuarios.asStateFlow()

    init {
        viewModelScope.launch {
            _dependencias.value = repositorio.obtenerDependencias()
            _usuarios.value = repositorio.obtenerUsuarios()
        }
    }

    fun confirmar(
        reserva: ReservaEntity,
        gestionadaPorId: Int,
        puedeConfirmar: Boolean,
        onResultado: (String) -> Unit
    ) {
        if (!puedeConfirmar) {
            onResultado("Solo la Secretaría puede confirmar reservas")
            return
        }
        if (reserva.estado != "PENDIENTE") return

        viewModelScope.launch {
            if (repositorio.confirmarReserva(reserva, gestionadaPorId)) {
                onResultado("Reserva confirmada")
            } else {
                onResultado("No se puede confirmar: ese horario ya está ocupado por una reserva confirmada")
            }
        }
    }

    fun rechazar(
        reserva: ReservaEntity,
        gestionadaPorId: Int,
        puedeConfirmar: Boolean,
        onResultado: (String) -> Unit
    ) {
        if (!puedeConfirmar) {
            onResultado("Solo la Secretaría puede rechazar reservas")
            return
        }
        if (reserva.estado != "PENDIENTE") return

        viewModelScope.launch {
            repositorio.rechazarReserva(reserva, gestionadaPorId)
            onResultado("Reserva rechazada")
        }
    }
}