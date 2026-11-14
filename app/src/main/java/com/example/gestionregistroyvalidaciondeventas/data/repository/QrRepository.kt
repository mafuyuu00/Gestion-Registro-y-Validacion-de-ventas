package com.example.gestionregistroyvalidaciondeventas.data.repository

import com.example.gestionregistroyvalidaciondeventas.model.QrResult

class QrRepository {
    fun processQrContent(content: String): QrResult {
        return QrResult(content)
    }
}
