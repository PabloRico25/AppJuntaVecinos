package com.example.appjuntavecinos

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

// Un error como el que responde el servidor: código HTTP y cuerpo JSON con "message".
fun errorHttp(codigo: Int, mensaje: String? = null): HttpException {
    val cuerpo = if (mensaje == null) "" else """{"status":$codigo,"message":"$mensaje"}"""
    val respuesta = Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType()))
    return HttpException(respuesta)
}
