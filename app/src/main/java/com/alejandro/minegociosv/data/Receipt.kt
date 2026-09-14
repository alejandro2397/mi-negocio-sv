package com.alejandro.minegociosv.data

data class Receipt(
    val businessName: String = "Mi Negocio SV",
    val businessPhone: String = "",
    val businessAddress: String = "",
    val sale: Sale,
    val productName: String,
    val customerName: String = "Cliente general",
    val receiptNumber: String = "V-${sale.id.toString().padStart(6, '0')}"
)
