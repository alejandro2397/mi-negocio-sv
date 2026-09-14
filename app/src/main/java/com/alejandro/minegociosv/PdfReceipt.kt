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
        val pageWidth = 420; val pageHeight = 620
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var y = 42f
        fun line(text: String, size: Float = 14f, bold: Boolean = false) { paint.textSize=size; paint.isFakeBoldText=bold; page.canvas.drawText(text,30f,y,paint); y += size+11f }
        line(receipt.businessName, 24f, true)
        if (receipt.businessPhone.isNotBlank()) line("Tel: ${receipt.businessPhone}")
        if (receipt.businessAddress.isNotBlank()) line(receipt.businessAddress)
        y += 5f; line("COMPROBANTE DE VENTA", 18f, true)
        line("No. ${receipt.receiptNumber}")
        line(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(receipt.sale.createdAt)))
        line("Cliente: ${receipt.customerName}")
        y += 8f; line("DETALLE", 15f, true)
        line(receipt.productName)
        line("Cantidad: ${receipt.sale.quantity}")
        line("Precio unitario: $%.2f".format(receipt.sale.unitPrice))
        y += 5f; line("TOTAL: $%.2f".format(receipt.sale.total), 20f, true)
        line("Gracias por su compra", 14f, true)
        document.finishPage(page)
        val dir=File(context.cacheDir,"comprobantes").apply{mkdirs()}
        val file=File(dir,"comprobante-${receipt.receiptNumber}-${receipt.sale.createdAt}.pdf")
        FileOutputStream(file).use{document.writeTo(it)}; document.close(); return file
    }
}
