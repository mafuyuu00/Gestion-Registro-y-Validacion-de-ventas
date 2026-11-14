package com.example.gestionregistroyvalidaciondeventas

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VentaViewModelTest {

    @Test
    fun validarMonto_numericoValido_esCorrecto() {
        val montoStr = "25000"
        val montoNum = montoStr.toDoubleOrNull()
        assertTrue(montoNum != null && montoNum > 0.0)
    }

    @Test
    fun validarMonto_negativoOInvalido_falla() {
        val montoStr = "-500"
        val montoNum = montoStr.toDoubleOrNull()
        val esValido = montoNum != null && montoNum > 0.0
        assertFalse(esValido)
    }

    @Test
    fun validarCodigoAutorizacion_noVacio_esValido() {
        val codigo = "AUT-999"
        assertFalse(codigo.isBlank())
    }
}
