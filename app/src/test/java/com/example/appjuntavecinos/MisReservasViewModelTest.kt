package com.example.appjuntavecinos

import android.app.Application
import com.example.appjuntavecinos.data.model.ReservaEntity
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.viewmodel.MisReservasViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MisReservasViewModelTest : StringSpec({

    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    val pendiente = ReservaEntity(id = 1, dependenciaId = 1, vecinoId = 1, fecha = "2026-11-25",
        horaInicio = "11:00", horaFin = "14:00", estado = "PENDIENTE", fechaSolicitud = "2026-11-13")

    fun repositorio(): JuntaRepository {
        val repo = mockk<JuntaRepository>()
        coEvery { repo.obtenerDependencias() } returns emptyList()
        coEvery { repo.obtenerUsuarios() } returns emptyList()
        return repo
    }

    fun crear(repo: JuntaRepository) = MisReservasViewModel(mockk<Application>(relaxed = true), repo)

    "mientras el servidor no responde la pantalla está cargando" {
        val repo = repositorio()
        val respuesta = CompletableDeferred<Boolean>()
        coEvery { repo.refrescarReservas() } coAnswers { respuesta.await() }

        val vm = crear(repo)
        vm.cargando.value shouldBe true

        respuesta.complete(true)
        vm.cargando.value shouldBe false
    }

    "si el servidor responde no hay aviso de datos sin actualizar" {
        val repo = repositorio()
        coEvery { repo.refrescarReservas() } returns true

        crear(repo).sinActualizar.value shouldBe false
    }

    "si el servidor no responde se avisa que no se pudo actualizar" {
        val repo = repositorio()
        coEvery { repo.refrescarReservas() } returns false

        val vm = crear(repo)

        vm.sinActualizar.value shouldBe true
        vm.cargando.value shouldBe false
    }

    "reintentar vuelve a pedir las reservas y quita el aviso si ahora responde" {
        val repo = repositorio()
        coEvery { repo.refrescarReservas() } returnsMany listOf(false, true)

        val vm = crear(repo)
        vm.sinActualizar.value shouldBe true

        vm.reintentar()

        vm.sinActualizar.value shouldBe false
        coVerify(exactly = 2) { repo.refrescarReservas() }
    }

    "si el servidor rechaza cancelar se guarda su mensaje" {
        val repo = repositorio()
        coEvery { repo.refrescarReservas() } returns true
        coEvery { repo.cancelarReserva(any()) } throws errorHttp(409, "La reserva ya no está pendiente")
        val vm = crear(repo)

        vm.cancelar(pendiente)

        vm.errorCancelar.value shouldBe "La reserva ya no está pendiente"
    }

    "un nuevo intento de cancelar borra el error anterior" {
        val repo = repositorio()
        coEvery { repo.refrescarReservas() } returns true
        coEvery { repo.cancelarReserva(any()) } throws errorHttp(500) andThen Unit
        val vm = crear(repo)
        vm.cancelar(pendiente)
        vm.errorCancelar.value shouldBe "El servidor no pudo atender la petición"

        vm.cancelar(pendiente)

        vm.errorCancelar.value.shouldBeNull()
    }

    "una reserva que no está pendiente no se intenta cancelar" {
        val repo = repositorio()
        coEvery { repo.refrescarReservas() } returns true
        val vm = crear(repo)

        vm.cancelar(pendiente.copy(estado = "CONFIRMADA"))

        coVerify(exactly = 0) { repo.cancelarReserva(any()) }
    }
})
