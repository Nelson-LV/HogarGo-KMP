package com.hogargo.kmp.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hogargo.kmp.advice.ui.AdviceUiState
import com.hogargo.kmp.advice.ui.DailyAdviceViewModel
import com.hogargo.kmp.finance.FinanceRules
import com.hogargo.kmp.household.ui.HogarCard
import com.hogargo.kmp.household.ui.HogarGoColors
import com.hogargo.kmp.l10n.L10n
import com.hogargo.kmp.l10n.homeAbout
import com.hogargo.kmp.l10n.homeNextEvent
import com.hogargo.kmp.l10n.homeNoEvents
import com.hogargo.kmp.l10n.homeSpentThisMonth
import com.hogargo.kmp.l10n.homeSubtitle
import com.hogargo.kmp.l10n.homeTitle
import com.hogargo.kmp.l10n.homeUnpaidBills
import com.hogargo.kmp.l10n.petCoinsBalance
import com.hogargo.kmp.l10n.petLevel
import com.hogargo.kmp.l10n.shortDate
import com.hogargo.kmp.pet.ui.ZoriAvatar
import com.hogargo.kmp.tasks.ui.TaskViewModel
import com.hogargo.kmp.ui.common.PillButton
import com.hogargo.kmp.ui.common.ScreenHeader
import kotlinx.datetime.LocalDate

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    adviceViewModel: DailyAdviceViewModel = viewModel { DailyAdviceViewModel() },
    taskViewModel: TaskViewModel? = null,
    onOpenAbout: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val adviceState by adviceViewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HogarGoColors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader(L10n.homeTitle, L10n.homeSubtitle)

        HogarCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ZoriAvatar(state.equippedIds, 96.dp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Zori · " + L10n.petLevel(state.petLevel), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
                    Text(L10n.petCoinsBalance(state.coins), color = HogarGoColors.brandOrangeDeep, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        HogarCard {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(L10n.homeSpentThisMonth, fontSize = 13.sp, color = HogarGoColors.secondaryText)
                Text(FinanceRules.formatMoney(state.monthSpent), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
            }
        }

        HogarCard {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(L10n.homeNextEvent, fontSize = 13.sp, color = HogarGoColors.secondaryText)
                val event = state.nextEvent
                if (event == null) {
                    Text(L10n.homeNoEvents, color = HogarGoColors.primaryText)
                } else {
                    Text(event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
                    Text(
                        L10n.shortDate(LocalDate.parse(event.date)) + (event.timeLabel?.let { " • $it" } ?: ""),
                        fontSize = 13.sp,
                        color = HogarGoColors.secondaryText,
                    )
                }
                Text(L10n.homeUnpaidBills(state.unpaidBills), fontSize = 13.sp, color = HogarGoColors.secondaryText, modifier = Modifier.padding(top = 8.dp))
            }
        }

        // Consejo del Dia (Advice Slip API)
        HogarCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "💡 " + L10n.tr("Consejo del Día", "Daily Advice"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HogarGoColors.primaryText,
                    )
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
                                L10n.tr("Consultando consejo del día...", "Fetching daily advice..."),
                                fontSize = 13.sp,
                                color = HogarGoColors.secondaryText,
                            )
                        }
                    }
                    is AdviceUiState.Success -> {
                        Text(
                            text = "“${advice.advice.advice}”",
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = HogarGoColors.primaryText,
                        )
                    }
                    is AdviceUiState.Error -> {
                        Text(
                            text = "“${advice.fallbackAdvice.advice}”",
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = HogarGoColors.primaryText,
                        )
                        Text(
                            L10n.tr("📱 Modo sin conexión (consejo local)", "📱 Offline mode (local advice)"),
                            fontSize = 11.sp,
                            color = HogarGoColors.secondaryText,
                        )
                    }
                }
            }
        }

        // Racha del Hogar y Recordatorio 8:00 PM
        if (taskViewModel != null) {
            val streakDays by taskViewModel.streakDays.collectAsState()
            val pendingCount by taskViewModel.pendingCount.collectAsState()
            HogarCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "🔥 " + L10n.tr("Racha del Hogar", "Home Streak"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HogarGoColors.primaryText,
                        )
                        Text(
                            "$streakDays " + L10n.tr("hechas", "done"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HogarGoColors.brandOrangeDeep,
                        )
                    }
                    Text(
                        if (pendingCount > 0) {
                            L10n.tr(
                                "Tienes $pendingCount tarea(s) pendiente(s). Complétalas antes de las 8:00 PM para proteger tu racha.",
                                "You have $pendingCount pending task(s). Complete them before 8:00 PM to keep your streak.",
                            )
                        } else {
                            L10n.tr(
                                "¡Todas las tareas están al día! Tu racha sigue activa hoy 🔥",
                                "All tasks are up to date! Your streak is safe today 🔥",
                            )
                        },
                        fontSize = 13.sp,
                        color = HogarGoColors.secondaryText,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PillButton(
                            text = L10n.tr("⏰ Activar 8:00 PM", "⏰ Set 8:00 PM"),
                            onClick = { taskViewModel.scheduleDailyStreakReminder() },
                            container = HogarGoColors.brandOrangeContainer,
                            content = HogarGoColors.brandOrangeDeep,
                            modifier = Modifier.weight(1f),
                        )
                        PillButton(
                            text = L10n.tr("🔔 Probar aviso", "🔔 Test notification"),
                            onClick = { taskViewModel.sendStreakReminderNow() },
                            container = HogarGoColors.surfaceContainerLow,
                            content = HogarGoColors.primaryText,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        PillButton(L10n.homeAbout, onClick = onOpenAbout, container = HogarGoColors.brandOrangeContainer, content = HogarGoColors.primaryText)
    }
}
