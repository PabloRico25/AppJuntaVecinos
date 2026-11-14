package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.model.UsuarioEntity
import com.example.appjuntavecinos.data.remote.mensajeDeError
import com.example.appjuntavecinos.data.repository.JuntaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ReservasDirectivaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    val reservas: Flow<List<ReservaEntity>> = repositorio.observarTodasLasReservas()
        .map { lista -> lista.sortedBy { if (it.estado == "PENDIENTE") 0 else 1 } }

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

    // Pide las reservas al servidor. La pantalla lo llama cada vez que se muestra (ver ActualizarAlVolver)
    fun actualizar() {
        viewModelScope.launch {
            repositorio.refrescarReservas()
        }
    }

    // gestionadaPorId ya no se usa: el servidor toma quién confirma desde el token
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
            try {
                repositorio.confirmarReserva(reserva)
                onResultado("Reserva confirmada")
            } catch (e: Exception) {
                onResultado(mensajeDeError(e))
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
            try {
                repositorio.rechazarReserva(reserva)
                onResultado("Reserva rechazada")
            } catch (e: Exception) {
                onResultado(mensajeDeError(e))
            }
        }
    }
}