package com.example.appjuntavecinos.data.remote

import com.example.appjuntavecinos.data.model.ErrorApi
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

// No visto en clase: convierte lo que falla en un mensaje para mostrar en pantalla.
// Si el servidor respondió con un error (401, 409...), su "message" ya viene escrito para el usuario.
fun mensajeDeError(error: Throwable): String {
    return when (error) {
        is HttpException -> {
            val texto = error.response()?.errorBody()?.string()
            val mensaje = try {
                Gson().fromJson(texto, ErrorApi::class.java)?.message
            } catch (e: Exception) {
                null
            }
            when {
                !mensaje.isNullOrBlank() -> mensaje
                // Spring Security responde 401 sin cuerpo cuando el token falta o venció
                error.code() == 401 -> "Tu sesión venció. Inicia sesión de nuevo"
                else -> "El servidor no pudo atender la petición"
            }
        }
        is IOException -> "No se pudo conectar con el servidor. Revisa que esté encendido"
        else -> "Ocurrió un error inesperado"
    }
}