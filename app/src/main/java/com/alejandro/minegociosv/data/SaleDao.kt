package com.alejandro.minegociosv.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY createdAt DESC") fun observeAll(): Flow<List<Sale>>
    @Insert suspend fun insert(sale: Sale)
    @Query("SELECT SUM(quantity * unitPrice) FROM sales") fun observeTotal(): Flow<Double?>
    @Query("SELECT SUM(quantity * (unitPrice - unitCost)) FROM sales") fun observeProfit(): Flow<Double?>
}
