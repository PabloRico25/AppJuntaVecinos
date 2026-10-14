package com.example.appjuntavecinos.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.appjuntavecinos.data.model.LoginErrores
import com.example.appjuntavecinos.data.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun onCorreoChange(valor: String) {
        _estado.update {
            it.copy(
                correo = valor,
                errores = it.errores.copy(correo = null)
            )
        }
    }

    fun onClaveChange(valor: String) {
        _estado.update {
            it.copy(
                clave = valor,
                errores = it.errores.copy(clave = null)
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
}