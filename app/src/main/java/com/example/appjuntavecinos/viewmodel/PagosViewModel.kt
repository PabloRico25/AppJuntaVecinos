package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.model.UsuarioEntity
import com.example.appjuntavecinos.data.repository.JuntaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PagosViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    val pagos: Flow<List<PagoEntity>> = repositorio.observarPagos()

    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias.asStateFlow()

    private val _usuarios = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    val usuarios: StateFlow<List<UsuarioEntity>> = _usuarios.asStateFlow()

    // Reservas CONFIRMADA que todavía no tienen su pago de arriendo registrado.
    // Se arma juntando dos Flow de Room con combine: si Luis registra un pago,
    // esta lista se achica sola, sin que nadie tenga que refrescar la pantalla.
    val reservasPorCobrar: Flow<List<ReservaEntity>> = combine(
        repositorio.observarTodasLasReservas(),
        repositorio.observarPagos()
    ) { reservas, pagos ->
        val yaPagadas = pagos.filter { it.tipo == "ARRIENDO" }.mapNotNull { it.reservaId }.toSet()
        reservas.filter { it.estado == "CONFIRMADA" && it.id !in yaPagadas }
    }

    init {
        viewModelScope.launch {
            _dependencias.value = repositorio.obtenerDependencias()
            _usuarios.value = repositorio.obtenerUsuarios()
        }
    }
}