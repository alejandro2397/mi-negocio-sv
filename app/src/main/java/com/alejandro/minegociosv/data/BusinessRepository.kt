package com.alejandro.minegociosv.data

import kotlinx.coroutines.flow.Flow

class BusinessRepository(private val products: ProductDao, private val sales: SaleDao) {
    fun observeProducts(): Flow<List<Product>> = products.observeAll()
    fun observeSales(): Flow<List<Sale>> = sales.observeAll()
    suspend fun addProduct(name: String, cost: Double, price: Double, stock: Double) =
        products.insert(Product(name = name.trim(), cost = cost, price = price, stock = stock))
    suspend fun addSale(productId: Long, quantity: Double, unitPrice: Double, unitCost: Double) =
        sales.insert(Sale(productId = productId, quantity = quantity, unitPrice = unitPrice, unitCost = unitCost))
}
