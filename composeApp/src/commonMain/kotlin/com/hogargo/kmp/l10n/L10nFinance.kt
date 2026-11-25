package com.hogargo.kmp.l10n

import com.hogargo.kmp.finance.ExpenseCategory
import com.hogargo.kmp.finance.FinanceError

// Textos de Finanzas (ES/EN). En ingles el proyecto usa "home", nunca "household".

val L10n.financeTitle get() = tr("El Cofre Comunitario", "The Family Chest")
val L10n.financeSubtitle get() = tr("El latido financiero de nuestra familia.", "Our family's financial heartbeat.")
val L10n.financeNewExpense get() = tr("Nuevo Gasto", "New Expense")
val L10n.financeThisMonth get() = tr("Este Mes", "This Month")
val L10n.financeSpent get() = tr("Gastado", "Spent")
val L10n.financeAverage get() = tr("Promedio", "Average")
val L10n.financeBreakdown get() = tr("Desglose de Gastos", "Expense Breakdown")
val L10n.financeRecentExpenses get() = tr("Gastos Recientes", "Recent Expenses")
val L10n.financeExpenseNameLabel get() = tr("Descripción", "Description")
val L10n.financeExpenseNameHint get() = tr("Ej: Supermercado", "E.g.: Groceries")
val L10n.financeExpenseCategoryLabel get() = tr("Categoría", "Category")
val L10n.financeExpenseAmountLabel get() = tr("Monto", "Amount")
val L10n.financeExpenseDateLabel get() = tr("Fecha", "Date")
val L10n.financeAdd get() = tr("Agregar", "Add")
val L10n.financeDeleteExpense get() = tr("Eliminar gasto", "Delete expense")
val L10n.financeEmptyExpenses
    get() = tr(
        "Aún no has registrado gastos. Toca \"Nuevo Gasto\" para añadir el primero.",
        "No expenses yet. Tap \"New Expense\" to add the first one.",
    )
val L10n.financeGoalsTitle get() = tr("Metas de ahorro", "Savings goals")
val L10n.financeGoalNew get() = tr("Nueva meta", "New goal")
val L10n.financeGoalCreateTitle get() = tr("Crear Meta de Ahorro", "Create Savings Goal")
val L10n.financeGoalNameLabel get() = tr("Nombre de la meta", "Goal name")
val L10n.financeGoalNameHint get() = tr("Ej: Vacaciones", "E.g.: Vacation")
val L10n.financeGoalTargetLabel get() = tr("Monto objetivo", "Target amount")
val L10n.financeGoalCreateButton get() = tr("Crear Meta", "Create Goal")
val L10n.financeGoalEmptyTitle get() = tr("Aún no tienes una meta de ahorro.", "You don't have a savings goal yet.")
val L10n.financeGoalEmptyAction get() = tr("Crear meta", "Create goal")
val L10n.financeGoalDeleteTitle get() = tr("¿Eliminar meta?", "Delete goal?")
fun L10n.financeGoalDeleteMessage(title: String) =
    tr("Se eliminará \"$title\" junto con su progreso.", "\"$title\" and its progress will be deleted.")
val L10n.financeGoalDeleteButton get() = tr("Eliminar", "Delete")
fun L10n.financeGoalContributeTo(title: String) = tr("Aportar a $title", "Contribute to $title")
val L10n.financeGoalContributeAmountLabel get() = tr("Monto a aportar", "Amount to contribute")
val L10n.financeGoalContributeButton get() = tr("Aportar", "Contribute")

fun L10n.categoryName(category: ExpenseCategory): String = when (category) {
    ExpenseCategory.GROCERIES -> tr("Comestibles", "Groceries")
    ExpenseCategory.BILLS -> tr("Servicios y Facturas", "Utilities & Bills")
    ExpenseCategory.PET -> tr("Cuidado de Mascota", "Pet Care")
    ExpenseCategory.LEISURE -> tr("Ocio y Cenas", "Leisure & Dining")
    ExpenseCategory.OTHER -> tr("Otros", "Other")
}

fun L10n.financeErrorMessage(error: FinanceError): String = when (error) {
    FinanceError.EMPTY_TITLE -> tr("Escribe una descripción", "Write a description")
    FinanceError.INVALID_AMOUNT -> tr("Ingresa un monto válido mayor a 0", "Enter a valid amount greater than 0")
}
