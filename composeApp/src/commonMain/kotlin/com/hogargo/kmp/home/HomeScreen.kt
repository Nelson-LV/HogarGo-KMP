package com.hogargo.kmp.home

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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
                text = "Good morning,\n$displayName!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = HogarGoColors.primaryText,
                lineHeight = 34.sp,
            )
            Text(
                text = "Let's make today a cozy day.",
                fontSize = 14.sp,
                color = HogarGoColors.secondaryText,
            )
        }

        // 2. Chip de Daily Energy (Racha)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5E3DB),
            modifier = Modifier.clickable {
                taskViewModel?.scheduleDailyStreakReminder()
            },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("🔥", fontSize = 18.sp)
                Column {
                    Text(
                        "Daily Energy",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = HogarGoColors.primaryText,
                    )
                    Text(
                        "$streakDays/7 day streak",
                        fontSize = 11.sp,
                        color = HogarGoColors.secondaryText,
                    )
                }
            }
        }

        // 3. Card Zori is resting
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
                        "Zori is resting.",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = HogarGoColors.primaryText,
                    )
                    Text(
                        "Complete tasks to wake Zori up and earn today's rewards!",
                        fontSize = 12.sp,
                        color = HogarGoColors.secondaryText,
                        lineHeight = 16.sp,
                    )
                    Text(
                        "Start Day",
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
                        Text("💡", fontSize = 16.sp)
                        Text(
                            "Consejo del día de Zori",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HogarGoColors.primaryText,
                        )
                    }
                    IconButton(
                        onClick = { adviceViewModel.loadDailyAdvice() },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Text("🔄", fontSize = 14.sp)
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
                                "Obteniendo consejo...",
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
                    Text(
                        "⩖",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = HogarGoColors.primaryText,
                    )
                    Text(
                        "Next Task",
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
                                    text = "${nextTask.category} • Today • ${nextTask.dueTime}",
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
                            text = "No pending tasks. Great job!",
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
                        "You don't have a savings goal yet.",
                        fontSize = 13.sp,
                        color = HogarGoColors.secondaryText,
                    )
                    Text(
                        "Create goal",
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
                icon = "⩖",
                label = "New Task",
                onClick = onNavigateToTasks,
                modifier = Modifier.weight(1f),
            )
            QuickActionCard(
                icon = "🧾",
                label = "Add Expense",
                onClick = onNavigateToFinance,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickActionCard(
                icon = "📅",
                label = "Event",
                onClick = onNavigateToCalendar,
                modifier = Modifier.weight(1f),
            )
            QuickActionCard(
                icon = "⋯",
                label = "More",
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
                    "More",
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
                            Text(
                                "ⓘ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = HogarGoColors.primaryText,
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "About HogarGo",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HogarGoColors.primaryText,
                            )
                            Text(
                                "App description and team credits",
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
                            Text("👥", fontSize = 18.sp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Integrantes del Hogar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HogarGoColors.primaryText,
                            )
                            Text(
                                "Ver los miembros registrados de la familia",
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
    icon: String,
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
                Text(icon, fontSize = 20.sp)
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
