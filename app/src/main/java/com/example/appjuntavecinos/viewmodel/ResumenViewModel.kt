package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.ResumenFinanciero
import com.example.appjuntavecinos.utils.calcularResumen
import com.example.appjuntavecinos.utils.fechaDeHoy
import com.example.appjuntavecinos.utils.sumarMeses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

class ResumenViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    val mesActual: String = fechaDeHoy().take(7)

    private val _mes = MutableStateFlow(mesActual)
    val mes: StateFlow<String> = _mes.asStateFlow()

    // No visto en clase (lo integramos con permiso del docente): combine junta varios Flow en uno.
    val resumen: Flow<ResumenFinanciero> = combine(_mes, repositorio.observarPagos()) { mesElegido, pagos ->
        calcularResumen(pagos, mesElegido)
    }

    fun cambiarMes(cambio: Int) {
        _mes.update { sumarMeses(it, cambio) }
    }
}