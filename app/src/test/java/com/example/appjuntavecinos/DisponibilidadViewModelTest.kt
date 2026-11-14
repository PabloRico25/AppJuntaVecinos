package com.example.appjuntavecinos

import android.app.Application
import com.example.appjuntavecinos.data.model.BloqueDto
import com.example.appjuntavecinos.data.model.DependenciaComunitariaEntity
import com.example.appjuntavecinos.data.repository.JuntaRepository
import com.example.appjuntavecinos.viewmodel.DisponibilidadViewModel
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DisponibilidadViewModelTest : StringSpec({

    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    val salon = DependenciaComunitariaEntity(id = 1, nombre = "Salón Principal")
    val sala = DependenciaComunitariaEntity(id = 2, nombre = "Sala Multiuso")

    fun crear(repo: JuntaRepository) = DisponibilidadViewModel(mockk<Application>(relaxed = true), repo)

    fun repositorioConSalas(): JuntaRepository {
        val repo = mockk<JuntaRepository>()
        coEvery { repo.obtenerDependencias() } returns listOf(salon, sala)
        coEvery { repo.obtenerUsuarios() } returns emptyList()
        coEvery { repo.obtenerBloquesOcupados(any(), any()) } returns emptyList()
        return repo
    }

    "al abrir elige la primera sala y arma el día completo libre" {
        val vm = crear(repositorioConSalas())

        vm.dependenciaId.value shouldBe 1
        vm.bloques.value.map { it.tipo } shouldBe listOf("LIBRE")
        vm.cargando.value shouldBe false
        vm.error.value.shouldBeNull()
    }

    "los bloques ocupados que entrega el servidor se mezclan con los tramos libres" {
        val repo = repositorioConSalas()
        coEvery { repo.obtenerBloquesOcupados(1, any()) } returns listOf(BloqueDto("14:00", "16:00", "CONFIRMADA", null))

        val vm = crear(repo)

        vm.bloques.value.map { it.tipo } shouldBe listOf("LIBRE", "CONFIRMADA", "LIBRE")
    }

    "cambiar de sala pide los bloques de la sala nueva" {
        val repo = repositorioConSalas()
        val vm = crear(repo)

        vm.onDependenciaChange(2)

        vm.dependenciaId.value shouldBe 2
        coVerify { repo.obtenerBloquesOcupados(2, any()) }
    }

    "si fallan los bloques se muestra el error y se vacía la lista" {
        val repo = repositorioConSalas()
        coEvery { repo.obtenerBloquesOcupados(any(), any()) } throws IOException("sin red")

        val vm = crear(repo)

        vm.error.value shouldBe "No se pudo conectar con el servidor. Revisa que esté encendido"
        vm.bloques.value shouldBe emptyList()
        vm.cargando.value shouldBe false
    }

    "reintentar con salas vuelve a pedir los bloques y limpia el error" {
        val repo = repositorioConSalas()
        coEvery { repo.obtenerBloquesOcupados(any(), any()) } throws IOException("sin red") andThen emptyList()
        val vm = crear(repo)
        vm.error.value shouldBe "No se pudo conectar con el servidor. Revisa que esté encendido"

        vm.reintentar()

        vm.error.value.shouldBeNull()
        vm.bloques.value.map { it.tipo } shouldBe listOf("LIBRE")
    }

    "sin salas se avisa que no cargaron y no se piden bloques" {
        val repo = mockk<JuntaRepository>()
        coEvery { repo.obtenerDependencias() } returns emptyList()
        coEvery { repo.obtenerUsuarios() } returns emptyList()

        val vm = crear(repo)

        vm.errorSalas.value shouldBe "No se pudieron cargar las salas. Revisa tu conexión"
        vm.cargando.value shouldBe false
        coVerify(exactly = 0) { repo.obtenerBloquesOcupados(any(), any()) }
    }

    "reintentar sin salas las vuelve a pedir y sigue con los bloques" {
        val repo = mockk<JuntaRepository>()
        coEvery { repo.obtenerDependencias() } returnsMany listOf(emptyList(), listOf(salon))
        coEvery { repo.obtenerUsuarios() } returns emptyList()
        coEvery { repo.obtenerBloquesOcupados(any(), any()) } returns emptyList()
        val vm = crear(repo)
        vm.errorSalas.value shouldBe "No se pudieron cargar las salas. Revisa tu conexión"

        vm.reintentar()

        vm.errorSalas.value.shouldBeNull()
        vm.dependenciaId.value shouldBe 1
        vm.bloques.value.map { it.tipo } shouldBe listOf("LIBRE")
    }
})
