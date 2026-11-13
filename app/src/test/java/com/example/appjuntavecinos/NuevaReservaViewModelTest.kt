package com.example.appjuntavecinos

import android.app.Application
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.viewmodel.NuevaReservaViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class NuevaReservaViewModelTest : StringSpec({

    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    val salon = DependenciaComunitariaEntity(id = 1, nombre = "Salón Principal")

    // El año 2100 y el año 2020, en milisegundos como los entrega el selector de fecha (UTC)
    val en2100 = 4102444800000L
    val en2020 = 1577836800000L

    fun crear(repositorio: JuntaRepository): NuevaReservaViewModel {
        coEvery { repositorio.obtenerDependencias() } returns listOf(salon)
        return NuevaReservaViewModel(mockk<Application>(relaxed = true), repositorio)
    }

    fun NuevaReservaViewModel.llenarCorrectamente() {
        onDependenciaChange(salon)
        onFechaChange(en2100)
        onHoraInicioChange("18:00")
        onHoraFinChange("20:00")
        onReglamentoChange(true)
    }

    "al abrir la pantalla se cargan las dependencias" {
        val vm = crear(mockk())

        vm.dependencias.value shouldBe listOf(salon)
    }

    "con el formulario vacío se marcan los campos obligatorios y no se llama al servidor" {
        val repositorio = mockk<JuntaRepository>()
        val vm = crear(repositorio)

        vm.guardar(1) { }

        val errores = vm.estado.value.errores
        errores.dependencia shouldBe "Elige una dependencia"
        errores.fecha shouldBe "Elige una fecha"
        errores.horaInicio shouldBe "Elige la hora de inicio"
        errores.horaFin shouldBe "Elige la hora de fin"
        errores.reglamento shouldBe "Debes aceptar el reglamento de la sede"
        coVerify(exactly = 0) { repositorio.crearReserva(any(), any(), any(), any()) }
    }

    "una fecha pasada se rechaza" {
        val vm = crear(mockk())
        vm.llenarCorrectamente()
        vm.onFechaChange(en2020)

        vm.validarFormulario() shouldBe false
        vm.estado.value.errores.fecha shouldBe "La fecha no puede ser pasada"
    }

    "la hora de fin debe ser mayor que la de inicio" {
        val vm = crear(mockk())
        vm.llenarCorrectamente()
        vm.onHoraFinChange("18:00")

        vm.validarFormulario() shouldBe false
        vm.estado.value.errores.horaFin shouldBe "La hora de fin debe ser mayor que la de inicio"
    }

    "cambiar un campo borra su error" {
        val vm = crear(mockk())
        vm.guardar(1) { }

        vm.onDependenciaChange(salon)

        vm.estado.value.errores.dependencia.shouldBeNull()
    }

    "un formulario correcto pide la reserva al servidor y avisa que salió bien" {
        val repositorio = mockk<JuntaRepository>()
        val vm = crear(repositorio)
        coEvery { repositorio.crearReserva(any(), any(), any(), any()) } just Runs
        vm.llenarCorrectamente()
        var salio = false

        vm.guardar(1) { salio = true }

        salio shouldBe true
        coVerify(exactly = 1) { repositorio.crearReserva(1, "2100-01-01", "18:00", "20:00") }
    }

    "si el servidor responde con un error se muestra su mensaje y no se avisa éxito" {
        val repositorio = mockk<JuntaRepository>()
        val vm = crear(repositorio)
        coEvery { repositorio.crearReserva(any(), any(), any(), any()) } throws
            errorHttp(409, "Ese horario ya está ocupado por una reserva confirmada")
        vm.llenarCorrectamente()
        var salio = false

        vm.guardar(1) { salio = true }

        salio shouldBe false
        vm.estado.value.errores.general shouldBe "Ese horario ya está ocupado por una reserva confirmada"
    }
})
