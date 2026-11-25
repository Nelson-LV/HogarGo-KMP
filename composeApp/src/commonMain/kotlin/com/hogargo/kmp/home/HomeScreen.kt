package com.hogargo.kmp.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.hogargo.kmp.ui.common.PillButton
import com.hogargo.kmp.ui.common.ScreenHeader
import kotlinx.datetime.LocalDate

@Composable
fun HomeScreen(viewModel: HomeViewModel, onOpenAbout: () -> Unit) {
    val state by viewModel.state.collectAsState()
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

        PillButton(L10n.homeAbout, onClick = onOpenAbout, container = HogarGoColors.brandOrangeContainer, content = HogarGoColors.primaryText)
    }
}
