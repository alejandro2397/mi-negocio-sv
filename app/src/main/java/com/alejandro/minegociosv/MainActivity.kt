package com.alejandro.minegociosv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.minegociosv.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BrandBlue = Color(0xFF1565C0)
private val BrandGreen = Color(0xFF2E7D32)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MiNegocioTheme { App() } }
    }
}

@Composable
private fun MiNegocioTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = BrandBlue,
        secondary = BrandGreen,
        surface = Color(0xFFF8F9FC),
        background = Color(0xFFF8F9FC)
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun App() {
    val context = LocalContext.current
    val container = remember { AppContainer(context.applicationContext) }
    val repo = remember { BusinessRepository(container.productDao, container.saleDao) }
    var screen by remember { mutableStateOf("Inicio") }
    val products by repo.observeProducts().collectAsStateWithLifecycle(emptyList())
    val sales by repo.observeSales().collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                listOf("Inicio", "Productos", "Ventas", "Escanear").forEach { item ->
                    NavigationBarItem(
                        selected = screen == item,
                        onClick = { screen = item },
                        icon = { Text(when (item) { "Inicio" -> "⌂"; "Productos" -> "▣"; "Ventas" -> "＄"; else -> "▤" }) },
                        label = { Text(item) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (screen) {
                "Productos" -> ProductScreen(products) { n, cost, price, stock -> scope.launch { repo.addProduct(n, cost, price, stock) } }
                "Ventas" -> SaleScreen(products, sales) { product, quantity -> scope.launch { repo.addSale(product, quantity) } }
                "Escanear" -> InvoiceScanScreen()
                else -> Home(products, sales, onProducts = { screen = "Productos" }, onSales = { screen = "Ventas" }, onScan = { screen = "Escanear" })
            }
        }
    }
}

@Composable
fun Home(products: List<Product>, sales: List<Sale>, onProducts: () -> Unit, onSales: () -> Unit, onScan: () -> Unit) {
    val total = sales.sumOf { it.total }
    val profit = sales.sumOf { it.profit }
    val lowStock = products.count { it.stock <= 5 }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp)
    ) {
        item {
            Text("Mi Negocio SV", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Controla tus ventas de forma sencilla", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Ventas acumuladas", color = Color.White)
                    Text("$%.2f".format(total), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${sales.size} ventas registradas", color = Color.White.copy(alpha = .85f))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("Ganancia", "$%.2f".format(profit), BrandGreen, Modifier.weight(1f))
                SummaryCard("Productos", products.size.toString(), BrandBlue, Modifier.weight(1f))
            }
        }
        item {
            Text("Acciones rápidas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction("＋ Producto", onProducts, Modifier.weight(1f))
                QuickAction("＋ Venta", onSales, Modifier.weight(1f))
                QuickAction("▤ Factura", onScan, Modifier.weight(1f))
            }
        }
        if (lowStock > 0) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("⚠ Inventario bajo", fontWeight = FontWeight.Bold)
                        Text("Tienes $lowStock producto(s) con 5 unidades o menos.")
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

@Composable
private fun QuickAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(52.dp), contentPadding = PaddingValues(horizontal = 6.dp)) {
        Text(label)
    }
}

@Composable
fun ProductScreen(products: List<Product>, add: (String, Double, Double, Double) -> Unit) {
    var show by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text("Productos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("${products.size} registrados", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Button(onClick = { show = true }) { Text("＋ Agregar") }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(products, key = { it.id }) { p ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(p.name, fontWeight = FontWeight.SemiBold); Text("Costo $%.2f  •  Venta $%.2f".format(p.cost, p.price)); Text("Stock %.2f".format(p.stock), color = if (p.stock <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
    if (show) ProductDialog({ show = false }, add)
}

@Composable
fun ProductDialog(close: () -> Unit, add: (String, Double, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = close,
        title = { Text("Nuevo producto") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            TextField(name, { name = it }, label = { Text("Nombre") })
            TextField(cost, { cost = it }, label = { Text("Costo") })
            TextField(price, { price = it }, label = { Text("Precio de venta") })
            TextField(stock, { stock = it }, label = { Text("Existencia") })
        } },
        confirmButton = { Button(enabled = name.isNotBlank() && cost.toDoubleOrNull() != null && price.toDoubleOrNull() != null && stock.toDoubleOrNull() != null, onClick = { add(name, cost.toDouble(), price.toDouble(), stock.toDouble()); close() }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = close) { Text("Cancelar") } }
    )
}

@Composable
fun SaleScreen(products: List<Product>, sales: List<Sale>, sell: (Product, Double) -> Unit) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf("1") }
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("Ventas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Registra y comparte tus comprobantes", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
            items(sales, key = { it.id }) { sale ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                    val name = products.firstOrNull { it.id == sale.productId }?.name ?: "Producto"
                    Text(name, fontWeight = FontWeight.SemiBold)
                    Text("${sale.quantity} × $%.2f = $%.2f".format(sale.unitPrice, sale.total))
                    Text("Ganancia $%.2f".format(sale.profit), color = BrandGreen)
                    Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(sale.createdAt)), style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { sharePdfReceipt(context, Receipt(sale = sale, productName = name)) }) { Text("Compartir comprobante PDF") }
                } }
            }
        }
        Text("Nueva venta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        products.filter { it.stock > 0 }.take(8).forEach { product ->
            OutlinedButton(Modifier.fillMaxWidth().padding(vertical = 2.dp), onClick = { selected = product }) { Text("${product.name}  •  $%.2f  •  Stock %.2f".format(product.price, product.stock)) }
        }
        selected?.let { product ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextField(quantity, { quantity = it }, label = { Text("Cantidad") }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Button(enabled = quantity.toDoubleOrNull()?.let { it > 0 && it <= product.stock } == true, onClick = { sell(product, quantity.toDouble()); selected = null; quantity = "1" }) { Text("Registrar") }
            }
        }
    }
}
