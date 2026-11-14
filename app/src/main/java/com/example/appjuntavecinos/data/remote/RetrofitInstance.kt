package com.example.appjuntavecinos.data.remote

import com.example.appjuntavecinos.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    // La dirección sale de build.gradle.kts (propiedad urlServidor): 10.0.2.2 para el emulador, y para un
    // teléfono real la IP del computador en la red local, que también debe estar en network_security_config.xml.
    private const val URL_BASE = BuildConfig.URL_SERVIDOR

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