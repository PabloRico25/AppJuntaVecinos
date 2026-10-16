package com.example.appjuntavecinos.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.utils.*
import kotlinx.coroutines.flow.Flow

// Si una reserva acaba a las 12:00, otra puede empezar a las 12:00.
fun horariosSeCruzan(inicioA: String, finA: String, inicioB: String, finB: String): Boolean =
    inicioA < finB && inicioB < finA

class JuntaRepository(context: Context, private val base: AppDatabase = AppDatabase.obtener(context)) {
    suspend fun buscarUsuarioPorCorreo(correo: String) = base.usuarioDao().buscarPorCorreo(correo)
    suspend fun obtenerUsuarios() = base.usuarioDao().obtenerTodos()
    suspend fun obtenerDependencias() = base.dependenciaDao().obtenerTodas()
    fun observarReservas(vecinoId: Int) = base.reservaDao().observarPorVecino(vecinoId)
    fun observarTodasLasReservas() = base.reservaDao().observarTodas()
    fun observarPagos(): Flow<List<PagoEntity>> = base.pagoDao().observarTodos()

    suspend fun hayChoque(dependenciaId: Int, fecha: String, horaInicio: String, horaFin: String): Boolean =
        base.reservaDao().obtenerConfirmadas(dependenciaId, fecha).any {
            horariosSeCruzan(it.horaInicio, it.horaFin, horaInicio, horaFin)
        }

    suspend fun crearReserva(reserva: ReservaEntity): Long = base.withTransaction {
        require(base.usuarioDao().obtenerPorId(reserva.vecinoId)?.rol == "VECINO") {
            "Solo un vecino puede solicitar una reserva"
        }
        require(obtenerDependencias().any { it.id == reserva.dependenciaId }) { "Elige una dependencia válida" }
        require(fechaEsValida(reserva.fecha) && reserva.fecha >= fechaDeHoy()) { "La fecha no puede ser pasada" }
        require(Regex("([01]\\d|2[0-3]):[0-5]\\d").matches(reserva.horaInicio) &&
            Regex("([01]\\d|2[0-3]):[0-5]\\d").matches(reserva.horaFin) &&
            reserva.horaInicio >= "09:00" && reserva.horaFin <= "22:00" &&
            reserva.horaInicio < reserva.horaFin) { "Revisa el horario de la reserva (09:00 a 22:00)" }
        require(!hayChoque(reserva.dependenciaId, reserva.fecha, reserva.horaInicio, reserva.horaFin)) {
            "Ese horario ya está ocupado por una reserva confirmada"
        }
        base.reservaDao().insertar(reserva.copy(id = 0, estado = "PENDIENTE", gestionadaPorId = null))
    }

    // Releer y cambiar dentro de una transacción evita confirmar dos solicitudes que se cruzan.
    suspend fun gestionarReserva(id: Int, gestionadaPorId: Int, confirmar: Boolean) = base.withTransaction {
        val usuario = base.usuarioDao().obtenerPorId(gestionadaPorId)
        require(usuario?.rol == "DIRECTIVA" && usuario.cargo == "SECRETARIA") {
            "Solo la Secretaría puede confirmar o rechazar reservas"
        }
        val actual = requireNotNull(base.reservaDao().obtenerPorId(id)) { "La reserva ya no existe" }
        require(actual.estado == "PENDIENTE") { "Esta solicitud ya fue gestionada" }
        if (confirmar) {
            require(actual.fecha >= fechaDeHoy()) { "No se puede confirmar una fecha pasada" }
            require(!hayChoque(actual.dependenciaId, actual.fecha, actual.horaInicio, actual.horaFin)) {
                "No se puede confirmar: ese horario ya está ocupado"
            }
        }
        base.reservaDao().actualizar(actual.copy(
            estado = if (confirmar) "CONFIRMADA" else "RECHAZADA",
            gestionadaPorId = gestionadaPorId
        ))
    }

    suspend fun cancelarReserva(reserva: ReservaEntity, vecinoId: Int) = base.withTransaction {
        val actual = requireNotNull(base.reservaDao().obtenerPorId(reserva.id)) { "La reserva ya no existe" }
        require(actual.vecinoId == vecinoId) { "Solo puedes cancelar tus propias solicitudes" }
        require(actual.estado == "PENDIENTE") { "Solo puedes cancelar solicitudes pendientes" }
        base.reservaDao().actualizar(actual.copy(estado = "CANCELADA"))
    }

    suspend fun cargarDatosDeEjemplo() = base.withTransaction {
        if (obtenerDependencias().isNotEmpty()) return@withTransaction
        val usuarios = listOf(
            UsuarioEntity(1, "Juan Pérez", "juan.perez@zaror.test", hashClave("1234"), "VECINO"),
            UsuarioEntity(2, "Ana Muñoz", "ana.munoz@zaror.test", hashClave("1234"), "VECINO"),
            UsuarioEntity(3, "Pedro Soto", "pedro.soto@zaror.test", hashClave("1234"), "VECINO"),
            UsuarioEntity(4, "María Torres", "maria.torres@zaror.test", hashClave("1234"), "DIRECTIVA", "SECRETARIA"),
            UsuarioEntity(5, "Luis Rojas", "luis.rojas@zaror.test", hashClave("1234"), "DIRECTIVA", "TESORERIA"),
            UsuarioEntity(6, "Carmen Vidal", "carmen.vidal@zaror.test", hashClave("1234"), "DIRECTIVA", "PRESIDENCIA")
        )
        usuarios.forEach { base.usuarioDao().insertar(it) }
        base.dependenciaDao().insertar(DependenciaComunitariaEntity(1, "Salón Principal"))
        base.dependenciaDao().insertar(DependenciaComunitariaEntity(2, "Sala Multiuso"))
        val hoy = fechaDeHoy()
        val reservas = listOf(
            ReservaEntity(1, 1, 1, fechaEn(11), "18:00", "21:00", "CONFIRMADA", hoy, 4),
            ReservaEntity(2, 2, 1, fechaEn(12), "11:00", "14:00", "PENDIENTE", hoy),
            ReservaEntity(3, 1, 3, fechaEn(11), "19:00", "21:00", "PENDIENTE", hoy),
            ReservaEntity(4, 2, 3, fechaEn(25), "10:00", "13:00", "PENDIENTE", hoy),
            ReservaEntity(5, 1, 1, fechaEn(18), "15:00", "18:00", "RECHAZADA", hoy, 4),
            ReservaEntity(6, 2, 1, fechaEn(4), "10:00", "12:00", "CANCELADA", hoy),
            ReservaEntity(7, 2, 3, fechaEn(-3), "10:00", "13:00", "CONFIRMADA", fechaEn(-10), 4),
            ReservaEntity(8, 1, 3, fechaEn(-3), "12:00", "16:00", "CONFIRMADA", fechaEn(-10), 4)
        )
        reservas.forEach { base.reservaDao().insertar(it) }
        val pagos = listOf(
            PagoEntity(reservaId = 8, tipo = "ARRIENDO", monto = 60000.0, fecha = fechaEn(-2), registradoPorId = 5),
            PagoEntity(tipo = "CONSUMO_BASICO", categoria = "ELECTRICIDAD", monto = 38500.0, fecha = fechaEn(-2), registradoPorId = 5),
            PagoEntity(reservaId = 7, tipo = "ARRIENDO", monto = 40000.0, fecha = fechaEn(-3), registradoPorId = 5),
            PagoEntity(tipo = "CONSUMO_BASICO", categoria = "AGUA", monto = 24200.0, fecha = fechaEn(-4), registradoPorId = 5),
            PagoEntity(tipo = "CONSUMO_BASICO", categoria = "GAS", monto = 24000.0, fecha = fechaEn(-5), registradoPorId = 5)
        )
        pagos.forEach { base.pagoDao().insertar(it) }
    }
}
