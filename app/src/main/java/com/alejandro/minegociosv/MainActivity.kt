package com.alejandro.minegociosv

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

private const val PREFS = "mi_negocio_sv"
private const val PRODUCTS = "products"
private const val SALES = "sales"
private const val COUNT = "count"
private const val TOTAL = "total"
private const val PROFIT = "profit"
private const val STOCK = "stock"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { App(applicationContext) } }
}

private fun receipt(context: Context, quantity: String, unit: String, cost: String, price: String): File {
    val directory = File(context.cacheDir, "comprobantes"); directory.mkdirs(); val file = File(directory, "comprobante_venta.pdf")
    val document = PdfDocument(); val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create()); val paint = android.graphics.Paint()
    paint.textSize = 22f; page.canvas.drawText("MI NEGOCIO SV", 50f, 70f, paint); paint.textSize = 16f
    page.canvas.drawText("COMPROBANTE DE VENTA", 50f, 105f, paint); page.canvas.drawText("Cantidad: $quantity $unit", 50f, 160f, paint); page.canvas.drawText("Costo: $$cost", 50f, 195f, paint); page.canvas.drawText("Venta: $$price", 50f, 230f, paint)
    val gain = (price.toDoubleOrNull() ?: 0.0) - (cost.toDoubleOrNull() ?: 0.0); page.canvas.drawText("Ganancia: $%.2f".format(gain), 50f, 265f, paint); page.canvas.drawText("Gracias por su compra", 50f, 330f, paint)
    document.finishPage(page); file.outputStream().use { document.writeTo(it) }; document.close(); return file
}

private fun share(context: Context, file: File) { val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file); val intent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }; context.startActivity(Intent.createChooser(intent, "Compartir comprobante")) }

@Composable private fun App(context: Context) {
    val preferences = remember { context.getSharedPreferences(PREFS, 0) }; var page by remember { mutableStateOf("home") }
    var products by remember { mutableStateOf(preferences.getStringSet(PRODUCTS, emptySet())?.toList() ?: emptyList()) }; var sales by remember { mutableStateOf(preferences.getStringSet(SALES, emptySet())?.toList()?.reversed() ?: emptyList()) }
    var stock by remember { mutableStateOf(preferences.getFloat(STOCK, 0f).toDouble()) }; var count by remember { mutableStateOf(preferences.getInt(COUNT, 0)) }; var total by remember { mutableStateOf(preferences.getFloat(TOTAL, 0f).toDouble()) }; var profit by remember { mutableStateOf(preferences.getFloat(PROFIT, 0f).toDouble()) }
    fun addProduct(name: String) { val cleanName = name.trim(); if (cleanName.isNotEmpty()) { products = products + cleanName; preferences.edit().putStringSet(PRODUCTS, products.toSet()).apply() } }
    fun addSale(totalSale: Double, gain: Double, detail: String) { count += 1; total += totalSale; profit += gain; sales = (sales + detail).takeLast(100); preferences.edit().putInt(COUNT, count).putFloat(TOTAL, total.toFloat()).putFloat(PROFIT, profit.toFloat()).putStringSet(SALES, sales.toSet()).apply() }
    fun clearSales() { sales = emptyList(); count = 0; total = 0.0; profit = 0.0; preferences.edit().remove(SALES).putInt(COUNT, 0).putFloat(TOTAL, 0f).putFloat(PROFIT, 0f).apply() }
    MaterialTheme { Surface(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Mi Negocio SV", style = MaterialTheme.typography.headlineLarge); Text("Controla tus ventas e inventario", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(18.dp))
        when (page) {
            "home" -> HomeScreen(count, total, profit, products.size, stock, { page = "products" }, { page = "sale" }, { page = "potato" }, { page = "history" }, { page = "inventory" })
            "products" -> ProductsScreen(products, ::addProduct) { page = "home" }
            "sale" -> SaleScreen(context, ::addSale) { page = "home" }
            "potato" -> PotatoScreen { page = "home" }
            "history" -> HistoryScreen(sales, ::clearSales) { page = "home" }
            "inventory" -> InventoryScreen(stock, { amount -> stock += amount; preferences.edit().putFloat(STOCK, stock.toFloat()).apply() }) { page = "home" }
        }
    } } }
}

@Composable private fun HomeScreen(count: Int, total: Double, profit: Double, productCount: Int, stock: Double, onProducts: () -> Unit, onSale: () -> Unit, onPotato: () -> Unit, onHistory: () -> Unit, onInventory: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Resumen", style = MaterialTheme.typography.titleLarge); Text("Ventas: $count"); Text("Vendido: $%.2f".format(total), style = MaterialTheme.typography.headlineSmall); Text("Ganancia: $%.2f".format(profit)); Text("Productos: $productCount"); Text("Inventario: %.2f unidades".format(stock)); Text("Versión 1.0")
    } }
    Spacer(Modifier.height(12.dp)); Button(onClick = onProducts, modifier = Modifier.fillMaxWidth()) { Text("Productos") }; Spacer(Modifier.height(6.dp)); Button(onClick = onSale, modifier = Modifier.fillMaxWidth()) { Text("Nueva venta") }; Spacer(Modifier.height(6.dp)); OutlinedButton(onClick = onPotato, modifier = Modifier.fillMaxWidth()) { Text("Calculadora de papa") }; Spacer(Modifier.height(6.dp)); OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text("Historial") }; Spacer(Modifier.height(6.dp)); OutlinedButton(onClick = onInventory, modifier = Modifier.fillMaxWidth()) { Text("Inventario") }
}

@Composable private fun ProductsScreen(products: List<String>, addProduct: (String) -> Unit, onBack: () -> Unit) { var name by remember { mutableStateOf("") }; Text("Productos", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(10.dp)); OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); Button(enabled = name.isNotBlank(), onClick = { addProduct(name); name = "" }, modifier = Modifier.fillMaxWidth()) { Text("Agregar") }; Spacer(Modifier.height(12.dp)); products.forEachIndexed { index, item -> Card(Modifier.fillMaxWidth().padding(bottom = 5.dp)) { Text("${index + 1}. $item", Modifier.padding(12.dp)) } }; OutlinedButton(onClick = onBack) { Text("Volver") } }

@Composable private fun SaleScreen(context: Context, saveSale: (Double, Double, String) -> Unit, onBack: () -> Unit) { var quantityText by remember { mutableStateOf("1") }; var costText by remember { mutableStateOf("") }; var priceText by remember { mutableStateOf("") }; var unit by remember { mutableStateOf("Unidad") }; var marginText by remember { mutableStateOf("20") }; val quantity = quantityText.toDoubleOrNull(); val cost = costText.toDoubleOrNull(); val price = priceText.toDoubleOrNull(); val margin = marginText.toDoubleOrNull(); val recommended = if (cost != null && margin != null && margin >= 0 && margin < 100) cost * (1 + margin / 100) else null
    Text("Nueva venta", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = quantityText, onValueChange = { quantityText = it }, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth()); Text("Unidad: $unit"); Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) { listOf("Unidad", "Libra", "Arroba", "Quintal", "Saco").forEach { option -> OutlinedButton(onClick = { unit = option }) { Text(option) } } }; OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Costo total") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(value = marginText, onValueChange = { marginText = it }, label = { Text("Margen %") }, modifier = Modifier.fillMaxWidth()); if (recommended != null) Text("Precio recomendado: $%.2f".format(recommended)); OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Precio de venta") }, modifier = Modifier.fillMaxWidth()); if (cost != null && price != null && price > 0) Text("Ganancia: $%.2f   Margen: %.1f%%".format(price - cost, (price - cost) / price * 100)); Button(enabled = quantity != null && quantity > 0 && cost != null && price != null && price >= cost, onClick = { val gain = price!! - cost!!; val detail = "${SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())} — $quantity $unit — venta $%.2f — ganancia $%.2f".format(price, gain); saveSale(price, gain, detail); quantityText = "1"; costText = ""; priceText = "" }, modifier = Modifier.fillMaxWidth()) { Text("Registrar venta") }; OutlinedButton(enabled = price != null && cost != null, onClick = { share(context, receipt(context, quantityText, unit, costText, priceText)) }, modifier = Modifier.fillMaxWidth()) { Text("Compartir PDF") }; OutlinedButton(onClick = onBack) { Text("Volver") }
}

@Composable private fun PotatoScreen(onBack: () -> Unit) { var costText by remember { mutableStateOf("") }; var weightText by remember { mutableStateOf("90") }; var marginText by remember { mutableStateOf("20") }; val cost = costText.toDoubleOrNull(); val weight = weightText.toDoubleOrNull(); val margin = marginText.toDoubleOrNull(); val costLb = if (cost != null && weight != null && weight > 0) cost / weight else null; val target = if (costLb != null && margin != null && margin < 100) costLb * (1 + margin / 100) else null; val perDollar = if (target != null && target > 0) 1 / target else null; Text("Calculadora de papa", style = MaterialTheme.typography.headlineMedium); OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Costo quintal $") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(value = weightText, onValueChange = { weightText = it }, label = { Text("Libras") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(value = marginText, onValueChange = { marginText = it }, label = { Text("Margen %") }, modifier = Modifier.fillMaxWidth()); if (costLb != null) Text("Costo/lb: $%.4f".format(costLb)); if (target != null) Text("Precio/lb: $%.4f".format(target), style = MaterialTheme.typography.titleLarge); if (perDollar != null) Text("Vender %.2f lb por $1".format(perDollar), style = MaterialTheme.typography.headlineSmall); if (perDollar != null) Text("Redondeado: %.0f lb por $1".format(ceil(perDollar))); OutlinedButton(onClick = onBack) { Text("Volver") } }

@Composable private fun HistoryScreen(items: List<String>, clear: () -> Unit, onBack: () -> Unit) { Text("Historial de ventas", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(10.dp)); if (items.isEmpty()) Text("No hay ventas registradas.") else { items.reversed().forEachIndexed { index, item -> Card(Modifier.fillMaxWidth().padding(bottom = 6.dp)) { Text("${index + 1}. $item", Modifier.padding(12.dp)) } }; OutlinedButton(onClick = clear) { Text("Borrar historial") } }; OutlinedButton(onClick = onBack) { Text("Volver") } }

@Composable private fun InventoryScreen(stock: Double, addStock: (Double) -> Unit, onBack: () -> Unit) { var quantityText by remember { mutableStateOf("") }; val quantity = quantityText.toDoubleOrNull(); Text("Inventario", style = MaterialTheme.typography.headlineMedium); Text("Existencia: %.2f".format(stock), style = MaterialTheme.typography.headlineSmall); if (stock < 10) Text("⚠️ Inventario bajo", color = MaterialTheme.colorScheme.error); OutlinedTextField(value = quantityText, onValueChange = { quantityText = it }, label = { Text("Cantidad a agregar") }, modifier = Modifier.fillMaxWidth()); Button(enabled = quantity != null && quantity > 0, onClick = { addStock(quantity!!); quantityText = "" }, modifier = Modifier.fillMaxWidth()) { Text("Agregar entrada") }; OutlinedButton(onClick = onBack) { Text("Volver") } }
