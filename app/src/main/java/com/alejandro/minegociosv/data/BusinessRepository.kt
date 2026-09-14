package com.alejandro.minegociosv.data

import kotlinx.coroutines.flow.Flow

class BusinessRepository(private val products: ProductDao, private val sales: SaleDao) {
    fun observeProducts(): Flow<List<Product>> = products.observeAll()
    fun observeSales(): Flow<List<Sale>> = sales.observeAll()
    suspend fun addProduct(name: String, cost: Double, price: Double, stock: Double) =
        products.insert(Product(name = name.trim(), cost = cost, price = price, stock = stock))
    suspend fun addSale(product: Product, quantity: Double): Boolean {
        if (quantity <= 0 || quantity > product.stock) return false
        val changed = products.decreaseStock(product.id, quantity)
        if (changed == 0) return false
        sales.insert(Sale(productId = product.id, quantity = quantity, unitPrice = product.price, unitCost = product.cost))
        return true
    }
}
