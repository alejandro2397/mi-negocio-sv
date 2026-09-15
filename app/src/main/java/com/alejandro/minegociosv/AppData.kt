package com.alejandro.minegociosv

data class Product(val id: Long, val name: String, val stock: Double = 0.0, val cost: Double = 0.0)
data class Sale(val id: Long, val product: String, val quantity: Double, val unit: String, val cost: Double, val price: Double, val date: String)
