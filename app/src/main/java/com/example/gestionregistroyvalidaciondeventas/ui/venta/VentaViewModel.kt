package com.example.gestionregistroyvalidaciondeventas.ui.venta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionregistroyvalidaciondeventas.data.local.AppDatabase
import com.example.gestionregistroyvalidaciondeventas.data.local.VentaEntity
import com.example.gestionregistroyvalidaciondeventas.data.remote.Post
import com.example.gestionregistroyvalidaciondeventas.data.repository.VentaRepository
import com.example.gestionregistroyvalidaciondeventas.model.VentaErrores
import com.example.gestionregistroyvalidaciondeventas.model.VentaUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VentaViewModel @JvmOverloads constructor(
    app: Application,
    private val repository: VentaRepository = VentaRepository(AppDatabase.getDatabase(app).ventaDao())
) : AndroidViewModel(app) {

    val ventasList: Flow<List<VentaEntity>> = repository.obtenerTodas()
    private var ventasRegistradas: List<VentaEntity> = emptyList()
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    private val _filtroEstado = MutableStateFlow("Todas")
    val filtroEstado: StateFlow<String> = _filtroEstado

    private val _postList = MutableStateFlow<List<Post>>(emptyList())
    val postList: StateFlow<List<Post>> = _postList

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _estado = MutableStateFlow(VentaUiState())
    val estado: StateFlow<VentaUiState> = _estado

    init {
        viewModelScope.launch {
            ventasList.collect { lista ->
                ventasRegistradas = lista
                val actual = _estado.value
                if (actual.id == 0 && codigoEnUso(actual.codigoAutorizacion, actual.id)) {
                    _estado.update {
                        it.copy(
                            codigoAutorizacion = siguienteCodigoAutorizacion(),
                            errores = it.errores.copy(codigoAutorizacion = null)
                        )
                    }
                }
            }
        }
        sincronizarConApi {}
    }

    fun siguienteCodigoAutorizacion(): String {
        val mayor = ventasRegistradas
            .mapNotNull { FORMATO_CODIGO.matchEntire(it.codigoAutorizacion.trim().uppercase())?.groupValues?.get(1)?.toIntOrNull() }
            .maxOrNull()
        val siguiente = if (mayor == null || mayor < CODIGO_INICIAL) CODIGO_INICIAL else mayor + 1
        return "AUT-$siguiente"
    }

    private fun codigoEnUso(codigo: String, idActual: Int): Boolean =
        ventasRegistradas.any {
            it.codigoAutorizacion.equals(codigo.trim(), ignoreCase = true) && it.id != idActual
        }

    fun contenidoQr(venta: VentaEntity): String =
        "VENTA:${venta.codigoAutorizacion}|CLIENTE:${venta.cliente}|MONTO:${venta.monto}|FECHA:${venta.fechaOperacion}|SERVICIO:${venta.tipoServicio}"

    fun validarQrVenta(ventaId: Int, contenidoLeido: String, onResultado: (Boolean, String) -> Unit) {
        val venta = ventasRegistradas.find { it.id == ventaId }
        when {
            venta == null -> onResultado(false, "La venta ya no existe")
            venta.estado != ESTADO_EN_REVISION ->
                onResultado(false, "La venta ${venta.codigoAutorizacion} no está en revisión")
            contenidoLeido.trim() != contenidoQr(venta) -> {
                val codigoLeido = contenidoLeido.trim().substringAfter("VENTA:", "").substringBefore("|")
                onResultado(
                    false,
                    if (codigoLeido.isNotBlank() && codigoLeido != venta.codigoAutorizacion)
                        "El QR corresponde a otra venta ($codigoLeido)"
                    else
                        "El QR no coincide con los datos de la venta ${venta.codigoAutorizacion}"
                )
            }
            else -> viewModelScope.launch {
                val enviada = repository.actualizar(venta.copy(estado = ESTADO_RECIBIDA))
                onResultado(
                    true,
                    if (enviada) "QR válido: venta ${venta.codigoAutorizacion} marcada como Recibida"
                    else "QR válido: venta marcada como Recibida (pendiente de sincronizar)"
                )
            }
        }
    }
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
        _estado.value = VentaUiState(codigoAutorizacion = siguienteCodigoAutorizacion())
    }

    fun limpiarMensaje() {
        _mensaje.value = null
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
                    observaciones = venta.observaciones,
                    remoteId = venta.remoteId
                )
            }
        }
    }
    fun validarFormulario(): Boolean {
        val estadoActual = _estado.value
        val montoNumerico = estadoActual.monto.toDoubleOrNull()
        val codigoDuplicado = codigoEnUso(estadoActual.codigoAutorizacion, estadoActual.id)

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
            else if (!FORMATO_CODIGO.matches(estadoActual.codigoAutorizacion.trim().uppercase()))
                "El código debe tener el formato AUT-###"
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
                observaciones = a.observaciones.trim(),
                remoteId = a.remoteId
            )

            val enviada = if (a.id == 0) {
                repository.insertar(entidad)
            } else {
                repository.actualizar(entidad)
            }

            _mensaje.value = if (enviada) {
                "Venta guardada y enviada al microservicio"
            } else {
                "Venta guardada localmente (microservicio no disponible)"
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

    fun sincronizarConApi(onResultado: (String) -> Unit) {
        viewModelScope.launch {
            val resultado = repository.sincronizar()
            resultado.fold(
                onSuccess = { msg -> onResultado(msg) },
                onFailure = { err -> onResultado("Error de conexión con ms-ventas: ${err.localizedMessage}") }
            )
        }
    }

    fun fetchPosts() {
        viewModelScope.launch {
            try {
                _postList.value = repository.getPosts()
            } catch (e: Exception) {
                println("Error al obtener datos: ${e.localizedMessage}")
            }
        }
    }

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    companion object {
        const val ESTADO_BORRADOR = "Borrador"
        const val ESTADO_EN_REVISION = "En revisión"
        const val ESTADO_RECIBIDA = "Recibida"
        private const val CODIGO_INICIAL = 100
        private val FORMATO_CODIGO = Regex("^AUT-(\\d{3,})$")
    }
}
