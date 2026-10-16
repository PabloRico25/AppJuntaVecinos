package com.example.appjuntavecinos.data.repository

import android.content.Context
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.model.UsuarioEntity
import com.example.appjuntavecinos.utils.hashClave
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// context: lo que Android usa para saber dónde está la app en el celular
// (carpetas, base de datos, recursos). Lo crea Android y lo recibimos desde el
// ViewModel (AndroidViewModel(application), como en la Guía 12) para pasárselo a la base.
class JuntaRepository(context: Context) {
    private val db = AppDatabase.obtener(context)
    private val usuarioDao = db.usuarioDao()
    private val pagoDao = db.pagoDao()
    private val reservaDao = db.reservaDao()
    private val dependenciaDao = db.dependenciaDao()

    suspend fun buscarUsuarioPorCorreo(correo: String): UsuarioEntity? {
        return usuarioDao.buscarPorCorreo(correo)
    }

    suspend fun obtenerDependencias(): List<DependenciaComunitariaEntity> {
        return dependenciaDao.obtenerTodas()
    }

    suspend fun crearReserva(reserva: ReservaEntity) {
        reservaDao.insertar(reserva)
    }

    suspend fun hayChoque(
        dependenciaId: Int,
        fecha: String,
        horaInicio: String,
        horaFin: String
    ): Boolean {
        val confirmadas = reservaDao.obtenerConfirmadas(dependenciaId, fecha)
        return confirmadas.any { it.horaInicio < horaFin && horaInicio < it.horaFin }
    }

    fun observarReservas(vecinoId: Int): Flow<List<ReservaEntity>> {
        return reservaDao.observarPorVecino(vecinoId)
    }

    suspend fun cancelarReserva(reserva: ReservaEntity) {
        reservaDao.actualizar(reserva.copy(estado = "CANCELADA"))
    }

    fun observarTodasLasReservas(): Flow<List<ReservaEntity>> {
        return reservaDao.observarTodas()
    }

    suspend fun obtenerUsuarios(): List<UsuarioEntity> {
        return usuarioDao.obtenerTodos()
    }

    suspend fun confirmarReserva(reserva: ReservaEntity, gestionadaPorId: Int): Boolean {
        if (hayChoque(reserva.dependenciaId, reserva.fecha, reserva.horaInicio, reserva.horaFin)) {
            return false
        }
        reservaDao.actualizar(reserva.copy(estado = "CONFIRMADA", gestionadaPorId = gestionadaPorId))
        return true
    }

    suspend fun rechazarReserva(reserva: ReservaEntity, gestionadaPorId: Int) {
        reservaDao.actualizar(reserva.copy(estado = "RECHAZADA", gestionadaPorId = gestionadaPorId))
    }

    fun observarPagos(): Flow<List<PagoEntity>> {
        return pagoDao.observarTodos()
    }

    fun observarReservasPagadas(): Flow<List<Int>> {
        return pagoDao.observarReservasPagadas()
    }

    suspend fun buscarReserva(id: Int): ReservaEntity? {
        return reservaDao.obtenerPorId(id)
    }

    suspend fun reservaPagada(id: Int): Boolean {
        return pagoDao.contarPagosDeArriendo(id) > 0
    }

    suspend fun obtenerReservasPorPagar(): List<ReservaEntity> {
        return reservaDao.obtenerConfirmadasSinPago()
    }

    suspend fun crearPago(pago: PagoEntity) {
        pagoDao.insertar(pago)
    }

    suspend fun cargarDatosDeEjemplo() {
        if (dependenciaDao.obtenerTodas().isNotEmpty()) return

        dependenciaDao.insertar(DependenciaComunitariaEntity(nombre = "Salón Principal"))
        dependenciaDao.insertar(DependenciaComunitariaEntity(nombre = "Sala Multiuso"))

        val clave = hashClave("1234")
        val usuarios = listOf(
            UsuarioEntity(nombre = "Juan Pérez", correo = "juan.perez@zaror.test", clave = clave,
                rol = "VECINO"),
            UsuarioEntity(nombre = "Ana Muñoz", correo = "ana.munoz@zaror.test", clave = clave,
                rol = "VECINO"),
            UsuarioEntity(nombre = "Pedro Soto", correo = "pedro.soto@zaror.test", clave = clave,
                rol = "VECINO"),
            UsuarioEntity(nombre = "María Torres", correo = "maria.torres@zaror.test", clave = clave,
                rol = "DIRECTIVA", cargo = "SECRETARIA"),
            UsuarioEntity(nombre = "Luis Rojas", correo = "luis.rojas@zaror.test", clave = clave,
                rol = "DIRECTIVA", cargo = "TESORERIA"),
            UsuarioEntity(nombre = "Carmen Vidal", correo = "carmen.vidal@zaror.test", clave = clave,
                rol = "DIRECTIVA", cargo = "PRESIDENCIA")
        )
        usuarios.forEach { usuarioDao.insertar(it) }

        val reservas = listOf(
            ReservaEntity(dependenciaId = 1, vecinoId = 1, fecha = fechaEn(11),
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
            PagoEntity(reservaId = 1, tipo = "ARRIENDO", monto = 50000,
                fecha = fechaEn(-1), registradoPorId = 5)
        )
        pagos.forEach { pagoDao.insertar(it) }
    }

    // Fecha de hoy más (o menos) algunos días, como texto: 2026-10-17.
    // Así los datos de ejemplo quedan siempre cerca del día en que se abre la app.
    // SimpleDateFormat se ve en MiAppFormulario, el ejemplo del docente.
    private fun fechaEn(dias: Int): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_YEAR, dias)
        return SimpleDateFormat("yyyy-MM-dd",
            Locale.getDefault()).format(calendario.time)
    }
}