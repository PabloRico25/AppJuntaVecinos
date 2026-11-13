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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MisReservasViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias.asStateFlow()

    private val _usuarios = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    val usuarios: StateFlow<List<UsuarioEntity>> = _usuarios.asStateFlow()

    // Las reservas con arriendo pagado las informa el servidor (un vecino no tiene el detalle en Room).
    // Se emite una vez, cuando llega la respuesta: mientras tanto las pantallas ven null (cargando).
    val reservasPagadas: Flow<List<Int>> = flow { emit(repositorio.obtenerReservasPagadas()) }

    init {
        viewModelScope.launch {
            _dependencias.value = repositorio.obtenerDependencias()
            _usuarios.value = repositorio.obtenerUsuarios()
        }
        viewModelScope.launch {
            repositorio.refrescarReservas()
        }
    }

    fun reservas(vecinoId: Int): Flow<List<ReservaEntity>> {
        return repositorio.observarReservas(vecinoId)
    }

    fun cancelar(reserva: ReservaEntity) {
        if (reserva.estado != "PENDIENTE") return

        viewModelScope.launch {
            try {
                repositorio.cancelarReserva(reserva)
            } catch (e: Exception) {
                // El mensaje de error en pantalla llega en B6 (estados de carga y de error)
            }
        }
    }
}