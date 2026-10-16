package com.example.appjuntavecinos.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.hashClave
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = JuntaRepository(application)
    private val _estado = MutableStateFlow(LoginUiState(cargando = true))
    val estado: StateFlow<LoginUiState> = _estado
    private val _sesion = MutableStateFlow<UsuarioEntity?>(null)
    val sesion: StateFlow<UsuarioEntity?> = _sesion

    init {
        viewModelScope.launch {
            try { repositorio.cargarDatosDeEjemplo() }
            catch (e: Exception) {
                if (e is CancellationException) throw e
                _estado.update { it.copy(errores = LoginErrores(general = "No se pudo preparar la base de datos. Intenta ingresar otra vez.")) }
            } finally { _estado.update { it.copy(cargando = false) } }
        }
    }

    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, errores = it.errores.copy(correo = null, general = null)) }
    }
    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, errores = it.errores.copy(clave = null, general = null)) }
    }
    fun validarFormulario(): Boolean {
        val datos = _estado.value
        val errores = LoginErrores(
            correo = when {
                datos.correo.isBlank() -> "Escribe tu correo"
                !Patterns.EMAIL_ADDRESS.matcher(datos.correo.trim()).matches() -> "Escribe un correo válido"
                else -> null
            },
            clave = when {
                datos.clave.isBlank() -> "Escribe tu clave"
                datos.clave.length < 4 -> "La clave debe tener al menos 4 caracteres"
                else -> null
            }
        )
        _estado.update { it.copy(errores = errores) }
        return errores == LoginErrores()
    }
    fun iniciarSesion(onExito: () -> Unit) {
        if (_estado.value.cargando || !validarFormulario()) return
        val datos = _estado.value
        _estado.update { it.copy(cargando = true) }
        viewModelScope.launch {
            try {
                repositorio.cargarDatosDeEjemplo()
                val usuario = repositorio.buscarUsuarioPorCorreo(datos.correo.trim().lowercase(Locale.ROOT))
                if (usuario != null && usuario.clave == hashClave(datos.clave)) {
                    _sesion.value = usuario
                    _estado.value = LoginUiState()
                    onExito()
                } else {
                    _estado.update { it.copy(errores = LoginErrores(general = "Correo o clave incorrectos")) }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _estado.update { it.copy(errores = LoginErrores(general = "No se pudo iniciar sesión. Intenta nuevamente.")) }
            } finally { _estado.update { it.copy(cargando = false) } }
        }
    }
    fun cerrarSesion() {
        _sesion.value = null
        _estado.value = LoginUiState()
    }
    fun puedeConfirmarReservas(): Boolean =
        _sesion.value?.let { it.rol == "DIRECTIVA" && it.cargo == "SECRETARIA" } == true
    fun puedeRegistrarPagos(): Boolean =
        _sesion.value?.let { it.rol == "DIRECTIVA" && it.cargo == "TESORERIA" } == true
}
