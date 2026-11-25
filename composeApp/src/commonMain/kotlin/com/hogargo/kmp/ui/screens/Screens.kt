package com.hogargo.kmp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hogargo.kmp.data.HogarGoRepository
import kotlinx.coroutines.flow.Flow

@Composable
private fun ScreenColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { content() }
}

@Composable
fun HomeScreen(repository: HogarGoRepository, onOpenAbout: () -> Unit) = ScreenColumn {
    val expenses by repository.expenseCount.collectAsState(0L)
    val pets by repository.petCount.collectAsState(0L)
    val events by repository.eventCount.collectAsState(0L)
    Text("Inicio", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(16.dp))
    Text("Gastos: $expenses  |  Mascotas: $pets  |  Eventos: $events")
    Spacer(Modifier.height(24.dp))
    OutlinedButton(onClick = onOpenAbout) { Text("Acerca de y creditos") }
}

/** Pantalla de seccion con un contador persistido y un boton de prueba (se reemplaza en cada rama). */
@Composable
private fun SectionPlaceholder(title: String, owner: String, count: Flow<Long>, onAddSample: () -> Unit) =
    ScreenColumn {
        val total by count.collectAsState(0L)
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text("Pendiente de implementar: $owner", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Text("Registros guardados: $total")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAddSample) { Text("Agregar registro de prueba") }
    }

@Composable
fun FinanceScreen(repository: HogarGoRepository) =
    SectionPlaceholder("Finanzas", "por asignar", repository.expenseCount, repository::addSampleExpense)

@Composable
fun PetsScreen(repository: HogarGoRepository) =
    SectionPlaceholder("Mascotas", "por asignar", repository.petCount, repository::addSamplePet)

@Composable
fun CalendarScreen(repository: HogarGoRepository) =
    SectionPlaceholder("Calendario", "por asignar", repository.eventCount, repository::addSampleEvent)

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
