package com.example.appjuntavecinos

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appjuntavecinos.data.repository.AppDatabase
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginNavegacionTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun esperar(texto: String) {
        compose.waitUntil(20000) {
            compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
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

    @Test fun validaCamposYRechazaCredencialesIncorrectas() {
        esperar("Ingresar")
        compose.onNodeWithText("Ingresar").performScrollTo().performClick()
        compose.onNodeWithText("Escribe tu correo").assertExists()
        compose.onNodeWithText("Escribe tu clave").assertExists()
        compose.onNodeWithText("Correo").performTextReplacement("nadie@zaror.test")
        compose.onNodeWithText("Clave").performTextReplacement("incorrecta")
        compose.onNodeWithText("Ingresar").performScrollTo().performClick()
        esperar("Correo o clave incorrectos")
    }

    @Test fun vecinoPuedeNavegarYElFormularioMuestraErrores() {
        ingresar("ana.munoz@zaror.test")
        esperar("Nueva reserva")
        compose.onNodeWithText("Todavía no tienes reservas").assertExists()
        compose.onNodeWithText("Nueva reserva").performClick()
        esperar("Enviar solicitud")
        compose.onNodeWithText("Enviar solicitud").performScrollTo().performClick()
        compose.onNodeWithText("Elige una dependencia").assertExists()
        compose.onNodeWithText("Elige una fecha").assertExists()
        compose.onNodeWithContentDescription("Volver").performClick()
        compose.onNodeWithText("Disponibilidad").performClick()
        esperar("Libre: disponible. Solicitado: pendiente de revisión. Ocupado: reserva confirmada.")
        compose.onNodeWithText("Juan Pérez", substring = true).assertDoesNotExist()
        salir()
    }

    @Test fun presidenciaTieneConsultaSinBotonesDeGestion() {
        ingresar("carmen.vidal@zaror.test")
        esperar("Solo la Secretaría puede confirmar o rechazar reservas.")
        compose.onNodeWithText("Confirmar", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText("Rechazar", useUnmergedTree = true).assertDoesNotExist()
        salir()
    }

    @Test fun crearVerYCancelarUnaSolicitudDesdeLaPantalla() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val base = AppDatabase.obtener(context)
        val anteriores = runBlocking { base.reservaDao().obtenerPorVecino(2).map { it.id }.toSet() }
        try {
            ingresar("ana.munoz@zaror.test")
            esperar("Nueva reserva")
            compose.onNodeWithText("Nueva reserva").performClick()
            compose.onNodeWithText("Dependencia").performClick()
            compose.onNodeWithText("Sala Multiuso").performClick()
            compose.onNodeWithText("Elegir fecha").performScrollTo().performClick()
            val patron = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yMMMMEEEEd")
            val fechaCompleta = SimpleDateFormat(patron, Locale.getDefault()).format(Date())
            compose.onNodeWithText(fechaCompleta, substring = true).performClick()
            compose.onNodeWithText("Aceptar").performClick()
            compose.onNodeWithText("Hora de inicio").performScrollTo().performClick()
            compose.onNodeWithText("10:00").performScrollTo().performClick()
            compose.onNodeWithText("Hora de fin").performScrollTo().performClick()
            compose.onNodeWithText("12:00").performScrollTo().performClick()
            compose.onNode(isToggleable()).performScrollTo().performClick()
            compose.onNodeWithText("Enviar solicitud").performScrollTo().performClick()
            esperar("Nueva reserva")
            val id = runBlocking { base.reservaDao().obtenerPorVecino(2).first { it.id !in anteriores }.id }
            compose.onNodeWithText("Ver detalle #$id").performScrollTo().performClick()
            esperar("Cancelar solicitud")
            compose.onNodeWithText("Cancelar solicitud").performScrollTo().performClick()
            compose.onNodeWithText("Sí, cancelar").performClick()
            esperar("Cancelada")
            compose.onNodeWithText("Cancelar solicitud").assertDoesNotExist()
            salir()
        } finally {
            // Se retiran solo las filas creadas por esta prueba; no se borra la base de la app.
            runBlocking {
                base.reservaDao().obtenerPorVecino(2).filter { it.id !in anteriores }.forEach {
                    base.openHelper.writableDatabase.execSQL("DELETE FROM ReservaEntity WHERE id = ?", arrayOf(it.id))
                }
            }
        }
    }

    @Test fun sesionYFormularioSeConservanAlRecrearLaActividad() {
        ingresar("ana.munoz@zaror.test")
        esperar("Nueva reserva")
        compose.onNodeWithText("Nueva reserva").performClick()
        compose.onNodeWithText("Dependencia").performClick()
        compose.onNodeWithText("Sala Multiuso").performClick()
        compose.activityRule.scenario.recreate()
        esperar("Enviar solicitud")
        compose.onNodeWithText("Sala Multiuso").assertExists()
        compose.onNodeWithContentDescription("Volver").performClick()
        salir()
    }
}
