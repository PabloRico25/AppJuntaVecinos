package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NuevaReservaViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = JuntaRepository(application)
    private val _estado = MutableStateFlow(NuevaReservaUiState())
    val estado: StateFlow<NuevaReservaUiState> = _estado
    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias
    val horas = (9..22).map { "%02d:00".format(it) }

    init {
        viewModelScope.launch {
            try { _dependencias.value = repositorio.obtenerDependencias() }
            catch (e: Exception) {
                if (e is CancellationException) throw e
                _estado.update { it.copy(errores = it.errores.copy(general = "No se pudieron cargar las dependencias")) }
            }
        }
    }

    fun onDependenciaChange(valor: DependenciaComunitariaEntity) {
        _estado.update { it.copy(dependencia = valor, errores = it.errores.copy(dependencia = null, general = null)) }
    }
    fun onFechaChange(millis: Long?) {
        if (millis != null) _estado.update { it.copy(fecha = fechaDesdeMillis(millis), errores = it.errores.copy(fecha = null, general = null)) }
    }
    fun onHoraInicioChange(valor: String) {
        _estado.update { it.copy(horaInicio = valor, errores = it.errores.copy(horaInicio = null, horaFin = null, general = null)) }
    }
    fun onHoraFinChange(valor: String) {
        _estado.update { it.copy(horaFin = valor, errores = it.errores.copy(horaFin = null, general = null)) }
    }
    fun onReglamentoChange(valor: Boolean) {
        _estado.update { it.copy(aceptaReglamento = valor, errores = it.errores.copy(reglamento = null)) }
    }
    fun validarFormulario(): Boolean {
        val errores = validarDatosReserva(_estado.value, fechaDeHoy())
        _estado.update { it.copy(errores = errores) }
        return errores == NuevaReservaErrores()
    }
    fun guardar(vecinoId: Int, onExito: () -> Unit) {
        if (_estado.value.guardando || !validarFormulario()) return
        val datos = _estado.value
        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                repositorio.crearReserva(ReservaEntity(
                    dependenciaId = datos.dependencia!!.id, vecinoId = vecinoId,
                    fecha = datos.fecha, horaInicio = datos.horaInicio, horaFin = datos.horaFin,
                    fechaSolicitud = fechaDeHoy()
                ))
                _estado.value = NuevaReservaUiState()
                onExito()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val mensaje = if (e is IllegalArgumentException) e.message else "No se pudo guardar. Intenta nuevamente."
                _estado.update { it.copy(guardando = false, errores = it.errores.copy(general = mensaje)) }
            }
        }
    }
}
