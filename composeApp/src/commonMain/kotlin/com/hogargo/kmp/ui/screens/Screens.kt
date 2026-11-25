package com.hogargo.kmp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
private fun ScreenColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { content() }
}

@Composable
fun HomeScreen(onOpenAbout: () -> Unit) = ScreenColumn {
    Text("Inicio", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(24.dp))
    OutlinedButton(onClick = onOpenAbout) { Text("Acerca de y creditos") }
}

/** Seccion aun no implementada: se reemplaza cuando llega el modulo. */
@Composable
fun ComingSoonScreen(title: String) = ScreenColumn {
    Text(title, style = MaterialTheme.typography.headlineMedium)
    Text("Pendiente de implementar", style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun AboutScreen(onBack: () -> Unit) = ScreenColumn {
    Text("Acerca de HogarGo", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(8.dp))
    Text(
        "Microproyecto Kotlin Multiplatform (Android e iOS) de la electiva " +
            "Desarrollo de Aplicaciones Moviles, Universidad del Cauca.",
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(Modifier.height(16.dp))
    Text("Creditos", style = MaterialTheme.typography.titleMedium)
    Text("Nelson Lopez Vidales")
    Text("(agregar nombres completos del equipo)")
    Spacer(Modifier.height(24.dp))
    OutlinedButton(onClick = onBack) { Text("Volver") }
}
