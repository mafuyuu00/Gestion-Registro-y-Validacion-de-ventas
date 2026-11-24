package com.example.gestionregistroyvalidaciondeventas.data.repository

import com.example.gestionregistroyvalidaciondeventas.data.local.VentaDao
import com.example.gestionregistroyvalidaciondeventas.data.local.VentaEntity
import com.example.gestionregistroyvalidaciondeventas.data.remote.Post
import com.example.gestionregistroyvalidaciondeventas.data.remote.PostApiService
import com.example.gestionregistroyvalidaciondeventas.data.remote.RetrofitClient
import com.example.gestionregistroyvalidaciondeventas.data.remote.VentaApiService
import com.example.gestionregistroyvalidaciondeventas.data.remote.VentaRemota
import kotlinx.coroutines.flow.Flow

class VentaRepository(
    private val dao: VentaDao,
    private val api: VentaApiService = RetrofitClient.apiService,
    private val postApi: PostApiService = RetrofitClient.postApi
) {

    fun obtenerTodas(): Flow<List<VentaEntity>> = dao.obtenerTodas()

    suspend fun getPosts(): List<Post> = postApi.getPosts()

    suspend fun insertar(venta: VentaEntity): Boolean {
        val idLocal = dao.insertar(venta).toInt()
        return try {
            val remota = api.crear(venta.aRemota())
            dao.actualizar(venta.copy(id = idLocal, remoteId = remota.id))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun actualizar(venta: VentaEntity): Boolean {
        dao.actualizar(venta)
        return try {
            if (venta.remoteId == null) {
                val remota = api.crear(venta.aRemota())
                dao.actualizar(venta.copy(remoteId = remota.id))
            } else {
                api.actualizar(venta.remoteId, venta.aRemota())
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminar(venta: VentaEntity): Boolean {
        dao.eliminar(venta)
        return try {
            venta.remoteId?.let { api.eliminar(it) }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sincronizar(): Result<String> {
        return try {
            dao.obtenerLista()
                .filter { it.remoteId == null }
                .forEach { pendiente ->
                    try {
                        val remota = api.crear(pendiente.aRemota())
                        dao.actualizar(pendiente.copy(remoteId = remota.id))
                    } catch (e: Exception) {
                    }
                }

            val remotas = api.listar()
            remotas.forEach { remota ->
                val idRemoto = remota.id ?: return@forEach
                val local = dao.buscarPorRemoteId(idRemoto)
                if (local == null) {
                    dao.insertar(remota.aEntidad())
                } else {
                    val actualizada = remota.aEntidad(local)
                    if (actualizada != local) dao.actualizar(actualizada)
                }
            }

            val idsRemotos = remotas.mapNotNull { it.id }.toSet()
            dao.obtenerLista()
                .filter { it.remoteId != null && it.remoteId !in idsRemotos }
                .forEach { dao.eliminar(it) }

            Result.success("Sincronizado con ms-ventas: ${remotas.size} ventas")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

fun VentaEntity.aRemota() = VentaRemota(
    id = remoteId,
    tipoServicio = tipoServicio,
    fechaOperacion = fechaOperacion,
    cliente = cliente,
    monto = monto.toDoubleOrNull() ?: 0.0,
    codigoAutorizacion = codigoAutorizacion,
    estado = estado,
    observaciones = observaciones
)

fun VentaRemota.aEntidad(local: VentaEntity? = null) = VentaEntity(
    id = local?.id ?: 0,
    tipoServicio = tipoServicio,
    fechaOperacion = fechaOperacion,
    cliente = cliente,
    monto = if (monto % 1.0 == 0.0) monto.toLong().toString() else monto.toString(),
    codigoAutorizacion = codigoAutorizacion,
    respaldoUri = local?.respaldoUri,
    estado = estado,
    observaciones = observaciones ?: "",
    remoteId = id
)
