package com.hogargo.kmp.finance

import com.hogargo.kmp.db.Expense
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FinanceRulesTest {
    private fun expense(amount: Double, category: ExpenseCategory, date: String) =
        Expense("id-$amount-$date", "h1", "gasto", category.id, amount, date, 0)

    private val today = LocalDate(2026, 11, 20)

    @Test
    fun monthTotalsIgnoreOtherMonths() {
        val list = listOf(
            expense(10.0, ExpenseCategory.GROCERIES, "2026-11-01"),
            expense(20.0, ExpenseCategory.GROCERIES, "2026-11-10"),
            expense(30.0, ExpenseCategory.LEISURE, "2026-11-15"),
            expense(500.0, ExpenseCategory.BILLS, "2026-10-31"),
        )
        val summary = FinanceRules.summarize(list, today)
        assertEquals(60.0, summary.total)
        assertEquals(20.0, summary.average)
        assertEquals(3, summary.count)
        assertEquals(listOf(ExpenseCategory.GROCERIES, ExpenseCategory.LEISURE), summary.breakdown.map { it.category })
    }

    @Test
    fun breakdownIsSortedByAmountAndProportional() {
        val list = listOf(
            expense(40.0, ExpenseCategory.GROCERIES, "2026-11-02"),
            expense(20.0, ExpenseCategory.LEISURE, "2026-11-03"),
        )
        val breakdown = FinanceRules.summarize(list, today).breakdown
        assertEquals(ExpenseCategory.GROCERIES, breakdown[0].category)
        assertEquals(0.6667f, breakdown[0].fraction, 0.001f)
        assertEquals(0.3333f, breakdown[1].fraction, 0.001f)
    }

    @Test
    fun emptyMonthHasZeroAverage() {
        val summary = FinanceRules.summarize(emptyList(), today)
        assertEquals(0.0, summary.total)
        assertEquals(0.0, summary.average)
        assertEquals(emptyList(), summary.breakdown)
    }

    @Test
    fun amountsAcceptCommaAndRejectInvalid() {
        assertEquals(12.5, FinanceRules.validAmount("12,5"))
        assertEquals(12.5, FinanceRules.validAmount(" 12.5 "))
        assertNull(FinanceRules.validAmount("0"))
        assertNull(FinanceRules.validAmount("-3"))
        assertNull(FinanceRules.validAmount("abc"))
        assertNull(FinanceRules.validAmount(""))
    }

    @Test
    fun validationOrderTitleThenAmount() {
        assertEquals(FinanceError.EMPTY_TITLE, FinanceRules.validateExpense("  ", "5"))
        assertEquals(FinanceError.INVALID_AMOUNT, FinanceRules.validateExpense("Pan", "0"))
        assertNull(FinanceRules.validateExpense("Pan", "5"))
    }

    @Test
    fun amountInputIsFilteredAndLimited() {
        assertEquals("12.5,3", FinanceRules.filterAmountInput("1a2.5,3x"))
        assertEquals(12, FinanceRules.filterAmountInput("1234567890123456").length)
    }

    @Test
    fun moneyFormat() {
        assertEquals("$1250.50", FinanceRules.formatMoney(1250.5))
        assertEquals("$0.00", FinanceRules.formatMoney(0.0))
        assertEquals("-$12.50", FinanceRules.formatMoney(-12.5))
        assertEquals("$0.05", FinanceRules.formatMoney(0.05))
    }

    @Test
    fun goalProgressIsClamped() {
        assertEquals(1f, FinanceRules.goalProgress(120.0, 100.0))
        assertEquals(0.5f, FinanceRules.goalProgress(50.0, 100.0))
        assertEquals(0f, FinanceRules.goalProgress(10.0, 0.0))
    }
}
