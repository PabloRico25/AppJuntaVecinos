package com.example.appjuntavecinos.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appjuntavecinos.data.model.LoginErrores
import com.example.appjuntavecinos.data.model.LoginUiState
import com.example.appjuntavecinos.data.model.UsuarioEntity
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.utils.hashClave
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = JuntaRepository(application)

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    private val _sesion = MutableStateFlow<UsuarioEntity?>(null)
    val sesion: StateFlow<UsuarioEntity?> = _sesion.asStateFlow()

    init {
        viewModelScope.launch {
            repositorio.cargarDatosDeEjemplo()
        }
    }

    fun onCorreoChange(valor: String) {
        _estado.update {
            it.copy(
                correo = valor,
                errores = it.errores.copy(correo = null, general = null)
            )
        }
    }

    fun onClaveChange(valor: String) {
        _estado.update {
            it.copy(
                clave = valor,
                errores = it.errores.copy(clave = null, general = null)
            )
        }
    }

    fun validarFormulario(): Boolean {
        val actual = _estado.value

        val nuevosErrores = LoginErrores(
            correo = when {
                actual.correo.isBlank() -> "Escribe tu correo"
                !Patterns.EMAIL_ADDRESS.matcher(actual.correo).matches() -> "Escribe un correo válido"
                else -> null
            },
            clave = when {
                actual.clave.isBlank() -> "Escribe tu clave"
                actual.clave.length < 4 -> "La clave debe tener al menos 4 caracteres"
                else -> null
            }
        )

        _estado.update { it.copy(errores = nuevosErrores) }
        return nuevosErrores == LoginErrores()
    }

    fun iniciarSesion(onExito: () -> Unit) {
        if (!validarFormulario()) return

        viewModelScope.launch {
            val actual = _estado.value
            val usuario = repositorio.buscarUsuarioPorCorreo(actual.correo)

            if (usuario != null && usuario.clave == hashClave(actual.clave)) {
                _sesion.value = usuario
                onExito()
            } else {
                _estado.update {
                    it.copy(errores = it.errores.copy(general = "Correo o clave incorrectos"))
                }
            }
        }
    }

    fun cerrarSesion() {
        _sesion.value = null
        _estado.value = LoginUiState()
    }

    fun puedeConfirmarReservas(): Boolean = _sesion.value?.cargo == "SECRETARIA"

    fun puedeRegistrarPagos(): Boolean = _sesion.value?.cargo == "TESORERIA"
}