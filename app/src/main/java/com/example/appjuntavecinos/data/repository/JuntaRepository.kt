package com.example.appjuntavecinos.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.IngresoEntity
import com.example.appjuntavecinos.data.model.LoginRequest
import com.example.appjuntavecinos.data.model.NuevaReservaRequest
import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.model.UsuarioEntity
import com.example.appjuntavecinos.data.remote.RetrofitInstance
import com.example.appjuntavecinos.data.remote.SesionToken
import com.example.appjuntavecinos.utils.hashClave
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class JuntaRepository(context: Context) {
    private val db = AppDatabase.obtener(context)
    private val usuarioDao = db.usuarioDao()
    private val pagoDao = db.pagoDao()
    private val reservaDao = db.reservaDao()
    private val dependenciaDao = db.dependenciaDao()
    private val ingresoDao = db.ingresoDao()

    suspend fun buscarUsuarioPorCorreo(correo: String): UsuarioEntity? {
        return usuarioDao.buscarPorCorreo(correo)
    }


    // Inicia sesión en el servidor: guarda el token para las peticiones siguientes y devuelve el usuario.
    // El servidor nunca envía la clave, por eso queda vacía en el UsuarioEntity de la sesión.
    suspend fun iniciarSesion(correo: String, clave: String): UsuarioEntity {
        val respuesta = RetrofitInstance.api.login(LoginRequest(correo, clave))
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
            val lista = RetrofitInstance.api.obtenerDependencias()
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
            val lista = RetrofitInstance.api.obtenerUsuarios().map {
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
    suspend fun refrescarReservas() {
        try {
            val lista = RetrofitInstance.api.obtenerReservas()
            db.withTransaction {
                reservaDao.borrarTodas()
                reservaDao.insertarTodas(lista)
            }
        } catch (e: Exception) {
            // Sin conexión o sesión vencida: se queda lo guardado
        }
    }

    fun observarReservas(vecinoId: Int): Flow<List<ReservaEntity>> {
        return reservaDao.observarPorVecino(vecinoId)
    }

    fun observarTodasLasReservas(): Flow<List<ReservaEntity>> {
        return reservaDao.observarTodas()
    }

    // Las escrituras SÍ lanzan el error: el ViewModel muestra el mensaje del servidor
    // (horario ocupado, falta de permiso...). Después se refrescan las reservas guardadas.
    suspend fun crearReserva(dependenciaId: Int, fecha: String, horaInicio: String, horaFin: String) {
        RetrofitInstance.api.crearReserva(NuevaReservaRequest(dependenciaId, fecha, horaInicio, horaFin))
        refrescarReservas()
    }

    suspend fun cancelarReserva(reserva: ReservaEntity) {
        RetrofitInstance.api.cancelarReserva(reserva.id)
        refrescarReservas()
    }

    suspend fun confirmarReserva(reserva: ReservaEntity) {
        RetrofitInstance.api.confirmarReserva(reserva.id)
        refrescarReservas()
    }

    suspend fun rechazarReserva(reserva: ReservaEntity) {
        RetrofitInstance.api.rechazarReserva(reserva.id)
        refrescarReservas()
    }

    fun observarPagos():Flow<List<PagoEntity>>{
        return pagoDao.observarTodos()
    }

    fun observarReservasPagadas():Flow<List<Int>> {
        return pagoDao.observarReservasPagadas()
    }

    suspend fun buscarReserva(id:Int):ReservaEntity?{
        return reservaDao.obtenerPorId(id)
    }

    suspend fun reservaPagada(id:Int):Boolean{
        return pagoDao.contarPagosDeArriendo(id)>0
    }

    suspend fun obtenerReservasPorPagar():List<ReservaEntity>{
        return reservaDao.obtenerConfirmadasSinPago()
    }

    suspend fun crearPago(pago:PagoEntity){
        pagoDao.insertar(pago)
    }

    suspend fun obtenerReservasUsadas():Set<Int>{
        return ingresoDao.obtenerReservasUsadas().toSet()
    }

    suspend fun registrarIngreso(ingreso:IngresoEntity){
        ingresoDao.insertar(ingreso)
    }

    suspend fun cargarDatosDeEjemplo() {
        if (dependenciaDao.obtenerTodas().isNotEmpty()) return

        dependenciaDao.insertar(DependenciaComunitariaEntity(nombre = "Salón Principal"))
        dependenciaDao.insertar(DependenciaComunitariaEntity(nombre = "Sala Multiuso"))
        val clave = hashClave("1234")
        val usuarios = listOf(
            UsuarioEntity(nombre="Juan Pérez",correo ="juan.perez@zaror.test",clave=clave,
                rol="VECINO"),
            UsuarioEntity(nombre="Ana Muñoz",correo="ana.munoz@zaror.test",clave=clave,
                rol="VECINO"),
            UsuarioEntity(nombre="Pedro Soto",correo="pedro.soto@zaror.test",clave=clave,
                rol="VECINO"),
            UsuarioEntity(nombre="María Torres",correo="maria.torres@zaror.test",clave=clave,
                rol="DIRECTIVA",cargo="SECRETARIA"),
            UsuarioEntity(nombre="Luis Rojas",correo="luis.rojas@zaror.test",clave=clave,
                rol="DIRECTIVA",cargo="TESORERIA"),
            UsuarioEntity(nombre="Carmen Vidal",correo="carmen.vidal@zaror.test",clave=clave,
                rol="DIRECTIVA",cargo="PRESIDENCIA")
        )
        usuarios.forEach { usuarioDao.insertar(it) }

        val reservas = listOf(
            ReservaEntity(dependenciaId=1,vecinoId=1,fecha = fechaEn(11),
                horaInicio = "18:00", horaFin = "21:00", estado = "CONFIRMADA",
                fechaSolicitud = fechaEn(-5), gestionadaPorId = 4),
            ReservaEntity(dependenciaId = 2, vecinoId = 1, fecha = fechaEn(12),
                horaInicio = "11:00", horaFin = "14:00", estado = "PENDIENTE",
                fechaSolicitud = fechaEn(-2)),
            ReservaEntity(dependenciaId = 1, vecinoId = 3, fecha = fechaEn(11),
                horaInicio = "19:00", horaFin = "21:00", estado = "PENDIENTE",
                fechaSolicitud = fechaEn(-1)),
            ReservaEntity(dependenciaId = 2, vecinoId = 3, fecha = fechaEn(25),
                horaInicio = "10:00", horaFin = "13:00", estado = "PENDIENTE",
                fechaSolicitud = fechaEn(-1)),
            ReservaEntity(dependenciaId = 1, vecinoId = 1, fecha = fechaEn(18),
                horaInicio = "15:00", horaFin = "18:00", estado = "RECHAZADA",
                fechaSolicitud = fechaEn(-9), gestionadaPorId = 4),
            ReservaEntity(dependenciaId = 2, vecinoId = 1, fecha = fechaEn(4),
                horaInicio = "10:00", horaFin = "12:00", estado = "CANCELADA",
                fechaSolicitud = fechaEn(-8)),
            ReservaEntity(dependenciaId = 2, vecinoId = 3, fecha = fechaEn(-3),
                horaInicio = "10:00", horaFin = "13:00", estado = "CONFIRMADA",
                fechaSolicitud = fechaEn(-10), gestionadaPorId = 4),
            ReservaEntity(dependenciaId = 1, vecinoId = 3, fecha = fechaEn(-3),
                horaInicio = "12:00", horaFin = "16:00", estado = "CONFIRMADA",
                fechaSolicitud = fechaEn(-10), gestionadaPorId = 4)
        )
        reservas.forEach { reservaDao.insertar(it) }

        val pagos = listOf(
            PagoEntity(reservaId = 8, tipo = "ARRIENDO", monto = 60000,
                fecha = fechaEn(-2), registradoPorId = 5),
            PagoEntity(tipo = "CONSUMO_BASICO", categoria = "ELECTRICIDAD", monto = 38500,
                fecha = fechaEn(-2), registradoPorId = 5),
            PagoEntity(reservaId = 7, tipo = "ARRIENDO", monto = 40000,
                fecha = fechaEn(-3), registradoPorId = 5),
            PagoEntity(tipo = "CONSUMO_BASICO", categoria = "AGUA", monto = 24200,
                fecha = fechaEn(-4), registradoPorId = 5),
            PagoEntity(tipo = "CONSUMO_BASICO", categoria = "GAS", monto = 24000,
                fecha = fechaEn(-5), registradoPorId = 5),
            PagoEntity(reservaId = 1, tipo = "ARRIENDO", monto = 45000,
                fecha = fechaEn(-1), registradoPorId = 5)
        )
        pagos.forEach { pagoDao.insertar(it) }
    }

    private fun fechaEn(dias: Int): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_YEAR, dias)
        return SimpleDateFormat("yyyy-MM-dd",
            Locale.getDefault()).format(calendario.time)
    }
}