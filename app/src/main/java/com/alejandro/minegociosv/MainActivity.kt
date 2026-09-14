package com.alejandro.minegociosv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.minegociosv.data.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { App() } }
}

@Composable
fun App() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val container = remember { AppContainer(context) }
    val repo = remember { BusinessRepository(container.productDao, container.saleDao) }
    var screen by remember { mutableStateOf("Inicio") }
    val products by repo.observeProducts().collectAsStateWithLifecycle(emptyList())
    val sales by repo.observeSales().collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    Scaffold(bottomBar = { NavigationBar { listOf("Inicio","Productos","Ventas").forEach { s -> NavigationBarItem(selected=s==screen,onClick={screen=s},icon={},label={Text(s)}) } } }) { p ->
        Box(Modifier.padding(p).fillMaxSize()) { when(screen) {
            "Productos" -> ProductScreen(products) { name,cost,price,stock -> scope.launch { repo.addProduct(name,cost,price,stock) } }
            "Ventas" -> SaleScreen(products) { product,qty -> scope.launch { repo.addSale(product.id,qty,product.price,product.cost) } }
            else -> Home(products, sales)
        } }
    }
}

@Composable fun Home(products: List<Product>, sales: List<Sale>) {
    val total = sales.sumOf { it.total }; val profit = sales.sumOf { it.profit }
    Column(Modifier.padding(20.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Mi Negocio SV", style=MaterialTheme.typography.headlineMedium); Text("Resumen del negocio")
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Ventas"); Text("$%.2f".format(total), style=MaterialTheme.typography.headlineSmall) } }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Ganancia estimada"); Text("$%.2f".format(profit), style=MaterialTheme.typography.headlineSmall) } }
        Text("Productos registrados: ${products.size}")
    }
}

@Composable fun ProductScreen(products: List<Product>, add: (String,Double,Double,Double)->Unit) {
    var show by remember { mutableStateOf(false) }
    Column(Modifier.padding(20.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) { Text("Productos", style=MaterialTheme.typography.headlineMedium); Button({show=true}) { Text("+ Agregar") } }; Spacer(Modifier.height(12.dp)); LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) { items(products) { p -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Text(p.name); Text("Venta: $%.2f  •  Stock: %.2f  •  Ganancia: $%.2f".format(p.price,p.stock,p.price-p.cost)) } } } } }
    if(show) ProductDialog({show=false}, add)
}

@Composable fun ProductDialog(close:()->Unit, add:(String,Double,Double,Double)->Unit) {
    var name by remember{mutableStateOf("")}; var cost by remember{mutableStateOf("")}; var price by remember{mutableStateOf("")}; var stock by remember{mutableStateOf("")}
    AlertDialog(onDismissRequest=close,title={Text("Nuevo producto")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){TextField(name,{name=it},label={Text("Nombre")});TextField(cost,{cost=it},label={Text("Costo")});TextField(price,{price=it},label={Text("Precio de venta")});TextField(stock,{stock=it},label={Text("Existencia")})}},confirmButton={Button(enabled=name.isNotBlank()&&cost.toDoubleOrNull()!=null&&price.toDoubleOrNull()!=null&&stock.toDoubleOrNull()!=null,onClick={add(name,cost.toDouble(),price.toDouble(),stock.toDouble());close()}){Text("Guardar")}},dismissButton={TextButton(close){Text("Cancelar")}})
}

@Composable fun SaleScreen(products:List<Product>, sell:(Product,Double)->Unit) {
    var selected by remember{mutableStateOf<Product?>(null)}; var qty by remember{mutableStateOf("1")}
    Column(Modifier.padding(20.dp)){Text("Nueva venta",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(12.dp));products.forEach{p->Button(Modifier.fillMaxWidth().padding(vertical=4.dp),onClick={selected=p}){Text("${p.name}  •  $%.2f".format(p.price))}};selected?.let{p->Spacer(Modifier.height(10.dp));Text("Producto: ${p.name}");TextField(qty,{qty=it},label={Text("Cantidad")});Spacer(Modifier.height(8.dp));Button(enabled=qty.toDoubleOrNull()!=null,onClick={sell(p,qty.toDouble());selected=null;qty="1"}){Text("Registrar venta • $%.2f".format((qty.toDoubleOrNull()?:0.0)*p.price))}}}
}
