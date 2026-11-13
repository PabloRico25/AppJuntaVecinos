package com.example.appjuntavecinos

import android.app.Application
import com.example.appjuntavecinos.data.model.UsuarioEntity
import com.example.appjuntavecinos.data.remote.SesionToken
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.viewmodel.LoginViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : StringSpec({

    // viewModelScope usa el hilo principal de Android, que no existe en una prueba: se reemplaza
    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    val maria = UsuarioEntity(id = 4, nombre = "María Torres", correo = "maria.torres@zaror.test",
        clave = "", rol = "DIRECTIVA", cargo = "SECRETARIA")
    val luis = UsuarioEntity(id = 5, nombre = "Luis Rojas", correo = "luis.rojas@zaror.test",
        clave = "", rol = "DIRECTIVA", cargo = "TESORERIA")
    val juan = UsuarioEntity(id = 1, nombre = "Juan Pérez", correo = "juan.perez@zaror.test",
        clave = "", rol = "VECINO")

    fun crear(repositorio: JuntaRepository) = LoginViewModel(mockk<Application>(relaxed = true), repositorio)

    fun LoginViewModel.escribir(correo: String, clave: String) {
        onCorreoChange(correo)
        onClaveChange(clave)
    }

    "con el correo vacío se avisa y no se llama al servidor" {
        val repositorio = mockk<JuntaRepository>()
        val vm = crear(repositorio)
        vm.escribir("", "1234")

        vm.iniciarSesion { }

        vm.estado.value.errores.correo shouldBe "Escribe tu correo"
        coVerify(exactly = 0) { repositorio.iniciarSesion(any(), any()) }
    }

    "un correo sin forma de correo se rechaza" {
        val vm = crear(mockk())
        vm.escribir("juan", "1234")

        vm.validarFormulario() shouldBe false
        vm.estado.value.errores.correo shouldBe "Escribe un correo válido"
    }

    "una clave de menos de 4 caracteres se rechaza" {
        val vm = crear(mockk())
        vm.escribir("juan.perez@zaror.test", "123")

        vm.validarFormulario() shouldBe false
        vm.estado.value.errores.clave shouldBe "La clave debe tener al menos 4 caracteres"
    }

    "escribir en un campo borra el error que tenía" {
        val vm = crear(mockk())
        vm.escribir("", "")
        vm.validarFormulario()

        vm.onCorreoChange("j")

        vm.estado.value.errores.correo.shouldBeNull()
    }

    "un login correcto guarda la sesión y avisa que entró" {
        val repositorio = mockk<JuntaRepository>()
        coEvery { repositorio.iniciarSesion("maria.torres@zaror.test", "1234") } returns maria
        val vm = crear(repositorio)
        vm.escribir("maria.torres@zaror.test", "1234")
        var entro = false

        vm.iniciarSesion { entro = true }

        entro shouldBe true
        vm.sesion.value?.nombre shouldBe "María Torres"
        vm.estado.value.cargando shouldBe false
    }

    "si el servidor rechaza el login se muestra su mensaje y no se entra" {
        val repositorio = mockk<JuntaRepository>()
        coEvery { repositorio.iniciarSesion(any(), any()) } throws errorHttp(401, "Correo o clave incorrectos")
        val vm = crear(repositorio)
        vm.escribir("juan.perez@zaror.test", "9999")
        var entro = false

        vm.iniciarSesion { entro = true }

        entro shouldBe false
        vm.sesion.value.shouldBeNull()
        vm.estado.value.errores.general shouldBe "Correo o clave incorrectos"
        vm.estado.value.cargando shouldBe false
    }

    "cerrar la sesión borra el token, el usuario y el formulario" {
        val repositorio = mockk<JuntaRepository>()
        coEvery { repositorio.iniciarSesion(any(), any()) } returns juan
        val vm = crear(repositorio)
        vm.escribir("juan.perez@zaror.test", "1234")
        vm.iniciarSesion { }
        SesionToken.token = "un-token"

        vm.cerrarSesion()

        SesionToken.token.shouldBeNull()
        vm.sesion.value.shouldBeNull()
        vm.estado.value.correo shouldBe ""
    }

    "solo la Secretaría confirma reservas y solo Tesorería registra pagos" {
        val repositorio = mockk<JuntaRepository>()

        for ((usuario, confirma, paga) in listOf(
            Triple(maria, true, false),
            Triple(luis, false, true),
            Triple(juan, false, false)
        )) {
            coEvery { repositorio.iniciarSesion(any(), any()) } returns usuario
            val vm = crear(repositorio)
            vm.escribir(usuario.correo, "1234")
            vm.iniciarSesion { }

            vm.puedeConfirmarReservas() shouldBe confirma
            vm.puedeRegistrarPagos() shouldBe paga
        }
    }

    "sin sesión nadie puede confirmar ni registrar pagos" {
        val vm = crear(mockk())

        vm.puedeConfirmarReservas() shouldBe false
        vm.puedeRegistrarPagos() shouldBe false
    }
})
