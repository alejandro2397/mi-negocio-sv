package com.alejandro.minegociosv.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cost: Double,
    val price: Double,
    val stock: Double
)
