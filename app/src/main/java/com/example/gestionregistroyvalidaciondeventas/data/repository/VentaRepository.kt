package com.example.gestionregistroyvalidaciondeventas.data.repository

import com.example.gestionregistroyvalidaciondeventas.data.local.VentaDao
import com.example.gestionregistroyvalidaciondeventas.data.local.VentaEntity
import kotlinx.coroutines.flow.Flow

class VentaRepository(private val dao: VentaDao) {

    fun obtenerTodas(): Flow<List<VentaEntity>> = dao.obtenerTodas()
    suspend fun insertar(venta: VentaEntity) = dao.insertar(venta)
    suspend fun actualizar(venta: VentaEntity) = dao.actualizar(venta)
    suspend fun eliminar(venta: VentaEntity) = dao.eliminar(venta)
}