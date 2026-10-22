package com.example.gestionregistroyvalidaciondeventas.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipoServicio: String,
    val fechaOperacion: String,
    val cliente: String,
    val monto: String,
    val codigoAutorizacion: String,
    val respaldoUri: String? = null,
    val estado: String = "Borrador",
    val observaciones: String = ""
)