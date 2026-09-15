package com.alejandro.minegociosv

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import kotlin.math.ceil

private const val PREFS = "mi_negocio_sv"
private const val PRODUCTS = "products"
private const val SALES_COUNT = "sales_count"
private const val SALES_TOTAL = "sales_total"
private const val SALES_PROFIT = "sales_profit"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { HomeScreen(applicationContext) } }
}

private fun createReceipt(context: Context, quantity: String, unit: String, cost: String, price: String): File {
    val dir = File(context.cacheDir, "comprobantes")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "comprobante_venta.pdf")
    val pdf = PdfDocument()
    val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val canvas = page.canvas
    val paint = android.graphics.Paint().apply { textSize = 22f }
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
    pdf.finishPage(page)
    file.outputStream().use { pdf.writeTo(it) }
    pdf.close()
    return file
}

private fun shareReceipt(context: Context, file: File) {
    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    context.startActivity(Intent.createChooser(intent, "Compartir comprobante"))
}

@Composable
private fun HomeScreen(context: Context) {
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var screen by remember { mutableStateOf("inicio") }
    var productName by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var costText by remember { mutableStateOf("") }
    var marginText by remember { mutableStateOf("20") }
    var priceText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Unidad") }
    var potatoesCost by remember { mutableStateOf("") }
    var potatoesWeight by remember { mutableStateOf("90") }
    var potatoesMargin by remember { mutableStateOf("20") }
    var products by remember { mutableStateOf(prefs.getStringSet(PRODUCTS, emptySet())?.toList() ?: emptyList()) }
    var salesCount by remember { mutableStateOf(prefs.getInt(SALES_COUNT, 0)) }
    var salesTotal by remember { mutableStateOf(prefs.getFloat(SALES_TOTAL, 0f).toDouble()) }
    var salesProfit by remember { mutableStateOf(prefs.getFloat(SALES_PROFIT, 0f).toDouble()) }
    fun saveProducts(list: List<String>) { products = list; prefs.edit().putStringSet(PRODUCTS, list.toSet()).apply() }
    fun saveSale(total: Double, profit: Double) { salesCount++; salesTotal += total; salesProfit += profit; prefs.edit().putInt(SALES_COUNT, salesCount).putFloat(SALES_TOTAL, salesTotal.toFloat()).putFloat(SALES_PROFIT, salesProfit.toFloat()).apply() }
    val quantity = quantityText.toDoubleOrNull(); val cost = costText.toDoubleOrNull(); val margin = marginText.toDoubleOrNull(); val price = priceText.toDoubleOrNull()
    val recommendedPrice = if (cost != null && margin != null && margin >= 0 && margin < 100) cost * (1 + margin / 100) else null
    val potatoCost = potatoesCost.toDoubleOrNull(); val potatoWeight = potatoesWeight.toDoubleOrNull(); val potatoMargin = potatoesMargin.toDoubleOrNull()
    val potatoCostLb = if (potatoCost != null && potatoWeight != null && potatoWeight > 0) potatoCost / potatoWeight else null
    val potatoTargetLb = if (potatoCostLb != null && potatoMargin != null && potatoMargin >= 0 && potatoMargin < 100) potatoCostLb * (1 + potatoMargin / 100) else null
    val potatoLbPerDollar = if (potatoTargetLb != null && potatoTargetLb > 0) 1 / potatoTargetLb else null
    val potatoRevenue = if (potatoTargetLb != null && potatoWeight != null) potatoTargetLb * potatoWeight else null
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                Text("Mi Negocio SV", style = MaterialTheme.typography.headlineLarge); Text("Tu negocio, más fácil.", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(20.dp))
                when (screen) {
                    "inicio" -> { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Resumen", style = MaterialTheme.typography.titleLarge); Text("Ventas: $salesCount"); Text("Total vendido: $%.2f".format(salesTotal), style = MaterialTheme.typography.headlineMedium); Text("Ganancia: $%.2f".format(salesProfit)); Text("Productos: ${products.size}") } }; Spacer(Modifier.height(16.dp)); Button(onClick = { screen = "productos" }, Modifier.fillMaxWidth()) { Text("Productos") }; Spacer(Modifier.height(8.dp)); OutlinedButton(onClick = { screen = "ventas" }, Modifier.fillMaxWidth()) { Text("Ventas") }; Spacer(Modifier.height(8.dp)); OutlinedButton(onClick = { screen = "papa" }, Modifier.fillMaxWidth()) { Text("Calculadora de papa") }
                    }
                    "productos" -> { Text("Productos", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(12.dp)); OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Nombre del producto") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); Button(onClick = { val name = productName.trim(); if (name.isNotEmpty()) { saveProducts(products + name); productName = "" } }, enabled = productName.trim().isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("Guardar producto") }; Spacer(Modifier.height(12.dp)); products.forEachIndexed { index, name -> Text("${index + 1}. $name") }; Spacer(Modifier.height(16.dp)); OutlinedButton(onClick = { screen = "inicio" }) { Text("Volver al inicio") }
                    }
                    "papa" -> { Text("Calculadora de papa", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(8.dp)); Text("Calcula cuántas libras vender por $1 para lograr tu margen."); Spacer(Modifier.height(10.dp)); OutlinedTextField(value = potatoesCost, onValueChange = { potatoesCost = it }, label = { Text("Costo del quintal ($)") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = potatoesWeight, onValueChange = { potatoesWeight = it }, label = { Text("Libras del quintal") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = potatoesMargin, onValueChange = { potatoesMargin = it }, label = { Text("Margen deseado (%)") }, modifier = Modifier.fillMaxWidth()); if (potatoCostLb != null) Text("Costo por libra: $%.4f".format(potatoCostLb)); if (potatoTargetLb != null) Text("Precio sugerido por libra: $%.4f".format(potatoTargetLb), style = MaterialTheme.typography.titleMedium); if (potatoLbPerDollar != null) Text("Vender aproximadamente %.2f lb por $1".format(potatoLbPerDollar), style = MaterialTheme.typography.titleLarge); if (potatoRevenue != null) Text("Ingreso sugerido por quintal: $%.2f".format(potatoRevenue)); if (potatoLbPerDollar != null) Text("En libras enteras: $1 cada %.0f lb".format(ceil(potatoLbPerDollar))); Spacer(Modifier.height(16.dp)); OutlinedButton(onClick = { screen = "inicio" }) { Text("Volver al inicio") }
                    }
                    else -> { Text("Nueva venta", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(12.dp)); OutlinedTextField(value = quantityText, onValueChange = { quantityText = it }, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); Text("Unidad: $unit"); Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("Unidad", "Libra", "Arroba", "Quintal", "Saco").forEach { option -> OutlinedButton(onClick = { unit = option }) { Text(option) } } }; Spacer(Modifier.height(8.dp)); OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Costo total") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = marginText, onValueChange = { marginText = it }, label = { Text("Margen deseado (%)") }, modifier = Modifier.fillMaxWidth()); if (recommendedPrice != null) { Text("Precio recomendado: $%.2f".format(recommendedPrice), style = MaterialTheme.typography.titleMedium); if (quantity != null && quantity > 0) Text("Recomendado por $unit: $%.2f".format(recommendedPrice / quantity)) }; Spacer(Modifier.height(8.dp)); OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Precio de venta total") }, modifier = Modifier.fillMaxWidth()); if (quantity != null && quantity > 0 && cost != null && price != null) { Text("Ganancia: $%.2f".format(price - cost)); Text("Margen real: %.1f%%".format(if (price > 0) (price - cost) / price * 100 else 0.0)); Text("Costo por $unit: $%.2f".format(cost / quantity)); Text("Venta por $unit: $%.2f".format(price / quantity)) }; Spacer(Modifier.height(12.dp)); Button(enabled = quantity != null && quantity > 0 && cost != null && cost >= 0 && price != null && price >= cost, onClick = { saveSale(price!!, price - cost!!); quantityText = "1"; costText = ""; priceText = "" }, Modifier.fillMaxWidth()) { Text("Registrar venta") }; Spacer(Modifier.height(8.dp)); OutlinedButton(enabled = quantity != null && cost != null && price != null, onClick = { val f = createReceipt(context, quantityText, unit, costText, priceText); shareReceipt(context, f) }, Modifier.fillMaxWidth()) { Text("Compartir comprobante PDF") }; Spacer(Modifier.height(12.dp)); Text("Ventas: $salesCount"); Text("Total vendido: $%.2f".format(salesTotal)); Text("Ganancia acumulada: $%.2f".format(salesProfit)); Spacer(Modifier.height(16.dp)); OutlinedButton(onClick = { screen = "inicio" }) { Text("Volver al inicio") }
                    }
                }
            }
        }
    }
}
