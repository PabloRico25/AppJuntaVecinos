package com.example.appjuntavecinos.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    // 10.0.2.2 es el computador visto desde el emulador. Con un teléfono real se usa la IP del
    // computador en la red local (también permitida en network_security_config.xml).
    private const val URL_BASE = "http://10.0.2.2:9090/"

    // Si el servidor no responde, la app falla en segundos en vez de quedarse esperando
    private val cliente = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor())
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}