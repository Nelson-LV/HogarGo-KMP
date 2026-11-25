package com.hogargo.kmp.finance

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.hogargo.kmp.core.newId
import com.hogargo.kmp.core.nowMillis
import com.hogargo.kmp.core.todayLocal
import com.hogargo.kmp.db.Expense
import com.hogargo.kmp.db.HogarGoDatabase
import com.hogargo.kmp.db.SavingsGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Gastos y metas de ahorro de un hogar. Todas las consultas filtran por householdId. */
class FinanceRepository(
    db: HogarGoDatabase,
    private val now: () -> Long = ::nowMillis,
    private val today: () -> LocalDate = ::todayLocal,
) {
    private val q = db.financeQueries

    fun observeExpenses(householdId: String): Flow<List<Expense>> =
        q.expensesByHousehold(householdId).asFlow().mapToList(Dispatchers.Default)

    fun observeGoals(householdId: String): Flow<List<SavingsGoal>> =
        q.goalsByHousehold(householdId).asFlow().mapToList(Dispatchers.Default)

    fun expenses(householdId: String): List<Expense> = q.expensesByHousehold(householdId).executeAsList()

    fun goals(householdId: String): List<SavingsGoal> = q.goalsByHousehold(householdId).executeAsList()

    /** @throws FinanceException si el titulo esta vacio o el monto no es mayor a 0. */
    fun addExpense(
        householdId: String,
        title: String,
        category: ExpenseCategory,
        amountText: String,
        date: LocalDate = today(),
    ) {
        FinanceRules.validateExpense(title, amountText)?.let { throw FinanceException(it) }
        val amount = FinanceRules.validAmount(amountText)!!
        q.insertExpense(newId(), householdId, title.trim(), category.id, amount, date.toString(), now())
    }

    fun deleteExpense(householdId: String, expenseId: String) = q.deleteExpense(expenseId, householdId)

    fun addGoal(householdId: String, title: String, targetText: String) {
        if (title.trim().isEmpty()) throw FinanceException(FinanceError.EMPTY_TITLE)
        val target = FinanceRules.validAmount(targetText) ?: throw FinanceException(FinanceError.INVALID_AMOUNT)
        q.insertGoal(newId(), householdId, title.trim(), target, now())
    }

    /** Suma el aporte a la meta (puede superar el objetivo). */
    fun contribute(householdId: String, goalId: String, amountText: String) {
        val amount = FinanceRules.validAmount(amountText) ?: throw FinanceException(FinanceError.INVALID_AMOUNT)
        q.contributeToGoal(amount, goalId, householdId)
    }

    fun deleteGoal(householdId: String, goalId: String) = q.deleteGoal(goalId, householdId)

    /** Borra todo lo del hogar (cuando sale la ultima persona). */
    fun deleteAll(householdId: String) {
        q.deleteExpensesByHousehold(householdId)
        q.deleteGoalsByHousehold(householdId)
    }
}
