package com.alejandro.minegociosv.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val quantity: Double,
    val unitPrice: Double,
    val unitCost: Double,
    val createdAt: Long = System.currentTimeMillis()
) {
    val total: Double get() = quantity * unitPrice
    val profit: Double get() = quantity * (unitPrice - unitCost)
}
