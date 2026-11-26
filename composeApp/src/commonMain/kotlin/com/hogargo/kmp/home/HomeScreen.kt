package com.hogargo.kmp.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hogargo.kmp.advice.ui.AdviceUiState
import com.hogargo.kmp.advice.ui.DailyAdviceViewModel
import com.hogargo.kmp.db.Task
import com.hogargo.kmp.finance.FinanceRules
import com.hogargo.kmp.household.HouseholdViewModel
import com.hogargo.kmp.household.ui.HogarCard
import com.hogargo.kmp.household.ui.HogarGoColors
import com.hogargo.kmp.l10n.L10n
import com.hogargo.kmp.core.todayLocal
import com.hogargo.kmp.l10n.shortDate
import kotlinx.datetime.LocalDate
import com.hogargo.kmp.pet.ui.ZoriAvatar
import com.hogargo.kmp.tasks.ui.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    householdViewModel: HouseholdViewModel? = null,
    taskViewModel: TaskViewModel? = null,
    adviceViewModel: DailyAdviceViewModel = viewModel { DailyAdviceViewModel() },
    onNavigateToTasks: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToPet: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val adviceState by adviceViewModel.uiState.collectAsState()
    val me by householdViewModel?.me?.collectAsState() ?: remember { mutableStateOf(null) }
    val tasks by taskViewModel?.tasks?.collectAsState() ?: remember { mutableStateOf(emptyList<Task>()) }
    val streakDays by taskViewModel?.streakDays?.collectAsState() ?: remember { mutableStateOf(0) }
    val completedCount by taskViewModel?.completedCount?.collectAsState() ?: remember { mutableStateOf(0L) }

    var showMoreSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HogarGoColors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Saludo personalizado
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val displayName = me?.name?.ifBlank { "Jhonatan123" } ?: "Jhonatan123"
            Text(
                text = L10n.tr("¡Buenos días,\n$displayName!", "Good morning,\n$displayName!"),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = HogarGoColors.primaryText,
                lineHeight = 34.sp,
            )
            Text(
                text = L10n.tr("Hagamos de hoy un día acogedor.", "Let's make today a cozy day."),
                fontSize = 14.sp,
                color = HogarGoColors.secondaryText,
            )
        }

        // 2. Chip estético de Daily Energy (Racha y recordatorio)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = HogarGoColors.surfaceContainer,
            border = BorderStroke(1.dp, HogarGoColors.borderSubtle.copy(alpha = 0.5f)),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                    taskViewModel?.scheduleDailyStreakReminder()
                },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(HogarGoColors.brandOrangeContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = HogarGoColors.brandOrangeDeep,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = L10n.tr("Energía Diaria", "Daily Energy"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = HogarGoColors.primaryText,
                    )
                    Text(
                        text = L10n.tr("Racha de $streakDays/7 días", "$streakDays/7 day streak"),
                        fontSize = 11.sp,
                        color = HogarGoColors.secondaryText,
                    )
                }
            }
        }

        // 3. Card Zori is resting / awake
        val isAwake = completedCount > 0
        HogarCard(background = HogarGoColors.surfaceContainer) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = if (isAwake) {
                            L10n.tr("¡Zori está feliz y despierto! ✨", "Zori is awake and happy! ✨")
                        } else {
                            L10n.tr("Zori está descansando.", "Zori is resting.")
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = HogarGoColors.primaryText,
                    )
                    Text(
                        text = if (isAwake) {
                            L10n.tr(
                                "¡Completaron tareas hoy! Zori tiene energía y el hogar gana recompensas.",
                                "Tasks were completed today! Zori is full of energy and earning rewards.",
                            )
                        } else {
                            L10n.tr(
                                "¡Completa tareas para despertar a Zori y ganar las recompensas de hoy!",
                                "Complete tasks to wake Zori up and earn today's rewards!",
                            )
                        },
                        fontSize = 12.sp,
                        color = HogarGoColors.secondaryText,
                        lineHeight = 16.sp,
                    )
                    Text(
                        text = if (isAwake) {
                            L10n.tr("Ver Tareas", "View Tasks")
                        } else {
                            L10n.tr("Comenzar Día", "Start Day")
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HogarGoColors.primaryText,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToTasks)
                            .padding(top = 4.dp),
                    )
                }
                Box(
                    modifier = Modifier.clickable(onClick = onNavigateToPet),
                ) {
                    ZoriAvatar(state.equippedIds, 84.dp)
                }
            }
        }

        // 4. Consejo del día de Zori
        HogarCard(background = HogarGoColors.surfaceContainer) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = HogarGoColors.primaryText,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = L10n.tr("Consejo del día de Zori", "Zori's Daily Advice"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HogarGoColors.primaryText,
                        )
                    }
                    IconButton(
                        onClick = { adviceViewModel.loadDailyAdvice() },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            tint = HogarGoColors.primaryText,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                when (val advice = adviceState) {
                    is AdviceUiState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = HogarGoColors.brandOrangeDeep,
                            )
                            Text(
                                text = L10n.tr("Obteniendo consejo...", "Fetching advice..."),
                                fontSize = 12.sp,
                                color = HogarGoColors.secondaryText,
                            )
                        }
                    }
                    is AdviceUiState.Success -> {
                        Text(
                            text = "\"${advice.advice.advice}\"",
                            fontSize = 13.sp,
                            color = HogarGoColors.primaryText,
                            lineHeight = 18.sp,
                        )
                    }
                    is AdviceUiState.Error -> {
                        Text(
                            text = "\"${advice.fallbackAdvice.advice}\"",
                            fontSize = 13.sp,
                            color = HogarGoColors.primaryText,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
        }

        // 5. Next Task
        val nextTask = tasks.firstOrNull { !it.isCompleted }
        HogarCard(background = HogarGoColors.surfaceContainer) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Checklist,
                        contentDescription = null,
                        tint = HogarGoColors.primaryText,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = L10n.tr("Próxima Tarea", "Next Task"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HogarGoColors.primaryText,
                    )
                }
                if (nextTask != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(HogarGoColors.surface)
                            .padding(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Checkbox(
                                checked = nextTask.isCompleted,
                                onCheckedChange = { taskViewModel?.toggleTaskCompleted(nextTask) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = HogarGoColors.brandOrangeDeep,
                                    uncheckedColor = HogarGoColors.secondaryText,
                                ),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = nextTask.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HogarGoColors.primaryText,
                                )
                                Text(
                                    text = "${nextTask.category} • ${taskDayLabel(nextTask.dueDate)} • ${nextTask.dueTime}",
                                    fontSize = 12.sp,
                                    color = HogarGoColors.secondaryText,
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(HogarGoColors.surface)
                            .padding(14.dp),
                    ) {
                        Text(
                            text = L10n.tr("¡No hay tareas pendientes! Gran trabajo.", "No pending tasks. Great job!"),
                            fontSize = 13.sp,
                            color = HogarGoColors.secondaryText,
                        )
                    }
                }
            }
        }

        // 6. Meta de ahorro (Savings Goal)
        HogarCard(background = HogarGoColors.surfaceContainer) {
            val goal = state.firstGoal
            if (goal == null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = L10n.tr("Aún no tienes una meta de ahorro.", "You don't have a savings goal yet."),
                        fontSize = 13.sp,
                        color = HogarGoColors.secondaryText,
                    )
                    Text(
                        text = L10n.tr("Crear meta", "Create goal"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HogarGoColors.primaryText,
                        modifier = Modifier.clickable(onClick = onNavigateToFinance),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            goal.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HogarGoColors.primaryText,
                        )
                        Text(
                            "${FinanceRules.formatMoney(goal.currentAmount)} / ${FinanceRules.formatMoney(goal.targetAmount)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HogarGoColors.brandOrangeDeep,
                        )
                    }
                    val progress = (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = HogarGoColors.brandOrangeDeep,
                        trackColor = HogarGoColors.brandOrangeContainer,
                    )
                }
            }
        }

        // 7. Acciones rápidas (Quick Actions 2x2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickActionCard(
                icon = Icons.Outlined.Checklist,
                label = L10n.tr("Nueva Tarea", "New Task"),
                onClick = onNavigateToTasks,
                modifier = Modifier.weight(1f),
            )
            QuickActionCard(
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                label = L10n.tr("Añadir Gasto", "Add Expense"),
                onClick = onNavigateToFinance,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickActionCard(
                icon = Icons.Outlined.CalendarMonth,
                label = L10n.tr("Evento", "Event"),
                onClick = onNavigateToCalendar,
                modifier = Modifier.weight(1f),
            )
            QuickActionCard(
                icon = Icons.Outlined.MoreHoriz,
                label = L10n.tr("Más", "More"),
                onClick = { showMoreSheet = true },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(16.dp))
    }

    // Bottom Sheet: More
    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            containerColor = HogarGoColors.surfaceContainer,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = HogarGoColors.secondaryText.copy(alpha = 0.5f),
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = L10n.tr("Más", "More"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = HogarGoColors.primaryText,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                // Opción 1: About HogarGo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(HogarGoColors.surface)
                        .clickable {
                            showMoreSheet = false
                            onOpenAbout()
                        }
                        .padding(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFDBC9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = HogarGoColors.primaryText,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = L10n.tr("Acerca de HogarGo", "About HogarGo"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HogarGoColors.primaryText,
                            )
                            Text(
                                text = L10n.tr(
                                    "Descripción de la app y créditos",
                                    "App description and team credits",
                                ),
                                fontSize = 12.sp,
                                color = HogarGoColors.secondaryText,
                            )
                        }
                    }
                }
                // Opción 2: Integrantes del Hogar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(HogarGoColors.surface)
                        .clickable {
                            showMoreSheet = false
                            onOpenProfile()
                        }
                        .padding(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFDBC9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Group,
                                contentDescription = null,
                                tint = HogarGoColors.primaryText,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = L10n.tr("Integrantes del Hogar", "Household Members"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HogarGoColors.primaryText,
                            )
                            Text(
                                text = L10n.tr(
                                    "Ver los miembros registrados de la familia",
                                    "View registered family members",
                                ),
                                fontSize = 12.sp,
                                color = HogarGoColors.secondaryText,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(HogarGoColors.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFDBC9)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = HogarGoColors.primaryText,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = HogarGoColors.primaryText,
            )
        }
    }
}

/** "Hoy" si la tarea es para hoy; si no, la fecha corta. */
private fun taskDayLabel(dueDate: String): String {
    val date = runCatching { LocalDate.parse(dueDate) }.getOrNull() ?: return dueDate
    return if (date == todayLocal()) L10n.tr("Hoy", "Today") else L10n.shortDate(date)
}
