package com.example.appjuntavecinos.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.appjuntavecinos.data.model.BloqueDto
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.IngresoDto
import com.example.appjuntavecinos.data.model.LoginRequest
import com.example.appjuntavecinos.data.model.NuevaReservaRequest
import com.example.appjuntavecinos.data.model.NuevoPagoRequest
import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.model.UsuarioEntity
import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.RetrofitInstance
import com.example.appjuntavecinos.data.remote.SesionToken
import kotlinx.coroutines.flow.Flow

class JuntaRepository(private val db: AppDatabase, private val api: ApiService) {

    // El que usan los ViewModels: abre Room y usa el Retrofit real. Las pruebas usan el constructor
    // principal, con una base de datos y un ApiService falsos.
    constructor(context: Context) : this(AppDatabase.obtener(context), RetrofitInstance.api)

    private val usuarioDao = db.usuarioDao()
    private val pagoDao = db.pagoDao()
    private val reservaDao = db.reservaDao()
    private val dependenciaDao = db.dependenciaDao()

    // Inicia sesión en el servidor: guarda el token para las peticiones siguientes y devuelve el usuario.
    // El servidor nunca envía la clave, por eso queda vacía en el UsuarioEntity de la sesión.
    suspend fun iniciarSesion(correo: String, clave: String): UsuarioEntity {
        val respuesta = api.login(LoginRequest(correo, clave))
        SesionToken.token = respuesta.token
        val usuario = respuesta.usuario
        return UsuarioEntity(
            id = usuario.id,
            nombre = usuario.nombre,
            correo = usuario.correo,
            clave = "",
            rol = usuario.rol,
            cargo = usuario.cargo
        )
    }

    // Dependencias y usuarios: se piden al servidor y se guardan en Room. Si no hay conexión o la sesión
    // venció, se queda con lo ya guardado (esa es la función de Room como caché).
    // No visto en clase: withTransaction agrupa varias operaciones de Room para que se hagan todas o ninguna.
    suspend fun obtenerDependencias(): List<DependenciaComunitariaEntity> {
        try {
            val lista = api.obtenerDependencias()
            db.withTransaction {
                dependenciaDao.borrarTodas()
                dependenciaDao.insertarTodas(lista)
            }
        } catch (e: Exception) {
            // Sin conexión: se usa lo guardado
        }
        return dependenciaDao.obtenerTodas()
    }

    suspend fun obtenerUsuarios(): List<UsuarioEntity> {
        try {
            val lista = api.obtenerUsuarios().map {
                UsuarioEntity(id = it.id, nombre = it.nombre, correo = it.correo, clave = "", rol = it.rol, cargo = it.cargo)
            }
            db.withTransaction {
                usuarioDao.borrarTodos()
                usuarioDao.insertarTodos(lista)
            }
        } catch (e: Exception) {
            // Sin conexión: se usa lo guardado
        }
        return usuarioDao.obtenerTodos()
    }

    // Las pantallas observan Room: al reemplazar las reservas guardadas, se actualizan solas.
    // El servidor decide qué reservas ve cada uno: el vecino recibe las suyas y la directiva, todas.
    suspend fun refrescarReservas(): Boolean {
        return try {
            val lista = api.obtenerReservas()
            db.withTransaction {
                reservaDao.borrarTodas()
                reservaDao.insertarTodas(lista)
            }
            true
        } catch (e: Exception) {
            // Sin conexión o sesión vencida: se queda lo guardado y se avisa a quien llamó
            false
        }
    }

    fun observarReservas(vecinoId: Int): Flow<List<ReservaEntity>> {
        return reservaDao.observarPorVecino(vecinoId)
    }

    fun observarTodasLasReservas(): Flow<List<ReservaEntity>> {
        return reservaDao.observarTodas()
    }

    // Los bloques ocupados de una sala en un día los entrega el servidor, que ve las reservas de todos
    // (un vecino solo tiene las suyas en Room). A un vecino no le llega quién reservó.
    suspend fun obtenerBloquesOcupados(dependenciaId: Int, fecha: String): List<BloqueDto> {
        return api.obtenerOcupacion(dependenciaId, fecha)
    }

    // Las escrituras SÍ lanzan el error: el ViewModel muestra el mensaje del servidor
    // (horario ocupado, falta de permiso...). Después se refrescan las reservas guardadas.
    suspend fun crearReserva(dependenciaId: Int, fecha: String, horaInicio: String, horaFin: String) {
        api.crearReserva(NuevaReservaRequest(dependenciaId, fecha, horaInicio, horaFin))
        refrescarReservas()
    }

    suspend fun cancelarReserva(reserva: ReservaEntity) {
        api.cancelarReserva(reserva.id)
        refrescarReservas()
    }

    suspend fun confirmarReserva(reserva: ReservaEntity) {
        api.confirmarReserva(reserva.id)
        refrescarReservas()
    }

    suspend fun rechazarReserva(reserva: ReservaEntity) {
        api.rechazarReserva(reserva.id)
        refrescarReservas()
    }

    fun observarPagos():Flow<List<PagoEntity>>{
        return pagoDao.observarTodos()
    }

    suspend fun buscarReserva(id:Int):ReservaEntity?{
        return reservaDao.obtenerPorId(id)
    }

    suspend fun reservaPagada(id:Int):Boolean{
        return pagoDao.contarPagosDeArriendo(id)>0
    }

    // Igual que las reservas: los pagos se piden al servidor y reemplazan lo guardado en Room.
    // Un vecino recibe los montos, pero no la reserva, el comprobante ni quién registró cada pago.
    suspend fun refrescarPagos() {
        try {
            val lista = api.obtenerPagos()
            db.withTransaction {
                pagoDao.borrarTodos()
                pagoDao.insertarTodos(lista)
            }
        } catch (e: Exception) {
            // Sin conexión o sesión vencida: se queda lo guardado
        }
    }

    // Ids de las reservas con el arriendo pagado. Lo informa el servidor, porque un vecino no tiene en
    // Room la reserva de cada pago; sin conexión se usa lo guardado.
    suspend fun obtenerReservasPagadas(): List<Int> {
        return try {
            api.obtenerReservasPagadas()
        } catch (e: Exception) {
            pagoDao.obtenerTodos().filter { it.tipo == "ARRIENDO" }.mapNotNull { it.reservaId }
        }
    }

    // Antes de calcular cuáles reservas faltan por cobrar se actualizan reservas y pagos desde el servidor
    suspend fun obtenerReservasPorPagar(): List<ReservaEntity> {
        refrescarReservas()
        refrescarPagos()
        return reservaDao.obtenerConfirmadasSinPago()
    }

    // Quién registra el pago lo toma el servidor del token, y solo Tesorería puede hacerlo.
    // Si responde con error (reserva sin confirmar, ya pagada...), se lanza para mostrar su mensaje.
    suspend fun crearPago(
        reservaId: Int?,
        tipo: String,
        categoria: String,
        monto: Int,
        fecha: String,
        comprobanteUri: String?,
        descripcion: String
    ) {
        api.registrarPago(
            NuevoPagoRequest(reservaId, tipo, categoria, monto, fecha, comprobanteUri, descripcion)
        )
        refrescarPagos()
    }

    // Inicia sesión en el servidor para activar la puerta. Solo la directiva puede: si no lo es, el servidor
    // responde 403. El token que entrega dura 7 días, porque la puerta queda activa sin que nadie la toque.
    suspend fun activarPuerta(correo: String, clave: String): String {
        return api.login(LoginRequest(correo, clave, "PUERTA")).token
    }

    // Los accesos los lleva el servidor: así un código usado en una puerta no sirve en otra.
    // Si no hay conexión, estas funciones lanzan el error y la puerta no abre.
    suspend fun obtenerReservasUsadas(): Set<Int> {
        return api.obtenerReservasUsadas().toSet()
    }

    suspend fun registrarIngreso(ingreso: IngresoDto) {
        api.registrarIngreso(ingreso)
    }
}