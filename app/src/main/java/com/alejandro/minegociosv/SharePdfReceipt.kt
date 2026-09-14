package com.alejandro.minegociosv

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.alejandro.minegociosv.data.Receipt

fun sharePdfReceipt(context: Context, receipt: Receipt) {
    val file = PdfReceipt.create(context, receipt)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_SUBJECT, "Comprobante de venta - ${receipt.businessName}")
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir comprobante PDF"))
}
