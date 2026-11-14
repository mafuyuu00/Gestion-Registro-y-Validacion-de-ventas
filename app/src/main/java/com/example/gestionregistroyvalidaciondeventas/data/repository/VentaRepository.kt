package com.example.gestionregistroyvalidaciondeventas.data.repository

import com.example.gestionregistroyvalidaciondeventas.data.local.VentaDao
import com.example.gestionregistroyvalidaciondeventas.data.local.VentaEntity
import com.example.gestionregistroyvalidaciondeventas.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow

class VentaRepository(private val dao: VentaDao) {
    private val apiService = RetrofitClient.apiService

    fun obtenerTodas(): Flow<List<VentaEntity>> = dao.obtenerTodas()

    suspend fun insertar(venta: VentaEntity) {
        dao.insertar(venta)
        try {
            if (venta.estado == "Recibida") {
                apiService.registrarVenta(venta)
            }
        } catch (e: Exception) {
            // Error de red manejado offline-first
        }
    }

    suspend fun actualizar(venta: VentaEntity) {
        dao.actualizar(venta)
        try {
            if (venta.estado == "Recibida") {
                apiService.registrarVenta(venta)
            }
        } catch (e: Exception) {
            // Error de red
        }
    }

    suspend fun eliminar(venta: VentaEntity) = dao.eliminar(venta)

    suspend fun sincronizarRemoto(): Result<String> {
        return try {
            val response = apiService.obtenerVentas()
            if (response.isSuccessful) {
                Result.success("Sincronización API REST exitosa (Conectado a Servidor)")
            } else {
                Result.failure(Exception("Error en servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
