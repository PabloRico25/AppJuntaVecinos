package com.example.appjuntavecinos

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.repository.*
import com.example.appjuntavecinos.utils.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class PagosPuertaNavegacionTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun esperar(texto: String) {
        compose.waitUntil(20000) { compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun ingresar(correo: String) {
        esperar("Ingresar")
        compose.onNodeWithText("Correo").performTextReplacement(correo)
        compose.onNodeWithText("Clave").performTextReplacement("1234")
        compose.onNodeWithText("Ingresar").performScrollTo().performClick()
    }
    private fun salir() {
        compose.onNodeWithContentDescription("Cuenta").performClick()
        compose.onNodeWithText("Cerrar sesión").performClick()
        esperar("Ingresar")
    }
    @Test fun tesoreriaPagaYVecinoVeSuTicket() {
        val base = AppDatabase.obtener(context)
        val repo = JuntaRepository(context)
        var id = 0
        try {
            esperar("Ingresar")
            id = runBlocking {
                val nueva = repo.crearReserva(ReservaEntity(dependenciaId = 2, vecinoId = 2,
                    fecha = fechaEn(70), horaInicio = "09:00", horaFin = "10:00", fechaSolicitud = fechaDeHoy())).toInt()
                repo.gestionarReserva(nueva, 4, true)
                nueva
            }
            ingresar("luis.rojas@zaror.test")
            esperar("Solo la Secretaría puede confirmar o rechazar reservas.")
            compose.onNodeWithText("Pagos").performClick()
            esperar("Registrar pago")
            compose.onNodeWithText("Registrar pago").performClick()
            esperar("Guardar pago")
            compose.onNodeWithText("Guardar pago").performScrollTo().performClick()
            compose.onNodeWithText("Elige el tipo de pago").assertExists()
            compose.onNodeWithText("Escribe el monto").assertExists()
            compose.onNodeWithText("Arriendo").performScrollTo().performClick()
            compose.onNodeWithText("Reserva", substring = false).performScrollTo().performClick()
            compose.onNodeWithText("#$id ·", substring = true).performClick()
            compose.onNodeWithText("Monto en pesos").performScrollTo().performTextReplacement("-5")
            compose.onNodeWithText("Guardar pago").performScrollTo().performClick()
            compose.onNodeWithText("El monto debe ser mayor que 0").assertExists()
            compose.onNodeWithText("Monto en pesos").performScrollTo().performTextReplacement("15000")
            compose.onNodeWithText("Elegir fecha").performScrollTo().performClick()
            val patron = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yMMMMEEEEd")
            compose.onNodeWithText(SimpleDateFormat(patron, Locale.getDefault()).format(Date()), substring = true).performClick()
            compose.onNodeWithText("Aceptar").performClick()
            compose.onNodeWithText("Confirmo que este pago es un registro informativo.").performScrollTo().performClick()
            compose.onNodeWithText("Guardar pago").performScrollTo().performClick()
            esperar("Pago registrado")
            assertTrue(runBlocking { repo.reservaPagada(id) })
            salir()
            ingresar("ana.munoz@zaror.test")
            esperar("Nueva reserva")
            compose.onNodeWithText("Ver detalle #$id").performScrollTo().performClick()
            esperar("Ver ticket QR")
            compose.onNodeWithText("Ver ticket QR").performScrollTo().performClick()
            esperar("Ticket de ingreso")
            compose.onNodeWithContentDescription("Código QR de la reserva $id").assertExists()
            compose.onNodeWithText("Ingreso: 08:50 a 09:15").assertExists()
            salir()
        } finally {
            if (id > 0) runBlocking {
                base.openHelper.writableDatabase.execSQL("DELETE FROM PagoEntity WHERE reservaId = ?", arrayOf(id))
                base.openHelper.writableDatabase.execSQL("DELETE FROM ReservaEntity WHERE id = ?", arrayOf(id))
            }
        }
    }
    @Test fun presidenciaNoRegistraPagosYRechazaCodigoInventado() {
        ingresar("carmen.vidal@zaror.test")
        esperar("Solo la Secretaría puede confirmar o rechazar reservas.")
        compose.onNodeWithText("Pagos").performClick()
        esperar("Solo la Tesorería puede registrar pagos.")
        compose.onNodeWithText("Registrar pago").assertDoesNotExist()
        compose.onNodeWithText("Escanear").performClick()
        esperar("Validar un ticket")
        compose.onNodeWithText("Estás en").performClick()
        compose.onNodeWithText("Sala Multiuso").performClick()
        compose.onNodeWithText("Código").performScrollTo().performTextReplacement("inventado")
        compose.onNodeWithText("Validar código").performScrollTo().performClick()
        esperar("Código no válido o no disponible en este dispositivo")
        salir()
    }
    @Test fun puertaConservaConfiguracionYExigeClaveParaSalir() {
        val preferencias = PuertaPreferencias(context)
        val antes = runBlocking { preferencias.configuracion.first() }
        Assume.assumeFalse(antes.modoPuerta)
        try {
            esperar("Ingresar")
            repeat(7) { compose.onNodeWithText("Inicia sesión").performClick() }
            esperar("Activar modo puerta")
            compose.onNodeWithText("Activar").performClick()
            esperar("Elige la sala de esta puerta")
            compose.onNodeWithText("Sala de la puerta").performClick()
            compose.onNodeWithText("Sala Multiuso").performClick()
            compose.onNodeWithText("Clave de la puerta").performTextReplacement("puerta123")
            compose.onNodeWithText("Activar").performClick()
            esperar("Acerque su QR")
            compose.activityRule.scenario.recreate()
            esperar("Acerque su QR")
            assertEquals(hashClave("puerta123"), runBlocking { preferencias.configuracion.first().claveHash })
            compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
            esperar("Salir del modo puerta")
            compose.onNodeWithText("Clave de la puerta").performTextReplacement("mal")
            compose.onNodeWithText("Salir").performClick()
            esperar("Clave incorrecta")
            compose.onNodeWithText("Clave de la puerta").performTextReplacement("puerta123")
            compose.onNodeWithText("Salir").performClick()
            esperar("Ingresar")
            assertFalse(runBlocking { preferencias.configuracion.first().modoPuerta })
        } finally { runBlocking { preferencias.desactivar() } }
    }
}
