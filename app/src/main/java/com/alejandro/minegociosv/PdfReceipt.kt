package com.alejandro.minegociosv

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.alejandro.minegociosv.data.Receipt
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReceipt {
    fun create(context: Context, receipt: Receipt): File {
        val document = PdfDocument()
        val pageWidth = 360
        val pageHeight = 560
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 14f }
        var y = 40f
        fun line(text: String, size: Float = 14f) { paint.textSize = size; page.canvas.drawText(text, 24f, y, paint); y += size + 12f }
        line(receipt.businessName, 22f)
        line("COMPROBANTE DE VENTA", 16f)
        line(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(receipt.sale.createdAt)))
        line("Cliente: ${receipt.customerName}")
        y += 8f
        line(receipt.productName)
        line("${receipt.sale.quantity} x $%.2f".format(receipt.sale.unitPrice))
        line("TOTAL: $%.2f".format(receipt.sale.total), 18f)
        line("Ganancia: $%.2f".format(receipt.sale.profit))
        y += 10f
        line("Gracias por su compra")
        document.finishPage(page)
        val dir = File(context.cacheDir, "comprobantes").apply { mkdirs() }
        val file = File(dir, "comprobante-${receipt.sale.id}-${receipt.sale.createdAt}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }
}
