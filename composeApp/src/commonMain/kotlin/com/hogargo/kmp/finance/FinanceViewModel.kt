package com.hogargo.kmp.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hogargo.kmp.core.todayLocal
import com.hogargo.kmp.db.Expense
import com.hogargo.kmp.db.SavingsGoal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

data class FinanceUiState(
    val expenses: List<Expense> = emptyList(),
    val goals: List<SavingsGoal> = emptyList(),
    val summary: MonthSummary = MonthSummary(0.0, 0.0, 0, emptyList()),
)

class FinanceViewModel(
    private val repository: FinanceRepository,
    private val householdId: String,
    private val today: () -> LocalDate = ::todayLocal,
) : ViewModel() {

    val state: StateFlow<FinanceUiState> = combine(
        repository.observeExpenses(householdId),
        repository.observeGoals(householdId),
    ) { expenses, goals ->
        FinanceUiState(expenses, goals, FinanceRules.summarize(expenses, today()))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceUiState())

    /** Devuelve el error de validacion o null si se guardo. */
    fun addExpense(title: String, category: ExpenseCategory, amountText: String, date: LocalDate): FinanceError? =
        attempt { repository.addExpense(householdId, title, category, amountText, date) }

    fun deleteExpense(id: String) = repository.deleteExpense(householdId, id)

    fun addGoal(title: String, targetText: String): FinanceError? =
        attempt { repository.addGoal(householdId, title, targetText) }

    fun contribute(goalId: String, amountText: String): FinanceError? =
        attempt { repository.contribute(householdId, goalId, amountText) }

    fun deleteGoal(id: String) = repository.deleteGoal(householdId, id)

    private fun attempt(block: () -> Unit): FinanceError? = try {
        block()
        null
    } catch (e: FinanceException) {
        e.error
    }
}
