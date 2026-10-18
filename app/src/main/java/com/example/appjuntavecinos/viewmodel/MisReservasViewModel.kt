package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.data.repository.JuntaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MisReservasViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = JuntaRepository(application)
    val pagadas = repositorio.observarReservasPagadas().catch {
        _mensaje.value = "No se pudo consultar el pago del ticket"
        emit(emptyList())
    }
    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias
    private val _usuarios = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    val usuarios: StateFlow<List<UsuarioEntity>> = _usuarios
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje
    private val _procesando = MutableStateFlow(false)
    val procesando: StateFlow<Boolean> = _procesando

    init {
        viewModelScope.launch {
            try {
                _dependencias.value = repositorio.obtenerDependencias()
                _usuarios.value = repositorio.obtenerUsuarios()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _mensaje.value = "No se pudieron cargar los datos"
            }
        }
    }
    fun reservas(vecinoId: Int) = repositorio.observarReservas(vecinoId).catch {
        _mensaje.value = "No se pudieron cargar tus reservas"
        emit(emptyList())
    }
    fun cancelar(reserva: ReservaEntity, vecinoId: Int) {
        if (_procesando.value) return
        if (reserva.estado != "PENDIENTE" || reserva.vecinoId != vecinoId) {
            _mensaje.value = "Solo puedes cancelar tus solicitudes pendientes"
            return
        }
        _procesando.value = true
        viewModelScope.launch {
            try {
                repositorio.cancelarReserva(reserva, vecinoId)
                _mensaje.value = "Solicitud cancelada"
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _mensaje.value = if (e is IllegalArgumentException) e.message else "No se pudo cancelar la solicitud"
            } finally { _procesando.value = false }
        }
    }
}
