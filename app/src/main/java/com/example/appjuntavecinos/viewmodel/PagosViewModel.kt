package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.data.repository.JuntaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PagosViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = JuntaRepository(application)
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    val pagos = repositorio.observarPagos().catch { _error.value = "No se pudieron cargar los pagos"; emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val porCobrar = combine(repositorio.observarTodasLasReservas(), repositorio.observarPagos()) { reservas, pagos ->
        val pagadas = pagos.filter { it.tipo == "ARRIENDO" }.mapNotNull { it.reservaId }
        reservas.filter { it.estado == "CONFIRMADA" && it.id !in pagadas }
    }.catch { _error.value = "No se pudieron cargar los cobros pendientes"; emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _usuarios = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    val usuarios: StateFlow<List<UsuarioEntity>> = _usuarios
    init {
        viewModelScope.launch {
            try { _usuarios.value = repositorio.obtenerUsuarios() }
            catch (e: Exception) {
                if (e is CancellationException) throw e
                _error.value = "No se pudieron cargar los nombres"
            }
        }
    }
}
