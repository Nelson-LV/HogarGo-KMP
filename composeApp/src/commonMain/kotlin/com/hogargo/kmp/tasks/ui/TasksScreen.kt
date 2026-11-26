package com.hogargo.kmp.tasks.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.hogargo.kmp.db.Member
import com.hogargo.kmp.db.Task
import com.hogargo.kmp.household.HouseholdViewModel
import com.hogargo.kmp.household.ui.HogarGoColors
import com.hogargo.kmp.household.ui.MemberAvatar

@Composable
fun TasksScreen(
    taskViewModel: TaskViewModel,
    householdViewModel: HouseholdViewModel,
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val members by householdViewModel.members.collectAsState()
    val streakDays by taskViewModel.streakDays.collectAsState()
    val totalCoins by taskViewModel.totalCoins.collectAsState()
    val pendingCount by taskViewModel.pendingCount.collectAsState()
    val filter by taskViewModel.currentFilter.collectAsState()
    var showNewTaskDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HogarGoColors.background)
            .padding(16.dp),
    ) {
        // Encabezado
        Text(
            text = "Tareas del hogar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = HogarGoColors.primaryText,
        )
        Text(
            text = "Organícense en equipo y ganen monedas para Zori 🐾",
            style = MaterialTheme.typography.bodyMedium,
            color = HogarGoColors.secondaryText,
        )

        Spacer(Modifier.height(16.dp))

        // Banner de Racha y Monedas
        StreakBanner(streakDays = streakDays, totalCoins = totalCoins)

        if (pendingCount > 0) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HogarGoColors.surfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "⏰ $pendingCount pendiente(s) antes de las 8:00 PM",
                    style = MaterialTheme.typography.bodySmall,
                    color = HogarGoColors.secondaryText,
                )
                IconButton(
                    onClick = { taskViewModel.sendStreakReminderNow() },
                    modifier = Modifier.size(28.dp),
                ) {
                    Text("🔔", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Filtros de Tareas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = filter == TaskFilter.ALL,
                onClick = { taskViewModel.setFilter(TaskFilter.ALL) },
                label = { Text("Todas") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HogarGoColors.brandOrangeContainer,
                    selectedLabelColor = HogarGoColors.brandOrangeDeep,
                ),
            )
            FilterChip(
                selected = filter == TaskFilter.PENDING,
                onClick = { taskViewModel.setFilter(TaskFilter.PENDING) },
                label = { Text("Pendientes") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HogarGoColors.brandOrangeContainer,
                    selectedLabelColor = HogarGoColors.brandOrangeDeep,
                ),
            )
            FilterChip(
                selected = filter == TaskFilter.COMPLETED,
                onClick = { taskViewModel.setFilter(TaskFilter.COMPLETED) },
                label = { Text("Hechas") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HogarGoColors.brandOrangeContainer,
                    selectedLabelColor = HogarGoColors.brandOrangeDeep,
                ),
            )
        }

        Spacer(Modifier.height(12.dp))

        // Lista de Tareas
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No hay tareas registradas en este filtro.\n¡Propón la primera!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HogarGoColors.secondaryText,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        members = members,
                        onToggle = { taskViewModel.toggleTaskCompleted(task) },
                        onDelete = { taskViewModel.deleteTask(task.id) },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Boton agregar nueva tarea
        Button(
            onClick = { showNewTaskDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HogarGoColors.brandOrangeDeep),
        ) {
            Text("+ Proponer nueva tarea", fontWeight = FontWeight.Bold)
        }
    }

    if (showNewTaskDialog) {
        NewTaskDialog(
            members = members,
            onDismiss = { showNewTaskDialog = false },
            onConfirm = { title, category, assigneeId, assigneeName, assignedToOther, coinReward, dueDate, dueTime ->
                taskViewModel.createTask(
                    title = title,
                    category = category,
                    assignedMemberId = assigneeId,
                    assignedMemberName = assigneeName,
                    assignedToOther = assignedToOther,
                    coinReward = coinReward,
                    dueDate = dueDate,
                    dueTime = dueTime,
                )
                showNewTaskDialog = false
            },
        )
    }
}

@Composable
private fun StreakBanner(streakDays: Int, totalCoins: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HogarGoColors.brandOrangeContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(HogarGoColors.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text("🔥", style = MaterialTheme.typography.titleLarge)
            }
            Column {
                Text(
                    text = "Racha del hogar",
                    style = MaterialTheme.typography.labelMedium,
                    color = HogarGoColors.brandOrangeDeep,
                )
                Text(
                    text = "$streakDays tareas hechas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HogarGoColors.primaryText,
                )
            }
        }

        // Monedas
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(HogarGoColors.surface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text("🪙 ", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$totalCoins",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HogarGoColors.primaryText,
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    members: List<Member>,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val assignee = members.firstOrNull { it.id == task.assignedMemberId }
    val isCompleted = task.isCompleted

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) HogarGoColors.surfaceContainerLow else HogarGoColors.surface,
        ),
        border = if (!isCompleted) BorderStroke(1.dp, HogarGoColors.borderSubtle) else null,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Etiqueta de Categoria
                Text(
                    text = task.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = HogarGoColors.brandOrangeDeep,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(HogarGoColors.brandOrangeContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )

                // Recompensa en monedas
                Text(
                    text = "+${task.coinReward} 🪙",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = HogarGoColors.brandOrangeDeep,
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = HogarGoColors.success,
                        uncheckedColor = HogarGoColors.secondaryText,
                    ),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        color = if (isCompleted) HogarGoColors.secondaryText else HogarGoColors.primaryText,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = if (task.dueTime.isNotBlank()) "⏰ ${task.dueTime}" else "Sin hora",
                            style = MaterialTheme.typography.bodySmall,
                            color = HogarGoColors.secondaryText,
                        )
                    }
                }

                // Avatar o nombre del responsable
                val assigneeName = if (task.assignedToOther) task.assignedMemberName else (assignee?.name ?: task.assignedMemberName)
                if (assigneeName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        MemberAvatar(name = assigneeName, size = 28.dp)
                    }
                }
            }
        }
    }
}
