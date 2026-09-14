package com.alejandro.minegociosv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MiNegocioSV() } }
}

@Composable
fun MiNegocioSV() {
    var screen by remember { mutableStateOf("Inicio") }
    Scaffold(bottomBar = { NavigationBar { listOf("Inicio","Productos","Ventas").forEach { item -> NavigationBarItem(selected = screen == item, onClick = { screen = item }, icon = {}, label = { Text(item) }) } } }) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) { when(screen) { "Productos" -> Productos(); "Ventas" -> Ventas(); else -> Inicio() } }
    }
}

@Composable fun Inicio() { LazyColumn(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Text("Mi Negocio SV", style = MaterialTheme.typography.headlineMedium) }; item { Text("Resumen de hoy") }; item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Ventas de hoy"); Text("$0.00", style = MaterialTheme.typography.headlineSmall) } } }; item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Ganancia estimada"); Text("$0.00", style = MaterialTheme.typography.headlineSmall) } } } } }
@Composable fun Productos() { Column(Modifier.padding(20.dp)) { Text("Productos", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(16.dp)); Button(onClick = {}) { Text("+ Agregar producto") }; Spacer(Modifier.height(16.dp)); Text("Todavía no hay productos.") } }
@Composable fun Ventas() { Column(Modifier.padding(20.dp)) { Text("Ventas", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(16.dp)); Button(onClick = {}) { Text("+ Nueva venta") }; Spacer(Modifier.height(16.dp)); Text("Todavía no hay ventas.") } }
