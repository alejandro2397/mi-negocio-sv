package com.alejandro.minegociosv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeScreen() }
    }
}

@Composable
private fun HomeScreen() {
    var showProducts by remember { mutableStateOf(false) }
    var showSales by remember { mutableStateOf(false) }
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                Text("Mi Negocio SV", style = MaterialTheme.typography.headlineLarge)
                Text("Tu negocio, más fácil.", style = MaterialTheme.typography.titleMedium)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tu negocio en un solo lugar", style = MaterialTheme.typography.titleLarge)
                        Text("Controla tus productos y ventas desde tu teléfono.")
                    }
                }
                if (!showProducts && !showSales) {
                    Button(onClick = { showProducts = true }, modifier = Modifier.fillMaxWidth()) { Text("Productos") }
                    OutlinedButton(onClick = { showSales = true }, modifier = Modifier.fillMaxWidth()) { Text("Ventas") }
                }
                if (showProducts) {
                    Text("Productos", style = MaterialTheme.typography.headlineSmall)
                    Text("Aquí podrás agregar y controlar tus productos.")
                    OutlinedButton(onClick = { showProducts = false }) { Text("Volver") }
                }
                if (showSales) {
                    Text("Ventas", style = MaterialTheme.typography.headlineSmall)
                    Text("Aquí podrás registrar tus ventas y consultar tus comprobantes.")
                    OutlinedButton(onClick = { showSales = false }) { Text("Volver") }
                }
            }
        }
    }
}
