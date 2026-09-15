package com.alejandro.minegociosv

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val PREFS = "mi_negocio_sv"
private const val PRODUCTS = "products"
private const val SALES_COUNT = "sales_count"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeScreen(applicationContext) }
    }
}

@Composable
private fun HomeScreen(context: Context) {
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var screen by remember { mutableStateOf("inicio") }
    var productName by remember { mutableStateOf("") }
    var products by remember { mutableStateOf(prefs.getStringSet(PRODUCTS, emptySet())?.toList() ?: emptyList()) }
    var salesCount by remember { mutableStateOf(prefs.getInt(SALES_COUNT, 0)) }

    fun saveProducts(list: List<String>) {
        products = list
        prefs.edit().putStringSet(PRODUCTS, list.toSet()).apply()
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Text("Mi Negocio SV", style = MaterialTheme.typography.headlineLarge)
                Text("Tu negocio, más fácil.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(20.dp))
                when (screen) {
                    "inicio" -> {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Resumen", style = MaterialTheme.typography.titleLarge)
                                Text("Ventas registradas: $salesCount")
                                Text("$0.00", style = MaterialTheme.typography.headlineMedium)
                                Text("Ganancia estimada: $0.00")
                                Text("Productos: ${products.size}")
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { screen = "productos" }, Modifier.fillMaxWidth()) { Text("Productos") }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { screen = "ventas" }, Modifier.fillMaxWidth()) { Text("Ventas") }
                    }
                    "productos" -> {
                        Text("Productos", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Nombre del producto") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { val name = productName.trim(); if (name.isNotEmpty()) { saveProducts(products + name); productName = "" } }, enabled = productName.trim().isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("Guardar producto") }
                        Spacer(Modifier.height(12.dp))
                        products.forEachIndexed { index, name -> Text("${index + 1}. $name"); Spacer(Modifier.height(4.dp)) }
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { screen = "inicio" }) { Text("Volver al inicio") }
                    }
                    else -> {
                        Text("Ventas", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Ventas registradas: $salesCount")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { salesCount++; prefs.edit().putInt(SALES_COUNT, salesCount).apply() }, Modifier.fillMaxWidth()) { Text("Registrar venta de prueba") }
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { screen = "inicio" }) { Text("Volver al inicio") }
                    }
                }
            }
        }
    }
}
