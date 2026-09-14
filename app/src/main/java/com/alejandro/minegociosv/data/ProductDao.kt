package com.alejandro.minegociosv.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name") fun observeAll(): Flow<List<Product>>
    @Insert suspend fun insert(product: Product)
    @Update suspend fun update(product: Product)
    @Query("DELETE FROM products WHERE id = :id") suspend fun delete(id: Long)
    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :id AND stock >= :quantity") suspend fun decreaseStock(id: Long, quantity: Double): Int
}
