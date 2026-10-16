package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.data.repository.JuntaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReservasDirectivaViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = JuntaRepository(application)
    private val _filtro = MutableStateFlow("PENDIENTE")
    val filtro: StateFlow<String> = _filtro
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje
    private val _procesando = MutableStateFlow(false)
    val procesando: StateFlow<Boolean> = _procesando
    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias
    private val _usuarios = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    val usuarios: StateFlow<List<UsuarioEntity>> = _usuarios

    val reservas = combine(repositorio.observarTodasLasReservas(), _filtro) { lista, filtro ->
        lista.filter { filtro == "TODAS" || it.estado == filtro }
    }.catch { _mensaje.value = "No se pudieron cargar las reservas"; emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
    fun cambiarFiltro(valor: String) { _filtro.value = valor }
    fun gestionar(reserva: ReservaEntity, usuario: UsuarioEntity, confirmar: Boolean) {
        if (_procesando.value) return
        if (usuario.rol != "DIRECTIVA" || usuario.cargo != "SECRETARIA") {
            _mensaje.value = "Solo la Secretaría puede confirmar o rechazar reservas"
            return
        }
        _procesando.value = true
        viewModelScope.launch {
            try {
                repositorio.gestionarReserva(reserva.id, usuario.id, confirmar)
                _mensaje.value = if (confirmar) "Reserva confirmada" else "Solicitud rechazada"
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _mensaje.value = if (e is IllegalArgumentException) e.message else "No se pudo gestionar la reserva"
            } finally { _procesando.value = false }
        }
    }
    fun limpiarMensaje() { _mensaje.value = null }
}
