package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.*
import kotlinx.coroutines.flow.*

fun calcularResumen(pagos: List<PagoEntity>, mes: String): ResumenUiState {
    val movimientos = pagos.filter { it.fecha.take(7) == mes }
    return ResumenUiState(
        mes = mes,
        ingresos = movimientos.filter { it.tipo == "ARRIENDO" }.sumOf { it.monto },
        gastos = movimientos.filter { it.tipo == "CONSUMO_BASICO" }.sumOf { it.monto },
        gastosPorCategoria = listOf("ELECTRICIDAD", "AGUA", "GAS").associateWith { categoria ->
            movimientos.filter { it.tipo == "CONSUMO_BASICO" && it.categoria == categoria }.sumOf { it.monto }
        },
        movimientos = movimientos
    )
}

class ResumenViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = JuntaRepository(application)
    private val _mes = MutableStateFlow(fechaDeHoy().take(7))
    val estado = combine(_mes, repositorio.observarPagos()) { mes, pagos -> calcularResumen(pagos, mes) }
        .catch { emit(ResumenUiState(mes = _mes.value, error = "No se pudo cargar el resumen")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenUiState())

    fun cambiarMes(cambio: Int) { _mes.value = mesAnterior(_mes.value, cambio) }
}
