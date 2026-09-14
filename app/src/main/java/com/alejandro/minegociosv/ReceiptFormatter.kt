package com.alejandro.minegociosv

import com.alejandro.minegociosv.data.Receipt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptFormatter {
    fun plainText(receipt: Receipt): String {
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(receipt.sale.createdAt))
        return buildString {
            appendLine("${receipt.businessName}")
            appendLine("COMPROBANTE DE VENTA")
            appendLine("Fecha: $date")
            appendLine("Cliente: ${receipt.customerName}")
            appendLine("------------------------------")
            appendLine("${receipt.productName}")
            appendLine("${receipt.sale.quantity} x $%.2f".format(receipt.sale.unitPrice))
            appendLine("TOTAL: $%.2f".format(receipt.sale.total))
            appendLine("------------------------------")
            appendLine("Gracias por su compra")
        }
    }
}
