package com.example.appjuntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.NuevaReservaErrores
import com.example.appjuntavecinos.data.model.NuevaReservaUiState
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.fechaDeHoy
import com.example.appjuntavecinos.utils.fechaDesdeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NuevaReservaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    private val _estado = MutableStateFlow(NuevaReservaUiState())
    val estado: StateFlow<NuevaReservaUiState> = _estado.asStateFlow()

    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())
    val dependencias: StateFlow<List<DependenciaComunitariaEntity>> = _dependencias.asStateFlow()

    val horas = (9..22).map { "%02d:00".format(it) }

    init {
        viewModelScope.launch {
            _dependencias.value = repositorio.obtenerDependencias()
        }
    }

    fun onDependenciaChange(valor: DependenciaComunitariaEntity) {
        _estado.update {
            it.copy(
                dependencia = valor,
                errores = it.errores.copy(dependencia = null, general = null)
            )
        }
    }

    fun onFechaChange(millis: Long?) {
        if (millis == null) return
        _estado.update {
            it.copy(
                fecha = fechaDesdeMillis(millis),
                errores = it.errores.copy(fecha = null, general = null)
            )
        }
    }

    fun onHoraInicioChange(valor: String) {
        _estado.update {
            it.copy(
                horaInicio = valor,
                errores = it.errores.copy(horaInicio = null, horaFin = null, general = null)
            )
        }
    }

    fun onHoraFinChange(valor: String) {
        _estado.update {
            it.copy(
                horaFin = valor,
                errores = it.errores.copy(horaFin = null, general = null)
            )
        }
    }

    fun onReglamentoChange(valor: Boolean) {
        _estado.update {
            it.copy(
                aceptaReglamento = valor,
                errores = it.errores.copy(reglamento = null)
            )
        }
    }

    fun validarFormulario(): Boolean {
        val actual = _estado.value

        val nuevosErrores = NuevaReservaErrores(
            dependencia = if (actual.dependencia == null) "Elige una dependencia" else null,
            fecha = when {
                actual.fecha.isBlank() -> "Elige una fecha"
                actual.fecha < fechaDeHoy() -> "La fecha no puede ser pasada"
                else -> null
            },
            horaInicio = if (actual.horaInicio.isBlank()) "Elige la hora de inicio" else null,
            horaFin = when {
                actual.horaFin.isBlank() -> "Elige la hora de fin"
                actual.horaInicio.isNotBlank() && actual.horaFin <= actual.horaInicio ->
                    "La hora de fin debe ser mayor que la de inicio"
                else -> null
            },
            reglamento = if (!actual.aceptaReglamento) "Debes aceptar el reglamento de la sede" else null
        )

        _estado.update { it.copy(errores = nuevosErrores) }
        return nuevosErrores == NuevaReservaErrores()
    }

    fun guardar(vecinoId: Int, onExito: () -> Unit) {
        if (!validarFormulario()) return

        viewModelScope.launch {
            val actual = _estado.value
            val dependencia = actual.dependencia ?: return@launch

            if (repositorio.hayChoque(dependencia.id, actual.fecha, actual.horaInicio, actual.horaFin)) {
                _estado.update {
                    it.copy(
                        errores = it.errores.copy(
                            general = "Ese horario ya está ocupado por una reserva confirmada"
                        )
                    )
                }
                return@launch
            }

            repositorio.crearReserva(
                ReservaEntity(
                    dependenciaId = dependencia.id,
                    vecinoId = vecinoId,
                    fecha = actual.fecha,
                    horaInicio = actual.horaInicio,
                    horaFin = actual.horaFin,
                    estado = "PENDIENTE",
                    fechaSolicitud = fechaDeHoy()
                )
            )
            onExito()
        }
    }
}