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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.minegociosv.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() { override fun onCreate(b: Bundle?) { super.onCreate(b); setContent { App() } } }

@Composable fun App() {
    val context=LocalContext.current; val c=remember{AppContainer(context)}; val repo=remember{BusinessRepository(c.productDao,c.saleDao)}
    var screen by remember{mutableStateOf("Inicio")}; val products by repo.observeProducts().collectAsStateWithLifecycle(emptyList()); val sales by repo.observeSales().collectAsStateWithLifecycle(emptyList()); val scope=rememberCoroutineScope()
    Scaffold(bottomBar={NavigationBar{listOf("Inicio","Productos","Ventas").forEach{s->NavigationBarItem(screen==s,{screen=s},{},{Text(s)})}}}){p->Box(Modifier.padding(p).fillMaxSize()){when(screen){"Productos"->ProductScreen(products){n,cost,price,stock->scope.launch{repo.addProduct(n,cost,price,stock)}};"Ventas"->SaleScreen(products,sales){product,q->scope.launch{repo.addSale(product,q)}};else->Home(products,sales)}}}
}
@Composable fun Home(products:List<Product>,sales:List<Sale>){val total=sales.sumOf{it.total};val profit=sales.sumOf{it.profit};Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Mi Negocio SV",style=MaterialTheme.typography.headlineMedium);Text("Resumen del negocio");Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Ventas");Text("$%.2f".format(total),style=MaterialTheme.typography.headlineSmall)}};Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Ganancia estimada");Text("$%.2f".format(profit),style=MaterialTheme.typography.headlineSmall)}};Text("Productos: ${products.size}")}}
@Composable fun ProductScreen(products:List<Product>,add:(String,Double,Double,Double)->Unit){var show by remember{mutableStateOf(false)};Column(Modifier.padding(20.dp)){Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween){Text("Productos",style=MaterialTheme.typography.headlineMedium);Button({show=true}){Text("+ Agregar")}};Spacer(Modifier.height(12.dp));LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){items(products){p->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text(p.name);Text("Costo: $%.2f  Venta: $%.2f  • Stock: %.2f".format(p.cost,p.price,p.stock));if(p.stock<=5)Text("⚠ Inventario bajo")}}}}};if(show)ProductDialog({show=false},add)}
@Composable fun ProductDialog(close:()->Unit,add:(String,Double,Double,Double)->Unit){var n by remember{mutableStateOf("")};var c by remember{mutableStateOf("")};var p by remember{mutableStateOf("")};var s by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Nuevo producto")},text={Column(verticalArrangement=Arrangement.spacedBy(6.dp)){TextField(n,{n=it},label={Text("Nombre")});TextField(c,{c=it},label={Text("Costo")});TextField(p,{p=it},label={Text("Precio de venta")});TextField(s,{s=it},label={Text("Existencia")})}},confirmButton={Button(n.isNotBlank()&&c.toDoubleOrNull()!=null&&p.toDoubleOrNull()!=null&&s.toDoubleOrNull()!=null,{add(n,c.toDouble(),p.toDouble(),s.toDouble());close()}){Text("Guardar")}},dismissButton={TextButton(close){Text("Cancelar")}})}
@Composable fun SaleScreen(products:List<Product>,sales:List<Sale>,sell:(Product,Double)->Unit){val context=LocalContext.current;var selected by remember{mutableStateOf<Product?>(null)};var q by remember{mutableStateOf("1")};Column(Modifier.padding(20.dp)){Text("Ventas",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(10.dp));Text("Historial",style=MaterialTheme.typography.titleMedium);LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)){items(sales){s->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(10.dp)){val name=products.firstOrNull{it.id==s.productId}?.name ?: "Producto";Text(name);Text("${s.quantity} × $%.2f = $%.2f  •  Ganancia $%.2f".format(s.unitPrice,s.total,s.profit));Text(SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(Date(s.createdAt)),style=MaterialTheme.typography.bodySmall);Button(onClick={sharePdfReceipt(context,Receipt(sale=s,productName=name))}){Text("Compartir PDF")}}}}};Spacer(Modifier.height(8.dp));Text("Nueva venta",style=MaterialTheme.typography.titleMedium);products.filter{it.stock>0}.forEach{p->Button(Modifier.fillMaxWidth().padding(vertical=2.dp),{selected=p}){Text("${p.name} • $%.2f • Stock %.2f".format(p.price,p.stock))}};selected?.let{p->TextField(q,{q=it},label={Text("Cantidad")});Button(q.toDoubleOrNull()!=null&&q.toDouble()>0&&q.toDouble()<=p.stock,{sell(p,q.toDouble());selected=null;q="1"}){Text("Registrar venta")}}}}
