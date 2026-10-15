package com.example.appjuntavecinos.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.data.model.RegistrarPagoErrores
import com.example.appjuntavecinos.data.model.RegistrarPagoUiState
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.fechaDesdeMillis
import com.example.appjuntavecinos.utils.guardarComprobante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistrarPagoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    private val _estado = MutableStateFlow(RegistrarPagoUiState())
    val estado: StateFlow<RegistrarPagoUiState> = _estado.asStateFlow()

    private val _reservas = MutableStateFlow<List<ReservaEntity>>(emptyList())
    val reservas: StateFlow<List<ReservaEntity>> = _reservas.asStateFlow()

    private val _dependencias = MutableStateFlow<List<DependenciaComunitariaEntity>>(emptyList())

    val tipos = listOf("ARRIENDO", "CONSUMO_BASICO")
    val categorias = listOf("ELECTRICIDAD", "AGUA", "GAS")

    init {
        viewModelScope.launch {
            _dependencias.value = repositorio.obtenerDependencias()
            _reservas.value = repositorio.obtenerReservasPorPagar()
        }
    }

    fun etiquetaReserva(reserva: ReservaEntity): String {
        val nombre = _dependencias.value.firstOrNull { it.id == reserva.dependenciaId }?.nombre ?: ""
        return "$nombre, ${reserva.fecha}, ${reserva.horaInicio} a ${reserva.horaFin}"
    }

    fun onTipoChange(valor: String) {
        _estado.update {
            it.copy(
                tipo = valor,
                reserva = null,
                categoria = "",
                errores = it.errores.copy(tipo = null, reserva = null, categoria = null, general = null)
            )
        }
    }

    fun onReservaChange(valor: ReservaEntity) {
        _estado.update {
            it.copy(reserva = valor, errores = it.errores.copy(reserva = null, general = null))
        }
    }

    fun onCategoriaChange(valor: String) {
        _estado.update {
            it.copy(categoria = valor, errores = it.errores.copy(categoria = null, general = null))
        }
    }

    fun onMontoChange(valor: String) {
        if (valor.length > 9 || !valor.all { it.isDigit() }) return
        _estado.update {
            it.copy(monto = valor, errores = it.errores.copy(monto = null, general = null))
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

    fun onDescripcionChange(valor: String) {
        _estado.update { it.copy(descripcion = valor) }
    }

    fun onAdjuntarChange(valor: Boolean) {
        _estado.update {
            it.copy(
                adjuntarComprobante = valor,
                comprobante = if (valor) it.comprobante else null,
                errores = it.errores.copy(comprobante = null)
            )
        }
    }

    fun onComprobanteElegido(uri: Uri?) {
        if (uri == null) return
        _estado.update {
            it.copy(comprobante = uri.toString(), errores = it.errores.copy(comprobante = null))
        }
    }

    fun avisarComprobante(mensaje: String) {
        _estado.update { it.copy(errores = it.errores.copy(comprobante = mensaje)) }
    }

    fun onConfirmaChange(valor: Boolean) {
        _estado.update {
            it.copy(confirma = valor, errores = it.errores.copy(confirma = null))
        }
    }

    fun validarFormulario(): Boolean {
        val actual = _estado.value
        val monto = actual.monto.toIntOrNull()

        val nuevosErrores = RegistrarPagoErrores(
            tipo = if (actual.tipo.isBlank()) "Elige el tipo de pago" else null,
            reserva = if (actual.tipo == "ARRIENDO" && actual.reserva == null) {
                "Elige la reserva que se paga"
            } else null,
            categoria = if (actual.tipo == "CONSUMO_BASICO" && actual.categoria.isBlank()) {
                "Elige la categoría del gasto"
            } else null,
            monto = when {
                monto == null -> "Escribe el monto"
                monto <= 0 -> "El monto debe ser mayor que 0"
                else -> null
            },
            fecha = if (actual.fecha.isBlank()) "Elige la fecha del pago" else null,
            comprobante = if (actual.adjuntarComprobante && actual.comprobante == null) {
                "Elige una imagen o desactiva el comprobante"
            } else null,
            confirma = if (!actual.confirma) "Debes confirmar que es un registro informativo" else null
        )

        _estado.update { it.copy(errores = nuevosErrores) }
        return nuevosErrores == RegistrarPagoErrores()
    }

    fun guardar(registradoPorId: Int, puedeRegistrar: Boolean, onExito: () -> Unit) {
        if (_estado.value.guardando) return
        if (!puedeRegistrar) {
            _estado.update {
                it.copy(errores = it.errores.copy(general = "Solo la Tesorería puede registrar pagos"))
            }
            return
        }
        if (!validarFormulario()) return

        _estado.update { it.copy(guardando = true) }

        viewModelScope.launch {
            val actual = _estado.value

            var comprobanteGuardado: String? = null
            if (actual.adjuntarComprobante && actual.comprobante != null) {
                comprobanteGuardado = guardarComprobante(getApplication(), Uri.parse(actual.comprobante))
                if (comprobanteGuardado == null) {
                    _estado.update {
                        it.copy(
                            guardando = false,
                            errores = it.errores.copy(general = "No se pudo guardar el comprobante")
                        )
                    }
                    return@launch
                }
            }

            repositorio.crearPago(
                PagoEntity(
                    reservaId = if (actual.tipo == "ARRIENDO") actual.reserva?.id else null,
                    tipo = actual.tipo,
                    categoria = if (actual.tipo == "CONSUMO_BASICO") actual.categoria else "",
                    monto = actual.monto.toInt(),
                    fecha = actual.fecha,
                    comprobanteUri = comprobanteGuardado,
                    registradoPorId = registradoPorId,
                    descripcion = actual.descripcion.trim()
                )
            )
            onExito()
        }
    }
}