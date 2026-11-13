package com.example.appjuntavecinos.data.remote

import com.example.appjuntavecinos.data.model.BloqueDto
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.IngresoDto
import com.example.appjuntavecinos.data.model.LoginRequest
import com.example.appjuntavecinos.data.model.LoginResponse
import com.example.appjuntavecinos.data.model.NuevaReservaRequest
import com.example.appjuntavecinos.data.model.NuevoPagoRequest
import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.model.UsuarioDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Cada función es una ruta del gateway (CONTRATO_API.md). Son suspend, como en el ejemplo del docente.
// Los métodos POST y PUT no se vieron en clase: la guía 14 pide investigarlos.
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body pedido: LoginRequest): LoginResponse

    @GET("usuarios")
    suspend fun obtenerUsuarios(): List<UsuarioDto>

    @GET("dependencias")
    suspend fun obtenerDependencias(): List<DependenciaComunitariaEntity>

    // El vecino recibe solo las suyas; la directiva, todas. "estado" es opcional.
    @GET("reservas")
    suspend fun obtenerReservas(@Query("estado") estado: String? = null): List<ReservaEntity>

    @GET("reservas/{id}")
    suspend fun obtenerReserva(@Path("id") id: Int): ReservaEntity

    @POST("reservas")
    suspend fun crearReserva(@Body pedido: NuevaReservaRequest): ReservaEntity

    @PUT("reservas/{id}/cancelar")
    suspend fun cancelarReserva(@Path("id") id: Int): ReservaEntity

    @PUT("reservas/{id}/confirmar")
    suspend fun confirmarReserva(@Path("id") id: Int): ReservaEntity

    @PUT("reservas/{id}/rechazar")
    suspend fun rechazarReserva(@Path("id") id: Int): ReservaEntity

    @GET("reservas/ocupacion")
    suspend fun obtenerOcupacion(
        @Query("dependenciaId") dependenciaId: Int,
        @Query("fecha") fecha: String
    ): List<BloqueDto>

    @GET("accesos/usadas")
    suspend fun obtenerReservasUsadas(): List<Int>

    @POST("accesos")
    suspend fun registrarIngreso(@Body ingreso: IngresoDto): IngresoDto
    @GET("pagos")
    suspend fun obtenerPagos(): List<PagoEntity>

    @GET("pagos/pagadas")
    suspend fun obtenerReservasPagadas(): List<Int>

    @POST("pagos")
    suspend fun registrarPago(@Body pedido: NuevoPagoRequest): PagoEntity
}