package com.alejandro.minegociosv.data

import android.content.Context

class AppContainer(context: Context) {
    private val db = AppDatabase.get(context)
    val productDao = db.productDao()
    val saleDao = db.saleDao()
}
