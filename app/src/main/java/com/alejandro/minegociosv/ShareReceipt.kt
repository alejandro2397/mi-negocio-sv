package com.alejandro.minegociosv

import android.content.Context
import android.content.Intent
import com.alejandro.minegociosv.data.Receipt

fun shareReceipt(context: Context, receipt: Receipt) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Comprobante de venta - ${receipt.businessName}")
        putExtra(Intent.EXTRA_TEXT, ReceiptFormatter.plainText(receipt))
    }
    context.startActivity(Intent.createChooser(intent, "Compartir comprobante"))
}
