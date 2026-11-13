package com.example.appjuntavecinos.data.remote

import okhttp3.Interceptor
import okhttp3.Response

// No visto en clase: un interceptor de OkHttp ve cada petición antes de que salga y puede modificarla.
// Este agrega "Authorization: Bearer <token>" si hay sesión; sin sesión (el login) la deja pasar igual.
class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = SesionToken.token
        val peticion = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(peticion)
    }
}