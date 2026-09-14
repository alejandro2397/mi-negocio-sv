package com.alejandro.minegociosv

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

@Composable
fun InvoiceScanScreen() {
    val context = LocalContext.current
    var rawText by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var units by remember { mutableStateOf("") }
    var markup by remember { mutableStateOf("50") }
    var suggestion by remember { mutableStateOf<PriceSuggestion?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { InvoiceScanner.scan(it, { rawText = it }, { rawText = "No se pudo leer la factura: ${it.message}" }) }
    }
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Escanear factura", style = MaterialTheme.typography.headlineMedium)
        Text("Toma una foto o selecciona una imagen de tu factura. Revisa los datos antes de guardarlos.")
        Button(onClick = { picker.launch("image/*") }) { Text("📷 Elegir foto de factura") }
        if (rawText.isNotBlank()) Text("Texto detectado:\n$rawText")
        TextField(cost, { cost = it; suggestion = null }, label = { Text("Costo total de compra") })
        TextField(units, { units = it; suggestion = null }, label = { Text("Cantidad comprada (lb, unidades, etc.)") })
        TextField(markup, { markup = it; suggestion = null }, label = { Text("Margen sobre costo (%)") })
        Button(enabled = cost.toDoubleOrNull() != null && units.toDoubleOrNull() != null && markup.toDoubleOrNull() != null, onClick = {
            suggestion = PriceSuggestionCalculator.calculate(cost.toDouble(), units.toDouble(), markup.toDouble())
        }) { Text("Calcular sugerencia") }
        suggestion?.let { s ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text("Sugerencia de venta", style = MaterialTheme.typography.titleLarge)
                Text("Costo por unidad: $%.3f".format(s.costPerUnit))
                Text("Aproximadamente %.2f unidades por $1".format(s.suggestedUnitsPerDollar))
                Text("Ganancia estimada por cada $1 vendido: $%.2f".format(s.estimatedProfitPerDollar))
            } }
        }
    }
}
