package com.example.appjuntavecinos

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.example.appjuntavecinos.data.model.BloqueDto
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.model.LoginRequest
import com.example.appjuntavecinos.data.model.LoginResponse
import com.example.appjuntavecinos.data.model.NuevaReservaRequest
import com.example.appjuntavecinos.data.model.NuevoPagoRequest
import com.example.appjuntavecinos.data.model.PagoEntity
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.model.UsuarioDto
import com.example.appjuntavecinos.data.remote.ApiService
import com.example.appjuntavecinos.data.remote.SesionToken
import com.example.appjuntavecinos.data.repository.AppDatabase
import com.example.appjuntavecinos.data.repository.DependenciaDao
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.data.repository.PagoDao
import com.example.appjuntavecinos.data.repository.ReservaDao
import com.example.appjuntavecinos.data.repository.UsuarioDao
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import retrofit2.HttpException
import java.io.IOException

class JuntaRepositoryTest : StringSpec({

    // withTransaction es una función de extensión de Room que necesita una base real:
    // aquí se reemplaza por una que simplemente ejecuta el bloque.
    beforeSpec {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { any<RoomDatabase>().withTransaction(any<suspend () -> Any?>()) } coAnswers {
            secondArg<suspend () -> Any?>().invoke()
        }
    }
    afterSpec { unmockkStatic("androidx.room.RoomDatabaseKt") }

    // Todo lo que el repositorio necesita, falso: la API y Room con sus cuatro DAO
    class Entorno {
        val api = mockk<ApiService>()
        val usuarioDao = mockk<UsuarioDao>(relaxed = true)
        val pagoDao = mockk<PagoDao>(relaxed = true)
        val reservaDao = mockk<ReservaDao>(relaxed = true)
        val dependenciaDao = mockk<DependenciaDao>(relaxed = true)
        val repositorio: JuntaRepository

        init {
            val db = mockk<AppDatabase>()
            every { db.usuarioDao() } returns usuarioDao
            every { db.pagoDao() } returns pagoDao
            every { db.reservaDao() } returns reservaDao
            every { db.dependenciaDao() } returns dependenciaDao
            repositorio = JuntaRepository(db, api)
        }
    }

    val maria = UsuarioDto(4, "María Torres", "maria.torres@zaror.test", "DIRECTIVA", "SECRETARIA")
    val reserva = ReservaEntity(id = 1, dependenciaId = 1, vecinoId = 3, fecha = "2026-11-20",
        horaInicio = "18:00", horaFin = "20:00", estado = "PENDIENTE", fechaSolicitud = "2026-11-13")

    "iniciar sesión guarda el token y devuelve el usuario sin su clave" {
        val e = Entorno()
        SesionToken.token = null
        coEvery { e.api.login(any()) } returns LoginResponse("token-de-maria", maria)

        val usuario = e.repositorio.iniciarSesion("maria.torres@zaror.test", "1234")

        SesionToken.token shouldBe "token-de-maria"
        usuario.nombre shouldBe "María Torres"
        usuario.cargo shouldBe "SECRETARIA"
        usuario.clave shouldBe ""
    }

    "si el servidor rechaza el login se lanza el error y no queda token" {
        val e = Entorno()
        SesionToken.token = null
        coEvery { e.api.login(any()) } throws errorHttp(401, "Correo o clave incorrectos")

        shouldThrow<HttpException> { e.repositorio.iniciarSesion("juan.perez@zaror.test", "9999") }

        SesionToken.token.shouldBeNull()
    }

    "activar la puerta pide un login con el dispositivo PUERTA y devuelve su token" {
        val e = Entorno()
        val pedido = slot<LoginRequest>()
        coEvery { e.api.login(capture(pedido)) } returns LoginResponse("token-de-7-dias", maria)

        val token = e.repositorio.activarPuerta("maria.torres@zaror.test", "1234")

        token shouldBe "token-de-7-dias"
        pedido.captured.dispositivo shouldBe "PUERTA"
    }

    "los bloques ocupados se piden al servidor con la sala y la fecha" {
        val e = Entorno()
        val bloques = listOf(BloqueDto("18:00", "20:00", "CONFIRMADA", null))
        coEvery { e.api.obtenerOcupacion(1, "2026-11-20") } returns bloques

        e.repositorio.obtenerBloquesOcupados(1, "2026-11-20") shouldBe bloques
    }

    "refrescar las reservas reemplaza lo guardado en Room y avisa que pudo" {
        val e = Entorno()
        val lista = listOf(reserva)
        coEvery { e.api.obtenerReservas(null) } returns lista

        e.repositorio.refrescarReservas() shouldBe true

        coVerify(exactly = 1) { e.reservaDao.borrarTodas() }
        coVerify(exactly = 1) { e.reservaDao.insertarTodas(lista) }
    }

    "sin conexión refrescar avisa que no pudo y no toca lo guardado" {
        val e = Entorno()
        coEvery { e.api.obtenerReservas(null) } throws IOException("sin red")

        e.repositorio.refrescarReservas() shouldBe false

        coVerify(exactly = 0) { e.reservaDao.borrarTodas() }
    }

    "sin conexión las dependencias salen de lo guardado en Room" {
        val e = Entorno()
        val guardadas = listOf(DependenciaComunitariaEntity(1, "Salón Principal"))
        coEvery { e.api.obtenerDependencias() } throws IOException("sin red")
        coEvery { e.dependenciaDao.obtenerTodas() } returns guardadas

        e.repositorio.obtenerDependencias() shouldBe guardadas
    }

    "las reservas pagadas las informa el servidor" {
        val e = Entorno()
        coEvery { e.api.obtenerReservasPagadas() } returns listOf(7, 8)

        e.repositorio.obtenerReservasPagadas() shouldBe listOf(7, 8)
    }

    "sin conexión las reservas pagadas se calculan con los arriendos guardados" {
        val e = Entorno()
        coEvery { e.api.obtenerReservasPagadas() } throws IOException("sin red")
        coEvery { e.pagoDao.obtenerTodos() } returns listOf(
            PagoEntity(reservaId = 3, tipo = "ARRIENDO", monto = 40000, fecha = "2026-11-10", registradoPorId = 5),
            PagoEntity(tipo = "CONSUMO_BASICO", categoria = "AGUA", monto = 24200, fecha = "2026-11-10", registradoPorId = 5),
            PagoEntity(reservaId = 9, tipo = "ARRIENDO", monto = 60000, fecha = "2026-11-11", registradoPorId = 5)
        )

        e.repositorio.obtenerReservasPagadas() shouldBe listOf(3, 9)
    }

    "crear una reserva la pide al servidor y después refresca las guardadas" {
        val e = Entorno()
        val pedido = slot<NuevaReservaRequest>()
        coEvery { e.api.crearReserva(capture(pedido)) } returns reserva
        coEvery { e.api.obtenerReservas(null) } returns listOf(reserva)

        e.repositorio.crearReserva(1, "2026-11-20", "18:00", "20:00")

        pedido.captured shouldBe NuevaReservaRequest(1, "2026-11-20", "18:00", "20:00")
        coVerify(exactly = 1) { e.api.obtenerReservas(null) }
    }

    "si el servidor rechaza la reserva el error llega a quien llamó y no se refresca" {
        val e = Entorno()
        coEvery { e.api.crearReserva(any()) } throws errorHttp(409, "Ese horario ya está ocupado por una reserva confirmada")

        shouldThrow<HttpException> { e.repositorio.crearReserva(1, "2026-11-20", "18:00", "20:00") }

        coVerify(exactly = 0) { e.api.obtenerReservas(any()) }
    }

    "registrar un pago lo envía al servidor y refresca los pagos guardados" {
        val e = Entorno()
        val pedido = slot<NuevoPagoRequest>()
        val pago = PagoEntity(id = 1, reservaId = 1, tipo = "ARRIENDO", monto = 30000,
            fecha = "2026-11-13", registradoPorId = 5)
        coEvery { e.api.registrarPago(capture(pedido)) } returns pago
        coEvery { e.api.obtenerPagos() } returns listOf(pago)

        e.repositorio.crearPago(1, "ARRIENDO", "", 30000, "2026-11-13", null, "")

        pedido.captured.monto shouldBe 30000
        pedido.captured.reservaId shouldBe 1
        coVerify(exactly = 1) { e.pagoDao.insertarTodos(listOf(pago)) }
    }
})
