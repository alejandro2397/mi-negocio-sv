package com.alejandro.minegociosv

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import kotlin.math.ceil

private const val PREFS = "mi_negocio_sv"
private const val PRODUCTS = "products"
private const val SALES_COUNT = "sales_count"
private const val SALES_TOTAL = "sales_total"
private const val SALES_PROFIT = "sales_profit"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App(applicationContext) }
    }
}

private fun createReceipt(context: Context, quantity: String, unit: String, cost: String, price: String): File {
    val dir = File(context.cacheDir, "comprobantes")
    dir.mkdirs()
    val file = File(dir, "comprobante_venta.pdf")
    val document = PdfDocument()
    val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val canvas = page.canvas
    val paint = android.graphics.Paint()
    paint.textSize = 22f
    canvas.drawText("MI NEGOCIO SV", 50f, 70f, paint)
    paint.textSize = 16f
    canvas.drawText("COMPROBANTE DE VENTA", 50f, 105f, paint)
    canvas.drawText("Cantidad: $quantity $unit", 50f, 160f, paint)
    canvas.drawText("Costo: $$cost", 50f, 195f, paint)
    canvas.drawText("Venta: $$price", 50f, 230f, paint)
    val c = cost.toDoubleOrNull() ?: 0.0
    val p = price.toDoubleOrNull() ?: 0.0
    canvas.drawText("Ganancia: $%.2f".format(p - c), 50f, 265f, paint)
    canvas.drawText("Gracias por su compra", 50f, 330f, paint)
    document.finishPage(page)
    file.outputStream().use { document.writeTo(it) }
    document.close()
    return file
}

private fun shareReceipt(context: Context, file: File) {
    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir comprobante"))
}

@Composable
private fun App(context: Context) {
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var screen by remember { mutableStateOf("inicio") }
    var products by remember { mutableStateOf(prefs.getStringSet(PRODUCTS, emptySet())?.toList() ?: emptyList()) }
    var salesCount by remember { mutableStateOf(prefs.getInt(SALES_COUNT, 0)) }
    var salesTotal by remember { mutableStateOf(prefs.getFloat(SALES_TOTAL, 0f).toDouble()) }
    var salesProfit by remember { mutableStateOf(prefs.getFloat(SALES_PROFIT, 0f).toDouble()) }

    fun addProduct(name: String) {
        val clean = name.trim()
        if (clean.isNotEmpty()) {
            products = products + clean
            prefs.edit().putStringSet(PRODUCTS, products.toSet()).apply()
        }
    }

    fun addSale(total: Double, profit: Double) {
        salesCount += 1
        salesTotal += total
        salesProfit += profit
        prefs.edit().putInt(SALES_COUNT, salesCount)
            .putFloat(SALES_TOTAL, salesTotal.toFloat())
            .putFloat(SALES_PROFIT, salesProfit.toFloat()).apply()
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Text("Mi Negocio SV", style = MaterialTheme.typography.headlineLarge)
                Text("Tu negocio, más fácil.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(20.dp))
                when (screen) {
                    "inicio" -> HomeScreen(salesCount, salesTotal, salesProfit, products.size,
                        { screen = "productos" }, { screen = "ventas" }, { screen = "papa" })
                    "productos" -> ProductsScreen(products, ::addProduct) { screen = "inicio" }
                    "papa" -> PotatoScreen { screen = "inicio" }
                    else -> SalesScreen(context, ::addSale) { screen = "inicio" }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(count: Int, total: Double, profit: Double, productCount: Int,
                       onProducts: () -> Unit, onSales: () -> Unit, onPotatoes: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumen", style = MaterialTheme.typography.titleLarge)
            Text("Ventas: $count")
            Text("Total vendido: $%.2f".format(total), style = MaterialTheme.typography.headlineMedium)
            Text("Ganancia: $%.2f".format(profit))
            Text("Productos: $productCount")
        }
    }
    Spacer(Modifier.height(16.dp))
    Button(onClick = onProducts, Modifier.fillMaxWidth()) { Text("Productos") }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = onSales, Modifier.fillMaxWidth()) { Text("Ventas") }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = onPotatoes, Modifier.fillMaxWidth()) { Text("Calculadora de papa") }
}

@Composable
private fun ProductsScreen(products: List<String>, addProduct: (String) -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    Text("Productos", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del producto") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    Button(enabled = name.trim().isNotEmpty(), onClick = { addProduct(name); name = "" }, modifier = Modifier.fillMaxWidth()) { Text("Guardar producto") }
    Spacer(Modifier.height(12.dp))
    products.forEachIndexed { index, item -> Text("${index + 1}. $item") }
    Spacer(Modifier.height(16.dp))
    OutlinedButton(onClick = onBack) { Text("Volver al inicio") }
}

@Composable
private fun PotatoScreen(onBack: () -> Unit) {
    var costText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("90") }
    var marginText by remember { mutableStateOf("20") }
    val cost = costText.toDoubleOrNull()
    val weight = weightText.toDoubleOrNull()
    val margin = marginText.toDoubleOrNull()
    val costLb = if (cost != null && weight != null && weight > 0) cost / weight else null
    val targetLb = if (costLb != null && margin != null && margin >= 0 && margin < 100) costLb * (1 + margin / 100) else null
    val lbPerDollar = if (targetLb != null && targetLb > 0) 1 / targetLb else null
    val revenue = if (targetLb != null && weight != null) targetLb * weight else null

    Text("Calculadora de papa", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(8.dp))
    Text("Calcula el precio y cuántas libras vender por $1.")
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Costo del quintal ($)") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(value = weightText, onValueChange = { weightText = it }, label = { Text("Libras del quintal") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(value = marginText, onValueChange = { marginText = it }, label = { Text("Margen deseado (%)") }, modifier = Modifier.fillMaxWidth())
    if (costLb != null) Text("Costo por libra: $%.4f".format(costLb))
    if (targetLb != null) Text("Precio sugerido por libra: $%.4f".format(targetLb), style = MaterialTheme.typography.titleMedium)
    if (lbPerDollar != null) Text("Vender %.2f lb por $1".format(lbPerDollar), style = MaterialTheme.typography.titleLarge)
    if (revenue != null) Text("Ingreso sugerido por quintal: $%.2f".format(revenue))
    if (lbPerDollar != null) Text("Redondeado: $1 cada %.0f lb".format(ceil(lbPerDollar)))
    Spacer(Modifier.height(16.dp))
    OutlinedButton(onClick = onBack) { Text("Volver al inicio") }
}

@Composable
private fun SalesScreen(context: Context, addSale: (Double, Double) -> Unit, onBack: () -> Unit) {
    var quantityText by remember { mutableStateOf("1") }
    var costText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Unidad") }
    var marginText by remember { mutableStateOf("20") }
    val quantity = quantityText.toDoubleOrNull()
    val cost = costText.toDoubleOrNull()
    val price = priceText.toDoubleOrNull()
    val margin = marginText.toDoubleOrNull()
    val recommended = if (cost != null && margin != null && margin >= 0 && margin < 100) cost * (1 + margin / 100) else null

    Text("Nueva venta", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(value = quantityText, onValueChange = { quantityText = it }, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    Text("Unidad: $unit")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("Unidad", "Libra", "Arroba", "Quintal", "Saco").forEach { option ->
            OutlinedButton(onClick = { unit = option }) { Text(option) }
        }
    }
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Costo total") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(value = marginText, onValueChange = { marginText = it }, label = { Text("Margen deseado (%)") }, modifier = Modifier.fillMaxWidth())
    if (recommended != null) Text("Precio recomendado: $%.2f".format(recommended), style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Precio de venta total") }, modifier = Modifier.fillMaxWidth())
    if (quantity != null && quantity > 0 && cost != null && price != null) {
        Text("Ganancia: $%.2f".format(price - cost))
        Text("Margen real: %.1f%%".format(if (price > 0) (price - cost) / price * 100 else 0.0))
        Text("Costo por $unit: $%.2f".format(cost / quantity))
        Text("Venta por $unit: $%.2f".format(price / quantity))
    }
    Spacer(Modifier.height(12.dp))
    Button(enabled = quantity != null && quantity > 0 && cost != null && cost >= 0 && price != null && price >= cost,
        onClick = { addSale(price!!, price - cost!!); quantityText = "1"; costText = ""; priceText = "" }, modifier = Modifier.fillMaxWidth()) { Text("Registrar venta") }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(enabled = quantity != null && cost != null && price != null,
        onClick = { shareReceipt(context, createReceipt(context, quantityText, unit, costText, priceText)) }, modifier = Modifier.fillMaxWidth()) { Text("Compartir comprobante PDF") }
    Spacer(Modifier.height(16.dp))
    OutlinedButton(onClick = onBack) { Text("Volver al inicio") }
}
