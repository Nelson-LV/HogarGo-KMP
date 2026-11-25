package com.hogargo.kmp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hogargo.kmp.data.HogarGoRepository
import com.hogargo.kmp.household.ui.HogarGoColors
import com.hogargo.kmp.tasks.ui.TaskViewModel
import kotlinx.coroutines.flow.Flow

@Composable
private fun ScreenColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HogarGoColors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) { content() }
}

@Composable
fun HomeScreen(
    repository: HogarGoRepository,
    taskViewModel: TaskViewModel,
    onOpenAbout: () -> Unit,
) = ScreenColumn {
    val expenses by repository.expenseCount.collectAsState(0L)
    val pets by repository.petCount.collectAsState(0L)
    val events by repository.eventCount.collectAsState(0L)
    val streakDays by taskViewModel.streakDays.collectAsState()
    val pendingTasks by taskViewModel.pendingCount.collectAsState()
    val totalCoins by taskViewModel.totalCoins.collectAsState()

    Spacer(Modifier.height(16.dp))

    Text("¡Bienvenido a HogarGo! 🦊", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
    Text("Tareas del hogar y finanzas en equipo", style = MaterialTheme.typography.bodyMedium, color = HogarGoColors.secondaryText)

    Spacer(Modifier.height(24.dp))

    // Card de resumen de racha y monedas
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HogarGoColors.brandOrangeContainer),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("🔥 Racha de Tareas", style = MaterialTheme.typography.labelMedium, color = HogarGoColors.brandOrangeDeep)
                Text("$streakDays hechas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("🪙 Monedas Ganadas", style = MaterialTheme.typography.labelMedium, color = HogarGoColors.brandOrangeDeep)
                Text("$totalCoins 🪙", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // Resumen general
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HogarGoColors.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumen del Hogar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
            Text("📝 Tareas pendientes: $pendingTasks", style = MaterialTheme.typography.bodyMedium)
            Text("💰 Gastos registrados: $expenses", style = MaterialTheme.typography.bodyMedium)
            Text("🐾 Mascotas: $pets", style = MaterialTheme.typography.bodyMedium)
            Text("📅 Eventos: $events", style = MaterialTheme.typography.bodyMedium)
        }
    }

    Spacer(Modifier.height(24.dp))

    OutlinedButton(onClick = onOpenAbout) { Text("Acerca de y créditos") }
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
    Text("Créditos", style = MaterialTheme.typography.titleMedium)
    Text("Jhonatan Palacios Gomez — Modulo de Tareas, Racha e Inicio")
    Text("Nelson Rodrigo López Vidales — Finanzas, Mascota y Calendario")
    Text("Dana Isabella Romero Núñez — Modulo de Hogares")
    Spacer(Modifier.height(24.dp))
    OutlinedButton(onClick = onBack) { Text("Volver") }
}
