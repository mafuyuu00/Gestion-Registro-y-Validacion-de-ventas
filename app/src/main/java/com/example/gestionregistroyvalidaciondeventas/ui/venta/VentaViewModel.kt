package com.example.gestionregistroyvalidaciondeventas.ui.venta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionregistroyvalidaciondeventas.data.local.AppDatabase
import com.example.gestionregistroyvalidaciondeventas.data.local.VentaEntity
import com.example.gestionregistroyvalidaciondeventas.data.repository.VentaRepository
import com.example.gestionregistroyvalidaciondeventas.model.VentaErrores
import com.example.gestionregistroyvalidaciondeventas.model.VentaUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VentaViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getDatabase(app).ventaDao()
    private val repository = VentaRepository(dao)
    val ventasList: Flow<List<VentaEntity>> = repository.obtenerTodas()
    private var ventasRegistradas: List<VentaEntity> = emptyList()
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    private val _filtroEstado = MutableStateFlow("Todas")
    val filtroEstado: StateFlow<String> = _filtroEstado

    init {
        viewModelScope.launch {
            ventasList.collect { lista ->
                ventasRegistradas = lista
            }
        }
    }
    private val _estado = MutableStateFlow(VentaUiState())
    val estado: StateFlow<VentaUiState> = _estado
    fun onBusquedaChange(valor: String) {
        _busqueda.value = valor
    }

    fun onFiltroEstadoChange(valor: String) {
        _filtroEstado.value = valor
    }
    fun onTipoServicioChange(valor: String) {
        _estado.update {
            it.copy(
                tipoServicio = valor,
                errores = it.errores.copy(tipoServicio = null)
            )
        }
    }

    fun onFechaOperacionChange(valor: String) {
        _estado.update {
            it.copy(
                fechaOperacion = valor,
                errores = it.errores.copy(fechaOperacion = null)
            )
        }
    }

    fun onClienteChange(valor: String) {
        _estado.update {
            it.copy(
                cliente = valor,
                errores = it.errores.copy(cliente = null)
            )
        }
    }

    fun onMontoChange(valor: String) {
        _estado.update {
            it.copy(
                monto = valor,
                errores = it.errores.copy(monto = null)
            )
        }
    }

    fun onCodigoAutorizacionChange(valor: String) {
        _estado.update {
            it.copy(
                codigoAutorizacion = valor,
                errores = it.errores.copy(codigoAutorizacion = null)
            )
        }
    }

    fun onObservacionesChange(valor: String) {
        _estado.update {
            it.copy(observaciones = valor)
        }
    }

    fun onRespaldoUriChange(valor: String?) {
        _estado.update {
            it.copy(respaldoUri = valor)
        }
    }

    fun limpiarFormulario() {
        _estado.value = VentaUiState()
    }

    fun cargarVenta(id: Int) {
        val venta = ventasRegistradas.find { it.id == id }
        if (venta != null && venta.estado == "Borrador") {
            _estado.update {
                VentaUiState(
                    id = venta.id,
                    tipoServicio = venta.tipoServicio,
                    fechaOperacion = venta.fechaOperacion,
                    cliente = venta.cliente,
                    monto = venta.monto,
                    codigoAutorizacion = venta.codigoAutorizacion,
                    respaldoUri = venta.respaldoUri,
                    estado = venta.estado,
                    observaciones = venta.observaciones
                )
            }
        }
    }
    fun validarFormulario(): Boolean {
        val estadoActual = _estado.value
        val montoNumerico = estadoActual.monto.toDoubleOrNull()
        val codigoDuplicado = ventasRegistradas.any {
            it.codigoAutorizacion.equals(estadoActual.codigoAutorizacion.trim(), ignoreCase = true) &&
                    it.id != estadoActual.id
        }

        val errores = VentaErrores(
            tipoServicio = if (estadoActual.tipoServicio.isBlank())
                "El tipo de convenio o servicio es obligatorio"
            else null,

            fechaOperacion = if (estadoActual.fechaOperacion.isBlank())
                "La fecha de operación es obligatoria"
            else null,

            cliente = if (estadoActual.cliente.isBlank())
                "El cliente ficticio es obligatorio"
            else null,

            monto = if (estadoActual.monto.isBlank())
                "El monto es obligatorio"
            else if (montoNumerico == null || montoNumerico <= 0)
                "El monto debe ser numérico y mayor que 0"
            else null,

            codigoAutorizacion = if (estadoActual.codigoAutorizacion.isBlank())
                "El código de autorización es obligatorio"
            else if (codigoDuplicado)
                "El código ya existe en los registros"
            else null
        )

        val hayErrores = listOfNotNull(
            errores.tipoServicio,
            errores.fechaOperacion,
            errores.cliente,
            errores.monto,
            errores.codigoAutorizacion
        ).isNotEmpty()

        _estado.update {
            it.copy(errores = errores)
        }

        return !hayErrores
    }
    fun guardar(estadoDestino: String = "Borrador", onListo: () -> Unit) {
        val a = _estado.value
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }

            val entidad = VentaEntity(
                id = a.id,
                tipoServicio = a.tipoServicio.trim(),
                fechaOperacion = a.fechaOperacion.trim(),
                cliente = a.cliente.trim(),
                monto = a.monto.trim(),
                codigoAutorizacion = a.codigoAutorizacion.trim(),
                respaldoUri = a.respaldoUri,
                estado = estadoDestino,
                observaciones = a.observaciones.trim()
            )

            if (a.id == 0) {
                repository.insertar(entidad)
            } else {
                repository.actualizar(entidad)
            }

            limpiarFormulario()
            onListo()
        }
    }

    fun eliminarVenta(venta: VentaEntity) {
        if (venta.estado == "Borrador") {
            viewModelScope.launch {
                repository.eliminar(venta)
            }
        }
    }
}