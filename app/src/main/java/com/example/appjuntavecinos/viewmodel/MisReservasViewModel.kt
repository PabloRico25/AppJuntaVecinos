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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MisReservasViewModel(
    application: Application,
    private val repositorio: JuntaRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, JuntaRepository(application))

    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias.asStateFlow()

    private val _usuarios = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    val usuarios: StateFlow<List<UsuarioEntity>> = _usuarios.asStateFlow()

    // Mientras se piden las reservas al servidor. Las pantallas muestran un esqueleto si no tienen nada guardado.
    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    // El servidor no respondió: la pantalla muestra lo guardado con un aviso, o un estado de error si no hay nada
    private val _sinActualizar = MutableStateFlow(false)
    val sinActualizar: StateFlow<Boolean> = _sinActualizar.asStateFlow()

    // El mensaje del servidor cuando falla cancelar una solicitud
    private val _errorCancelar = MutableStateFlow<String?>(null)
    val errorCancelar: StateFlow<String?> = _errorCancelar.asStateFlow()

    // Las reservas con arriendo pagado las informa el servidor (un vecino no tiene el detalle en Room).
    // Se emite una vez, cuando llega la respuesta: mientras tanto las pantallas ven null (cargando).
    val reservasPagadas: Flow<List<Int>> = flow { emit(repositorio.obtenerReservasPagadas()) }

    init {
        viewModelScope.launch {
            _dependencias.value = repositorio.obtenerDependencias()
            _usuarios.value = repositorio.obtenerUsuarios()
        }
        cargar()
    }

    fun reintentar() {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            _cargando.value = true
            _sinActualizar.value = !repositorio.refrescarReservas()
            _cargando.value = false
        }
    }

    fun reservas(vecinoId: Int): Flow<List<ReservaEntity>> {
        return repositorio.observarReservas(vecinoId)
    }

    fun cancelar(reserva: ReservaEntity) {
        if (reserva.estado != "PENDIENTE") return

        viewModelScope.launch {
            try {
                _errorCancelar.value = null
                repositorio.cancelarReserva(reserva)
            } catch (e: Exception) {
                _errorCancelar.value = mensajeDeError(e)
            }
        }
    }
}
