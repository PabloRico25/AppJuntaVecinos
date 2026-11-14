package com.example.appjuntavecinos

import com.example.appjuntavecinos.data.remote.mensajeDeError
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ErroresApiTest : StringSpec({

    fun errorHttp(codigo: Int, cuerpo: String): HttpException {
        val respuesta = Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType()))
        return HttpException(respuesta)
    }

    "si el servidor responde con un mensaje, ese mensaje se muestra" {
        val error = errorHttp(409, """{"status":409,"message":"Ese horario ya está ocupado por una reserva confirmada"}""")
        mensajeDeError(error) shouldBe "Ese horario ya está ocupado por una reserva confirmada"
    }

    "un 401 sin cuerpo significa que la sesión venció" {
        mensajeDeError(errorHttp(401, "")) shouldBe "Tu sesión venció. Inicia sesión de nuevo"
    }

    "un error del servidor sin mensaje usa un texto genérico" {
        mensajeDeError(errorHttp(500, "{}")) shouldBe "El servidor no pudo atender la petición"
    }

    "un cuerpo que no es JSON no rompe el mensaje" {
        mensajeDeError(errorHttp(500, "<html>error</html>")) shouldBe "El servidor no pudo atender la petición"
    }

    "sin conexión se pide revisar que el servidor esté encendido" {
        mensajeDeError(IOException("timeout")) shouldBe "No se pudo conectar con el servidor. Revisa que esté encendido"
    }

    "cualquier otro fallo se muestra como inesperado" {
        mensajeDeError(IllegalStateException("x")) shouldBe "Ocurrió un error inesperado"
    }
})
