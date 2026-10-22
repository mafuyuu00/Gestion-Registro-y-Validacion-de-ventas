package com.example.gestionregistroyvalidaciondeventas.model

data class VentaErrores(
    val tipoServicio: String? = null,
    val fechaOperacion: String? = null,
    val cliente: String? = null,
    val monto: String? = null,
    val codigoAutorizacion: String? = null
)