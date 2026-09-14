package com.alejandro.minegociosv.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name") fun observeAll(): Flow<List<Product>>
    @Insert suspend fun insert(product: Product)
    @Query("DELETE FROM products WHERE id = :id") suspend fun delete(id: Long)
}
