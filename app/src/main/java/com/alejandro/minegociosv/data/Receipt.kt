package com.alejandro.minegociosv.data

data class Receipt(
    val businessName: String = "Mi Negocio SV",
    val sale: Sale,
    val productName: String,
    val customerName: String = "Cliente general"
)
