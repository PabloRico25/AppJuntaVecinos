package com.example.appjuntavecinos.data.remote

import okhttp3.Interceptor
import okhttp3.Response

// No visto en clase: un interceptor de OkHttp ve cada petición antes de que salga y puede modificarla.
// Este agrega "Authorization: Bearer <token>" si hay sesión; sin sesión (el login) la deja pasar igual.
// Además mira la respuesta: un 401 en una petición que llevaba token significa que la sesión venció.
class TokenInterceptor(
    private val alVencerLaSesion: () -> Unit = { EventosSesion.avisarVencida() }
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = SesionToken.token
        val peticion = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        val respuesta = chain.proceed(peticion)
        // Sin token, un 401 es el login con la clave incorrecta: eso no es una sesión vencida
        if (token != null && respuesta.code == 401) {
            alVencerLaSesion()
        }
        return respuesta
    }
}