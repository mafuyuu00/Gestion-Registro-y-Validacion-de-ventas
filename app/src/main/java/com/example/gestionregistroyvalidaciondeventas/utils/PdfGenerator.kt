package com.example.gestionregistroyvalidaciondeventas.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.gestionregistroyvalidaciondeventas.data.local.VentaEntity
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    fun generarPdfVenta(context: Context, venta: VentaEntity): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
        }

        var y = 60f
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Comprobante Oficial de Venta", 50f, y, paint)

        y += 40f
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Sistema de Gestión y Validación - 004D", 50f, y, paint)

        y += 30f
        canvas.drawText("--------------------------------------------------------------------------------", 50f, y, paint)

        y += 35f
        paint.isFakeBoldText = true
        canvas.drawText("Detalles de la Transacción:", 50f, y, paint)

        y += 30f
        paint.isFakeBoldText = false
        canvas.drawText("• ID de Registro: #${venta.id}", 50f, y, paint)
        y += 25f
        canvas.drawText("• Código de Autorización: ${venta.codigoAutorizacion}", 50f, y, paint)
        y += 25f
        canvas.drawText("• Convenio / Servicio: ${venta.tipoServicio}", 50f, y, paint)
        y += 25f
        canvas.drawText("• Cliente: ${venta.cliente}", 50f, y, paint)
        y += 25f
        canvas.drawText("• Monto: \$${venta.monto}", 50f, y, paint)
        y += 25f
        canvas.drawText("• Fecha de Operación: ${venta.fechaOperacion}", 50f, y, paint)
        y += 25f
        canvas.drawText("• Estado: ${venta.estado}", 50f, y, paint)
        y += 25f
        canvas.drawText("• Observaciones: ${venta.observaciones.ifBlank { "Sin observaciones" }}", 50f, y, paint)

        y += 50f
        canvas.drawText("--------------------------------------------------------------------------------", 50f, y, paint)
        y += 30f
        paint.isFakeBoldText = true
        canvas.drawText("Documento generado digitalmente para respaldo de ventas.", 50f, y, paint)

        pdfDocument.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "VentasPDFs")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "Comprobante_${venta.codigoAutorizacion}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }
}
