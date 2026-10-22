package com.example.gestionregistroyvalidaciondeventas.model

data class VentaUiState(
    val id: Int = 0,
    val tipoServicio: String = "",
    val fechaOperacion: String = "",
    val cliente: String = "",
    val monto: String = "",
    val codigoAutorizacion: String = "",
    val respaldoUri: String? = null,
    val estado: String = "Borrador",
    val observaciones: String = "",
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    val errores: VentaErrores = VentaErrores()
)