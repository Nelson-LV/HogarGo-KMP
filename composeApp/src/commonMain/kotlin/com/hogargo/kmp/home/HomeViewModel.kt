package com.hogargo.kmp.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hogargo.kmp.calendar.CalendarRepository
import com.hogargo.kmp.calendar.CalendarRules
import com.hogargo.kmp.core.todayLocal
import com.hogargo.kmp.db.CalendarEvent
import com.hogargo.kmp.finance.FinanceRepository
import com.hogargo.kmp.finance.FinanceRules
import com.hogargo.kmp.pet.PetRepository
import com.hogargo.kmp.pet.PetRules
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

/** Resumen del hogar en Inicio: gasto del mes, proximo evento, facturas por pagar y Zori. */
data class HomeUiState(
    val monthSpent: Double = 0.0,
    val nextEvent: CalendarEvent? = null,
    val unpaidBills: Int = 0,
    val petLevel: Int = 1,
    val coins: Int = 0,
    val equippedIds: Set<String> = emptySet(),
)

class HomeViewModel(
    finance: FinanceRepository,
    calendar: CalendarRepository,
    private val pet: PetRepository,
    private val householdId: String,
    private val today: () -> LocalDate = ::todayLocal,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        finance.observeExpenses(householdId),
        calendar.observeEvents(householdId),
        calendar.observeBills(householdId),
        pet.observeState(householdId),
        pet.observeWardrobe(householdId),
    ) { expenses, events, bills, petState, wardrobe ->
        val now = today()
        HomeUiState(
            monthSpent = FinanceRules.summarize(expenses, now).total,
            nextEvent = CalendarRules.upcomingEvents(events, now).firstOrNull { !it.done },
            unpaidBills = bills.count { !it.paid },
            petLevel = PetRules.level(petState?.careActions ?: 0),
            coins = pet.coins(householdId),
            equippedIds = wardrobe.filter { it.owned && it.equipped }.map { it.itemId }.toSet(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
