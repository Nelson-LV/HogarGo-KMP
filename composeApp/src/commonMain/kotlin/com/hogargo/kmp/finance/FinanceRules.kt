package com.hogargo.kmp.finance

import com.hogargo.kmp.db.Expense
import kotlinx.datetime.LocalDate
import kotlin.math.roundToLong

enum class ExpenseCategory(val id: String) {
    GROCERIES("groceries"),
    BILLS("bills"),
    PET("pet"),
    LEISURE("leisure"),
    OTHER("other");

    companion object {
        fun fromId(id: String): ExpenseCategory = entries.firstOrNull { it.id == id } ?: OTHER
    }
}

enum class FinanceError { EMPTY_TITLE, INVALID_AMOUNT }

class FinanceException(val error: FinanceError) : Exception(error.name)

/** Una categoria dentro del desglose del mes. [fraction] va de 0 a 1 respecto al total gastado. */
data class CategoryShare(val category: ExpenseCategory, val amount: Double, val fraction: Float)

data class MonthSummary(
    val total: Double,
    val average: Double,
    val count: Int,
    val breakdown: List<CategoryShare>,
)

/** Reglas puras de finanzas (sin base de datos) para poder probarlas facilmente. */
object FinanceRules {
    /** Acepta coma o punto decimal. Devuelve null si no es un numero. */
    fun parseAmount(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

    /** Solo digitos, '.' y ',' y maximo 12 caracteres (campo de monto). */
    fun filterAmountInput(text: String): String =
        text.filter { it.isDigit() || it == '.' || it == ',' }.take(12)

    /** Devuelve el monto valido (> 0) o null. */
    fun validAmount(text: String): Double? = parseAmount(text)?.takeIf { it > 0 && it.isFinite() }

    fun validateExpense(title: String, amountText: String): FinanceError? = when {
        title.trim().isEmpty() -> FinanceError.EMPTY_TITLE
        validAmount(amountText) == null -> FinanceError.INVALID_AMOUNT
        else -> null
    }

    /** Resumen de los gastos cuyo anio-mes coincide con [month] (cualquier dia de ese mes). */
    fun summarize(expenses: List<Expense>, month: LocalDate): MonthSummary {
        val prefix = monthPrefix(month)
        val inMonth = expenses.filter { it.date.startsWith(prefix) }
        val total = inMonth.sumOf { it.amount }
        val average = if (inMonth.isEmpty()) 0.0 else total / inMonth.size
        val breakdown = inMonth
            .groupBy { ExpenseCategory.fromId(it.category) }
            .map { (category, items) ->
                val amount = items.sumOf { it.amount }
                CategoryShare(category, amount, if (total > 0) (amount / total).toFloat() else 0f)
            }
            .sortedByDescending { it.amount }
        return MonthSummary(total, average, inMonth.size, breakdown)
    }

    private fun monthPrefix(date: LocalDate) =
        "${date.year.toString().padStart(4, '0')}-${date.monthNumber.toString().padStart(2, '0')}-"

    /** Dinero como "$1250.50" (siempre dos decimales). */
    fun formatMoney(value: Double): String {
        val cents = (kotlin.math.abs(value) * 100).roundToLong()
        val sign = if (value < 0 && cents > 0) "-" else ""
        return "$sign$${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
    }

    /** Progreso visual de una meta, recortado a 0..1 (puede aportarse mas que el objetivo). */
    fun goalProgress(current: Double, target: Double): Float =
        if (target <= 0) 0f else (current / target).toFloat().coerceIn(0f, 1f)
}
