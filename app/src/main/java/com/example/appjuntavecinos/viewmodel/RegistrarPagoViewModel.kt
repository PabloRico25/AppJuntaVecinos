package com.example.appjuntavecinos.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class RegistrarPagoViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = JuntaRepository(application)
    private val _estado = MutableStateFlow(RegistrarPagoUiState())
    val estado: StateFlow<RegistrarPagoUiState> = _estado
    private val _reservas = MutableStateFlow<List<ReservaEntity>>(emptyList())
    val reservas: StateFlow<List<ReservaEntity>> = _reservas
    private var dependencias = emptyList<DependenciaComunitariaEntity>()
    private var usuarios = emptyList<UsuarioEntity>()
    val tipos = listOf("ARRIENDO", "CONSUMO_BASICO")
    val categorias = listOf("ELECTRICIDAD", "AGUA", "GAS")

    init {
        viewModelScope.launch {
            try {
                dependencias = repositorio.obtenerDependencias()
                usuarios = repositorio.obtenerUsuarios()
                _reservas.value = repositorio.obtenerReservasPorPagar()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                avisarComprobante("No se pudieron cargar las reservas")
            }
        }
    }

    fun etiquetaReserva(reserva: ReservaEntity): String =
        "#${reserva.id} · ${usuarios.find { it.id == reserva.vecinoId }?.nombre.orEmpty()} · " +
            "${dependencias.find { it.id == reserva.dependenciaId }?.nombre.orEmpty()} · ${fechaParaMostrar(reserva.fecha)}"

    fun onTipoChange(valor: String) {
        if (_estado.value.guardando) return
        _estado.update { it.copy(tipo = valor, reserva = null, categoria = "",
            errores = it.errores.copy(tipo = null, reserva = null, categoria = null, general = null)) }
    }
    fun onReservaChange(valor: ReservaEntity) {
        if (!_estado.value.guardando) _estado.update { it.copy(reserva = valor, errores = it.errores.copy(reserva = null, general = null)) }
    }
    fun onCategoriaChange(valor: String) {
        if (!_estado.value.guardando) _estado.update { it.copy(categoria = valor, errores = it.errores.copy(categoria = null)) }
    }
    fun onMontoChange(valor: String) {
        // Conservar el signo o separador permite rechazarlo, en vez de cambiar el monto pegado.
        if (!_estado.value.guardando) _estado.update { it.copy(monto = valor.take(32),
            errores = it.errores.copy(monto = null)) }
    }
    fun onFechaChange(millis: Long?) {
        if (!_estado.value.guardando && millis != null) _estado.update {
            it.copy(fecha = fechaDesdeMillis(millis), errores = it.errores.copy(fecha = null))
        }
    }
    fun onDescripcionChange(valor: String) {
        if (!_estado.value.guardando) _estado.update { it.copy(descripcion = valor.take(240)) }
    }
    fun onAdjuntarChange(valor: Boolean) {
        if (!_estado.value.guardando) _estado.update {
            it.copy(adjuntarComprobante = valor, comprobante = if (valor) it.comprobante else null,
                errores = it.errores.copy(comprobante = null))
        }
    }
    fun onComprobanteElegido(uri: Uri?) {
        if (!_estado.value.guardando && uri != null) _estado.update {
            it.copy(comprobante = uri.toString(), errores = it.errores.copy(comprobante = null, general = null))
        }
    }
    fun avisarComprobante(mensaje: String) { _estado.update { it.copy(errores = it.errores.copy(general = mensaje)) } }
    fun onConfirmaChange(valor: Boolean) {
        if (!_estado.value.guardando) _estado.update { it.copy(confirma = valor, errores = it.errores.copy(confirma = null)) }
    }
    fun validarFormulario(): Boolean {
        val errores = validarDatosPago(_estado.value)
        _estado.update { it.copy(errores = errores) }
        return errores == RegistrarPagoErrores()
    }
    fun guardar(registradoPorId: Int, puedeRegistrar: Boolean, onExito: () -> Unit) {
        if (_estado.value.guardando) return
        if (!puedeRegistrar) { avisarComprobante("Solo la Tesorería puede registrar pagos"); return }
        if (!validarFormulario()) return
        val datos = _estado.value
        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            var copia: String? = null
            var guardado = false
            try {
                if (datos.adjuntarComprobante) {
                    withContext(Dispatchers.IO) {
                        copia = guardarComprobante(getApplication(), Uri.parse(datos.comprobante!!))
                    }
                    require(copia != null) { "No se pudo guardar el comprobante. Elige una imagen de hasta 20 MB." }
                }
                repositorio.crearPago(PagoEntity(
                    reservaId = datos.reserva?.id, tipo = datos.tipo, categoria = datos.categoria,
                    monto = datos.monto.toDouble(), fecha = datos.fecha, comprobanteUri = copia,
                    registradoPorId = registradoPorId, descripcion = datos.descripcion.trim()
                ))
                guardado = true
                _estado.value = RegistrarPagoUiState()
                onExito()
            } catch (e: Exception) {
                if (!guardado) copia?.let { eliminarComprobante(getApplication(), it) }
                if (e is CancellationException) throw e
                avisarComprobante(if (e is IllegalArgumentException) e.message.orEmpty() else "No se pudo guardar el pago")
                _reservas.value = runCatching { repositorio.obtenerReservasPorPagar() }.getOrDefault(_reservas.value)
            } finally { _estado.update { it.copy(guardando = false) } }
        }
    }
}
