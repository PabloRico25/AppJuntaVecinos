package com.example.appjuntavecinos

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.repository.*
import com.example.appjuntavecinos.utils.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReservasRoomTest {
    private lateinit var context: Context
    private lateinit var base: AppDatabase
    private lateinit var repositorio: JuntaRepository

    @Before fun preparar() = runBlocking {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        base = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repositorio = JuntaRepository(context, base)
        repositorio.cargarDatosDeEjemplo()
    }
    @After fun cerrar() { base.close() }

    private fun solicitud(inicio: String = "10:00", fin: String = "12:00") =
        ReservaEntity(dependenciaId = 2, vecinoId = 2, fecha = fechaEn(1),
            horaInicio = inicio, horaFin = fin, fechaSolicitud = fechaDeHoy())

    private suspend fun debeRechazar(accion: suspend () -> Unit) {
        try { accion(); fail("La operación debía ser rechazada") }
        catch (_: IllegalArgumentException) { }
    }

    @Test fun cargaUnaVezYConservaAnaSinReservas() = runBlocking {
        repositorio.cargarDatosDeEjemplo()
        assertEquals(6, repositorio.obtenerUsuarios().size)
        assertEquals(2, repositorio.obtenerDependencias().size)
        assertTrue(base.reservaDao().obtenerPorVecino(2).isEmpty())
        assertEquals(5, base.pagoDao().obtenerTodos().size)
        val usuario = repositorio.buscarUsuarioPorCorreo("ana.munoz@zaror.test")!!
        assertEquals(hashClave("1234"), usuario.clave)
        assertNotEquals("1234", usuario.clave)
    }

    @Test fun dosPendientesPuedenCoincidirPeroSoloUnaSeConfirma() = runBlocking {
        val primera = repositorio.crearReserva(solicitud()).toInt()
        val segunda = repositorio.crearReserva(solicitud("11:00", "13:00")).toInt()
        repositorio.gestionarReserva(primera, 4, true)
        debeRechazar { repositorio.gestionarReserva(segunda, 4, true) }
        assertEquals("PENDIENTE", base.reservaDao().obtenerPorId(segunda)?.estado)
        assertEquals(4, base.reservaDao().obtenerPorId(primera)?.gestionadaPorId)
    }

    @Test fun tesoreriaYPresidenciaNoPuedenConfirmar() = runBlocking {
        val id = repositorio.crearReserva(solicitud()).toInt()
        debeRechazar { repositorio.gestionarReserva(id, 5, true) }
        debeRechazar { repositorio.gestionarReserva(id, 6, false) }
        assertEquals("PENDIENTE", base.reservaDao().obtenerPorId(id)?.estado)
    }

    @Test fun cancelarExigePropietarioYPendiente() = runBlocking {
        val id = repositorio.crearReserva(solicitud()).toInt()
        val reserva = base.reservaDao().obtenerPorId(id)!!
        debeRechazar { repositorio.cancelarReserva(reserva, 1) }
        repositorio.cancelarReserva(reserva, 2)
        assertEquals("CANCELADA", base.reservaDao().obtenerPorId(id)?.estado)
        debeRechazar { repositorio.gestionarReserva(id, 4, true) }
        debeRechazar { repositorio.cancelarReserva(base.reservaDao().obtenerPorId(1)!!, 1) }
    }

    @Test fun confirmadoBloqueaCrearPeroPermiteHorarioContiguo() = runBlocking {
        val id = repositorio.crearReserva(solicitud()).toInt()
        repositorio.gestionarReserva(id, 4, true)
        debeRechazar { repositorio.crearReserva(solicitud("11:00", "13:00")) }
        assertTrue(repositorio.crearReserva(solicitud("12:00", "14:00")) > 0)
    }

    @Test fun reservaPersisteAlCerrarYReabrirLaBase() = runBlocking {
        val nombre = "prueba-reservas-${System.nanoTime()}.db"
        var archivo = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()
        try {
            val repoArchivo = JuntaRepository(context, archivo)
            repoArchivo.cargarDatosDeEjemplo()
            val id = repoArchivo.crearReserva(solicitud()).toInt()
            archivo.close()
            archivo = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()
            assertEquals("PENDIENTE", archivo.reservaDao().obtenerPorId(id)?.estado)
            assertEquals(2, archivo.reservaDao().obtenerPorId(id)?.vecinoId)
        } finally { archivo.close(); context.deleteDatabase(nombre) }
    }
}
