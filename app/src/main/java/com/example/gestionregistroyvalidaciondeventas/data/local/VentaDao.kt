package com.example.gestionregistroyvalidaciondeventas.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {

    @Insert
    suspend fun insertar(venta: VentaEntity)

    @Update
    suspend fun actualizar(venta: VentaEntity)

    @Delete
    suspend fun eliminar(venta: VentaEntity)

    @Query("SELECT * FROM ventas ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<VentaEntity>>
}