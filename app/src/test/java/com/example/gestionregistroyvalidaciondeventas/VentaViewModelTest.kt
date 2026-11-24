package com.example.gestionregistroyvalidaciondeventas

import android.app.Application
import com.example.gestionregistroyvalidaciondeventas.data.local.VentaEntity
import com.example.gestionregistroyvalidaciondeventas.data.remote.Post
import com.example.gestionregistroyvalidaciondeventas.data.repository.VentaRepository
import com.example.gestionregistroyvalidaciondeventas.ui.venta.VentaViewModel

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class VentaViewModelTest : StringSpec({

    val dispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(dispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    val borrador = VentaEntity(
        id = 1, tipoServicio = "Seguro Automotriz Full", fechaOperacion = "24/11/2026",
        cliente = "Cliente Uno", monto = "15000", codigoAutorizacion = "AUT-101",
        estado = "Borrador", remoteId = 10L
    )
    val recibida = VentaEntity(
        id = 2, tipoServicio = "Convenio Dental Corporativo", fechaOperacion = "24/11/2026",
        cliente = "Cliente Dos", monto = "30000", codigoAutorizacion = "AUT-102",
        estado = "Recibida", remoteId = 11L
    )
    val enRevision = VentaEntity(
        id = 3, tipoServicio = "Plan Telecomunicaciones Pyme", fechaOperacion = "24/11/2026",
        cliente = "Cliente Tres", monto = "45000", codigoAutorizacion = "AUT-103",
        estado = "En revisión", remoteId = 12L
    )
    val fakeVentas = listOf(borrador, recibida, enRevision)

    fun crearFakeRepository(ventas: List<VentaEntity> = fakeVentas): VentaRepository {
        val fakeRepository = mockk<VentaRepository>()
        every { fakeRepository.obtenerTodas() } returns flowOf(ventas)
        coEvery { fakeRepository.sincronizar() } returns Result.success("Sincronizado con ms-ventas: 3 ventas")
        coEvery { fakeRepository.insertar(any()) } returns true
        coEvery { fakeRepository.actualizar(any()) } returns true
        coEvery { fakeRepository.eliminar(any()) } returns true
        return fakeRepository
    }

    fun crearViewModel(repository: VentaRepository) =
        VentaViewModel(mockk<Application>(relaxed = true), repository)

    fun VentaViewModel.llenarFormulario() {
        onTipoServicioChange("Plan Telecomunicaciones Pyme")
        onFechaOperacionChange("24/11/2026")
        onClienteChange("Cliente Prueba")
        onMontoChange("25000")
    }

    "ventasList debe contener las ventas que entrega el repositorio" {
        runTest(dispatcher) {
            val testViewModel = crearViewModel(crearFakeRepository())
            advanceUntilIdle()

            testViewModel.ventasList.first() shouldContainExactly fakeVentas
        }
    }

    "postList debe contener los datos de la API externa después de fetchPosts()" {
        runTest(dispatcher) {
            val fakePosts = listOf(
                Post(userId = 1, id = 1, title = "Título 1", body = "Contenido 1"),
                Post(userId = 2, id = 2, title = "Título 2", body = "Contenido 2")
            )
            val fakeRepository = crearFakeRepository()
            coEvery { fakeRepository.getPosts() } returns fakePosts

            val testViewModel = crearViewModel(fakeRepository)
            testViewModel.fetchPosts()
            advanceUntilIdle()

            testViewModel.postList.value shouldContainExactly fakePosts
        }
    }

    "una venta nueva recibe el siguiente código de autorización disponible" {
        runTest(dispatcher) {
            val testViewModel = crearViewModel(crearFakeRepository())
            advanceUntilIdle()

            testViewModel.limpiarFormulario()

            testViewModel.estado.value.codigoAutorizacion shouldBe "AUT-104"
        }
    }

    "sin ventas registradas el primer código es AUT-100" {
        runTest(dispatcher) {
            val testViewModel = crearViewModel(crearFakeRepository(ventas = emptyList()))
            advanceUntilIdle()

            testViewModel.siguienteCodigoAutorizacion() shouldBe "AUT-100"
        }
    }

    "si el código del formulario pasa a estar en uso se asigna el siguiente" {
        runTest(dispatcher) {
            val ventasFlow = MutableStateFlow(listOf(borrador))
            val fakeRepository = crearFakeRepository()
            every { fakeRepository.obtenerTodas() } returns ventasFlow
            val testViewModel = crearViewModel(fakeRepository)
            advanceUntilIdle()

            testViewModel.limpiarFormulario()
            testViewModel.estado.value.codigoAutorizacion shouldBe "AUT-102"

            ventasFlow.value = listOf(borrador, recibida)
            advanceUntilIdle()

            testViewModel.estado.value.codigoAutorizacion shouldBe "AUT-103"
        }
    }

    "validarFormulario con campos vacíos debe mostrar los errores" {
        runTest(dispatcher) {
            val testViewModel = crearViewModel(crearFakeRepository())
            advanceUntilIdle()
            testViewModel.limpiarFormulario()

            testViewModel.validarFormulario() shouldBe false

            val errores = testViewModel.estado.value.errores
            errores.tipoServicio shouldBe "El tipo de convenio o servicio es obligatorio"
            errores.fechaOperacion shouldBe "La fecha de operación es obligatoria"
            errores.cliente shouldBe "El cliente ficticio es obligatorio"
            errores.monto shouldBe "El monto es obligatorio"
            errores.codigoAutorizacion shouldBe null
        }
    }

    "el monto debe ser numérico y mayor que 0" {
        runTest(dispatcher) {
            val testViewModel = crearViewModel(crearFakeRepository())
            advanceUntilIdle()
            testViewModel.limpiarFormulario()
            testViewModel.llenarFormulario()

            testViewModel.onMontoChange("-500")
            testViewModel.validarFormulario() shouldBe false
            testViewModel.estado.value.errores.monto shouldBe "El monto debe ser numérico y mayor que 0"
        }
    }

    "un formulario completo con el código asignado es válido" {
        runTest(dispatcher) {
            val testViewModel = crearViewModel(crearFakeRepository())
            advanceUntilIdle()
            testViewModel.limpiarFormulario()
            testViewModel.llenarFormulario()

            testViewModel.validarFormulario() shouldBe true
        }
    }

    "enviar una venta nueva la guarda En revisión con el código automático" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()
            val testViewModel = crearViewModel(fakeRepository)
            advanceUntilIdle()
            testViewModel.limpiarFormulario()
            testViewModel.llenarFormulario()

            var listo = false
            testViewModel.guardar(estadoDestino = VentaViewModel.ESTADO_EN_REVISION) { listo = true }
            advanceUntilIdle()

            coVerify {
                fakeRepository.insertar(match { it.codigoAutorizacion == "AUT-104" && it.estado == "En revisión" })
            }
            listo shouldBe true
            testViewModel.mensaje.value shouldBe "Venta guardada y enviada al microservicio"
        }
    }

    "guardar una venta en borrador cargada la actualiza manteniendo su código" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()
            coEvery { fakeRepository.actualizar(any()) } returns false
            val testViewModel = crearViewModel(fakeRepository)
            advanceUntilIdle()

            testViewModel.cargarVenta(borrador.id)
            testViewModel.onClienteChange("Cliente Editado")
            testViewModel.guardar { }
            advanceUntilIdle()

            coVerify {
                fakeRepository.actualizar(match {
                    it.id == 1 && it.codigoAutorizacion == "AUT-101" && it.cliente == "Cliente Editado"
                })
            }
            testViewModel.mensaje.value shouldBe "Venta guardada localmente (microservicio no disponible)"
        }
    }

    "cargarVenta solo permite editar ventas en Borrador" {
        runTest(dispatcher) {
            val testViewModel = crearViewModel(crearFakeRepository())
            advanceUntilIdle()

            testViewModel.cargarVenta(enRevision.id)
            testViewModel.estado.value.id shouldBe 0

            testViewModel.cargarVenta(borrador.id)
            testViewModel.estado.value.codigoAutorizacion shouldBe "AUT-101"
        }
    }

    "eliminarVenta solo elimina ventas en Borrador" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()
            val testViewModel = crearViewModel(fakeRepository)
            advanceUntilIdle()

            testViewModel.eliminarVenta(recibida)
            testViewModel.eliminarVenta(borrador)
            advanceUntilIdle()

            coVerify(exactly = 1) { fakeRepository.eliminar(borrador) }
            coVerify(exactly = 0) { fakeRepository.eliminar(recibida) }
        }
    }

    "un QR válido marca la venta en revisión como Recibida" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()
            val testViewModel = crearViewModel(fakeRepository)
            advanceUntilIdle()

            var valido = false
            testViewModel.validarQrVenta(enRevision.id, testViewModel.contenidoQr(enRevision)) { ok, _ -> valido = ok }
            advanceUntilIdle()

            valido shouldBe true
            coVerify { fakeRepository.actualizar(enRevision.copy(estado = "Recibida")) }
        }
    }

    "un QR de otra venta no cambia el estado" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()
            val testViewModel = crearViewModel(fakeRepository)
            advanceUntilIdle()

            var mensaje = ""
            testViewModel.validarQrVenta(enRevision.id, testViewModel.contenidoQr(recibida)) { _, msg -> mensaje = msg }
            advanceUntilIdle()

            mensaje shouldBe "El QR corresponde a otra venta (AUT-102)"
            coVerify(exactly = 0) { fakeRepository.actualizar(any()) }
        }
    }

    "solo se pueden validar ventas que están en revisión" {
        runTest(dispatcher) {
            val fakeRepository = crearFakeRepository()
            val testViewModel = crearViewModel(fakeRepository)
            advanceUntilIdle()

            var valido = true
            testViewModel.validarQrVenta(borrador.id, testViewModel.contenidoQr(borrador)) { ok, _ -> valido = ok }
            advanceUntilIdle()

            valido shouldBe false
            coVerify(exactly = 0) { fakeRepository.actualizar(any()) }
        }
    }

    "sincronizarConApi informa el resultado del microservicio" {
        runTest(dispatcher) {
            val testViewModel = crearViewModel(crearFakeRepository())
            advanceUntilIdle()

            var resultado = ""
            testViewModel.sincronizarConApi { resultado = it }
            advanceUntilIdle()

            resultado shouldBe "Sincronizado con ms-ventas: 3 ventas"
        }
    }
})
