package com.example.appjuntavecinos

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appjuntavecinos.data.model.*
import com.example.appjuntavecinos.data.repository.*
import com.example.appjuntavecinos.utils.*
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PagosRecursosTest {
    private lateinit var context: Context
    private lateinit var base: AppDatabase
    private lateinit var repo: JuntaRepository
    @Before fun preparar() = runBlocking {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        base = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repo = JuntaRepository(context, base)
        repo.cargarDatosDeEjemplo()
    }
    @After fun cerrar() { base.close() }
    private fun pago() = PagoEntity(reservaId = 1, tipo = "ARRIENDO", monto = 15000.0,
        fecha = fechaDeHoy(), registradoPorId = 5)
    private suspend fun rechaza(accion: suspend () -> Unit) {
        try { accion(); fail("Debía rechazar la operación") } catch (_: IllegalArgumentException) { }
    }
    @Test fun soloTesoreriaRegistraUnaReservaConfirmada() = runBlocking {
        listOf(1, 4, 6).forEach { rechaza { repo.crearPago(pago().copy(registradoPorId = it)) } }
        rechaza { repo.crearPago(pago().copy(reservaId = 2)) }
        rechaza { repo.crearPago(pago().copy(monto = Double.NaN)) }
        rechaza { repo.crearPago(pago().copy(monto = 1.5)) }
        assertTrue(repo.crearPago(pago()) > 0)
        assertTrue(repo.reservaPagada(1))
        assertFalse(repo.obtenerReservasPorPagar().any { it.id == 1 })
    }
    @Test fun dosPagosSimultaneosNoDuplicanElArriendo() = runBlocking {
        val resultados = (1..2).map {
            async(Dispatchers.IO) { runCatching { repo.crearPago(pago()) }.isSuccess }
        }.awaitAll()
        assertEquals(1, resultados.count { it })
        assertEquals(1, base.pagoDao().contarPagosDeArriendo(1))
    }
    @Test fun gastoSeGuardaSinReservaYConCategoria() = runBlocking {
        val gasto = pago().copy(tipo = "CONSUMO_BASICO", categoria = "GAS", descripcion = "Prueba")
        val id = repo.crearPago(gasto).toInt()
        val guardado = base.pagoDao().obtenerTodos().first { it.id == id }
        assertNull(guardado.reservaId)
        assertEquals("GAS", guardado.categoria)
        rechaza { repo.crearPago(gasto.copy(categoria = "")) }
        rechaza { repo.crearPago(gasto.copy(fecha = fechaEn(1))) }
    }
    @Test fun comprobanteSeCopiaYNoDependeDelArchivoDeOrigen() {
        val origen = File.createTempFile("prueba-", ".png", context.cacheDir)
        var copia: String? = null
        try {
            val bitmap = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.BLUE)
            origen.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            copia = guardarComprobante(context, Uri.fromFile(origen))
            assertNotNull(copia)
            origen.delete()
            assertEquals(40, BitmapFactory.decodeFile(Uri.parse(copia).path).width)
        } finally { origen.delete(); copia?.let { eliminarComprobante(context, it) } }
    }
    @Test fun archivoQueNoEsImagenSeRechazaSinDejarCopia() {
        val falso = File.createTempFile("falso-", ".png", context.cacheDir)
        val antes = File(context.filesDir, "comprobantes").list()?.size ?: 0
        try {
            falso.writeText("esto no es una imagen")
            assertNull(guardarComprobante(context, Uri.fromFile(falso)))
            assertEquals(antes, File(context.filesDir, "comprobantes").list()?.size ?: 0)
        } finally { falso.delete() }
    }
    @Test fun qrGeneradoEsLeidoPorMlKitConNombreAcentuado() = runBlocking {
        val contenido = armarContenidoQr(repo.buscarReserva(1)!!, "Ana Muñoz")
        val lector = BarcodeScanning.getClient()
        try {
            val resultado = Tasks.await(lector.process(InputImage.fromBitmap(generarQr(contenido), 0)), 20, TimeUnit.SECONDS)
            assertEquals(contenido, resultado.single().rawValue)
        } finally { lector.close() }
    }
    @Test fun migracionConservaDatosYAgregaIngresoPersistente() = runBlocking {
        // Esquema 2 publicado en schemas/: archivo antiguo de prueba, sin tocar la base del usuario.
        val nombre = "migracion-${System.nanoTime()}.db"
        val archivo = context.getDatabasePath(nombre)
        archivo.parentFile!!.mkdirs()
        val anterior = SQLiteDatabase.openOrCreateDatabase(archivo, null)
        anterior.execSQL("CREATE TABLE UsuarioEntity (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT NOT NULL, correo TEXT NOT NULL, clave TEXT NOT NULL, rol TEXT NOT NULL, cargo TEXT NOT NULL)")
        anterior.execSQL("CREATE UNIQUE INDEX index_UsuarioEntity_correo ON UsuarioEntity (correo)")
        anterior.execSQL("CREATE TABLE DependenciaComunitariaEntity (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT NOT NULL)")
        anterior.execSQL("CREATE TABLE ReservaEntity (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, dependenciaId INTEGER NOT NULL, vecinoId INTEGER NOT NULL, fecha TEXT NOT NULL, horaInicio TEXT NOT NULL, horaFin TEXT NOT NULL, estado TEXT NOT NULL, fechaSolicitud TEXT NOT NULL, gestionadaPorId INTEGER)")
        anterior.execSQL("CREATE TABLE PagoEntity (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, reservaId INTEGER, tipo TEXT NOT NULL, categoria TEXT NOT NULL, monto REAL NOT NULL, fecha TEXT NOT NULL, comprobanteUri TEXT, registradoPorId INTEGER NOT NULL, descripcion TEXT NOT NULL)")
        anterior.execSQL("INSERT INTO UsuarioEntity VALUES(99, 'Vecino anterior', 'antes@zaror.test', 'hash', 'VECINO', '')")
        anterior.execSQL("INSERT INTO ReservaEntity VALUES(99, 1, 99, '2026-10-20', '18:00', '20:00', 'CONFIRMADA', '2026-10-17', 4)")
        anterior.execSQL("INSERT INTO PagoEntity VALUES(99, 99, 'ARRIENDO', '', 15000, '2026-10-17', null, 5, 'Pago anterior')")
        anterior.version = 2
        anterior.close()
        var nueva = Room.databaseBuilder(context, AppDatabase::class.java, nombre).addMigrations(AppDatabase.MIGRACION_2_3).build()
        try {
            assertEquals("Vecino anterior", nueva.usuarioDao().obtenerPorId(99)?.nombre)
            assertEquals("CONFIRMADA", nueva.reservaDao().obtenerPorId(99)?.estado)
            assertEquals("Pago anterior", nueva.pagoDao().obtenerTodos().single().descripcion)
            assertTrue(nueva.ingresoDao().insertar(IngresoEntity(99, "2026-10-20", "18:00")) > 0)
            assertEquals(-1L, nueva.ingresoDao().insertar(IngresoEntity(99, "2026-10-20", "18:01")))
            nueva.close()
            nueva = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()
            assertEquals(listOf(99), nueva.ingresoDao().obtenerReservasUsadas())
        } finally { nueva.close(); context.deleteDatabase(nombre) }
    }
}
