package com.hogargo.kmp.finance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hogargo.kmp.core.todayLocal
import com.hogargo.kmp.db.Expense
import com.hogargo.kmp.db.SavingsGoal
import com.hogargo.kmp.finance.CategoryShare
import com.hogargo.kmp.finance.ExpenseCategory
import com.hogargo.kmp.finance.FinanceError
import com.hogargo.kmp.finance.FinanceRules
import com.hogargo.kmp.finance.FinanceViewModel
import com.hogargo.kmp.household.ui.HogarCard
import com.hogargo.kmp.household.ui.HogarFieldKind
import com.hogargo.kmp.household.ui.HogarGoColors
import com.hogargo.kmp.household.ui.HogarTextField
import com.hogargo.kmp.l10n.L10n
import com.hogargo.kmp.l10n.categoryName
import com.hogargo.kmp.l10n.financeAdd
import com.hogargo.kmp.l10n.financeAverage
import com.hogargo.kmp.l10n.financeBreakdown
import com.hogargo.kmp.l10n.financeDeleteExpense
import com.hogargo.kmp.l10n.financeEmptyExpenses
import com.hogargo.kmp.l10n.financeErrorMessage
import com.hogargo.kmp.l10n.financeExpenseAmountLabel
import com.hogargo.kmp.l10n.financeExpenseCategoryLabel
import com.hogargo.kmp.l10n.financeExpenseDateLabel
import com.hogargo.kmp.l10n.financeExpenseNameHint
import com.hogargo.kmp.l10n.financeExpenseNameLabel
import com.hogargo.kmp.l10n.financeGoalContributeAmountLabel
import com.hogargo.kmp.l10n.financeGoalContributeButton
import com.hogargo.kmp.l10n.financeGoalContributeTo
import com.hogargo.kmp.l10n.financeGoalCreateButton
import com.hogargo.kmp.l10n.financeGoalCreateTitle
import com.hogargo.kmp.l10n.financeGoalDeleteButton
import com.hogargo.kmp.l10n.financeGoalDeleteMessage
import com.hogargo.kmp.l10n.financeGoalDeleteTitle
import com.hogargo.kmp.l10n.financeGoalEmptyAction
import com.hogargo.kmp.l10n.financeGoalEmptyTitle
import com.hogargo.kmp.l10n.financeGoalNameHint
import com.hogargo.kmp.l10n.financeGoalNameLabel
import com.hogargo.kmp.l10n.financeGoalNew
import com.hogargo.kmp.l10n.financeGoalTargetLabel
import com.hogargo.kmp.l10n.financeGoalsTitle
import com.hogargo.kmp.l10n.financeNewExpense
import com.hogargo.kmp.l10n.financeRecentExpenses
import com.hogargo.kmp.l10n.financeSpent
import com.hogargo.kmp.l10n.financeSubtitle
import com.hogargo.kmp.l10n.financeThisMonth
import com.hogargo.kmp.l10n.financeTitle
import com.hogargo.kmp.l10n.shortDate
import com.hogargo.kmp.ui.common.HogarDateField
import com.hogargo.kmp.ui.common.PillButton
import com.hogargo.kmp.ui.common.ScreenHeader
import kotlinx.datetime.LocalDate

private val categoryColor = mapOf(
    ExpenseCategory.GROCERIES to Color(0xFF8D4F11),
    ExpenseCategory.BILLS to Color(0xFF904917),
    ExpenseCategory.PET to Color(0xFFFEAC67),
    ExpenseCategory.LEISURE to Color(0xFFFFDBC9),
    ExpenseCategory.OTHER to Color(0xFFE4E3DB),
)

private val categoryIcon = mapOf(
    ExpenseCategory.GROCERIES to "🛒",
    ExpenseCategory.BILLS to "⚡",
    ExpenseCategory.PET to "🐾",
    ExpenseCategory.LEISURE to "🍽️",
    ExpenseCategory.OTHER to "⋯",
)

@Composable
fun FinanceScreen(viewModel: FinanceViewModel) {
    val state by viewModel.state.collectAsState()
    var showExpense by remember { mutableStateOf(false) }
    var showGoal by remember { mutableStateOf(false) }
    var contributeTo by remember { mutableStateOf<SavingsGoal?>(null) }
    var deleteGoal by remember { mutableStateOf<SavingsGoal?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HogarGoColors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader(L10n.financeTitle, L10n.financeSubtitle)
        PillButton("＋ " + L10n.financeNewExpense, onClick = { showExpense = true })

        GoalsSection(
            goals = state.goals,
            onNew = { showGoal = true },
            onContribute = { contributeTo = it },
            onDelete = { deleteGoal = it },
        )

        HogarCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(L10n.financeThisMonth)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(L10n.financeSpent, FinanceRules.formatMoney(state.summary.total), Modifier.weight(1f))
                    StatTile(L10n.financeAverage, FinanceRules.formatMoney(state.summary.average), Modifier.weight(1f))
                }
            }
        }

        HogarCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle(L10n.financeBreakdown)
                state.summary.breakdown.forEach { BreakdownRow(it) }
            }
        }

        HogarCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle(L10n.financeRecentExpenses)
                if (state.expenses.isEmpty()) {
                    Text(L10n.financeEmptyExpenses, color = HogarGoColors.secondaryText, fontSize = 14.sp)
                } else {
                    state.expenses.forEach { ExpenseRow(it, onDelete = { viewModel.deleteExpense(it.id) }) }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showExpense) {
        AddExpenseDialog(
            onDismiss = { showExpense = false },
            onSave = { title, category, amount, date ->
                viewModel.addExpense(title, category, amount, date).also { if (it == null) showExpense = false }
            },
        )
    }
    if (showGoal) {
        GoalDialog(
            onDismiss = { showGoal = false },
            onSave = { title, target -> viewModel.addGoal(title, target).also { if (it == null) showGoal = false } },
        )
    }
    contributeTo?.let { goal ->
        ContributeDialog(
            goal = goal,
            onDismiss = { contributeTo = null },
            onSave = { amount -> viewModel.contribute(goal.id, amount).also { if (it == null) contributeTo = null } },
        )
    }
    deleteGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { deleteGoal = null },
            title = { Text(L10n.financeGoalDeleteTitle) },
            text = { Text(L10n.financeGoalDeleteMessage(goal.title)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoal(goal.id)
                    deleteGoal = null
                }) { Text(L10n.financeGoalDeleteButton, color = HogarGoColors.alertRed) }
            },
            dismissButton = { TextButton(onClick = { deleteGoal = null }) { Text(L10n.cancel) } },
        )
    }
}

@Composable
private fun SectionTitle(text: String) =
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(HogarGoColors.surfaceContainer).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, fontSize = 12.sp, color = HogarGoColors.secondaryText)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
    }
}

@Composable
private fun BreakdownRow(share: CategoryShare) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(L10n.categoryName(share.category), fontSize = 14.sp, color = HogarGoColors.primaryText)
            Text(FinanceRules.formatMoney(share.amount), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = HogarGoColors.primaryText)
        }
        Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(HogarGoColors.surfaceContainer)) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(share.fraction.coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(categoryColor.getValue(share.category)),
            )
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onDelete: () -> Unit) {
    val category = ExpenseCategory.fromId(expense.category)
    val date = runCatching { LocalDate.parse(expense.date) }.getOrNull()
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(categoryColor.getValue(category)),
            contentAlignment = Alignment.Center,
        ) { Text(categoryIcon.getValue(category), fontSize = 20.sp) }
        Column(Modifier.weight(1f)) {
            Text(expense.title, fontWeight = FontWeight.SemiBold, color = HogarGoColors.primaryText, maxLines = 1)
            Text(L10n.categoryName(category), fontSize = 12.sp, color = HogarGoColors.secondaryText)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("-" + FinanceRules.formatMoney(expense.amount), fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
            if (date != null) Text(L10n.shortDate(date), fontSize = 12.sp, color = HogarGoColors.secondaryText)
        }
        Box(Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onDelete), contentAlignment = Alignment.Center) {
            Text("🗑", fontSize = 16.sp)
        }
    }
}

@Composable
private fun GoalsSection(
    goals: List<SavingsGoal>,
    onNew: () -> Unit,
    onContribute: (SavingsGoal) -> Unit,
    onDelete: (SavingsGoal) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            SectionTitle(L10n.financeGoalsTitle)
            if (goals.size >= 2) PillButton("＋ " + L10n.financeGoalNew, onClick = onNew, container = HogarGoColors.brandOrangeContainer, content = HogarGoColors.primaryText)
        }
        when {
            goals.isEmpty() -> HogarCard {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(L10n.financeGoalEmptyTitle, color = HogarGoColors.secondaryText)
                    PillButton(L10n.financeGoalEmptyAction, onClick = onNew)
                }
            }
            goals.size == 1 -> GoalCard(goals.first(), Modifier.fillMaxWidth(), onContribute, onDelete)
            else -> Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                goals.forEach { GoalCard(it, Modifier.width(280.dp), onContribute, onDelete) }
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoal,
    modifier: Modifier,
    onContribute: (SavingsGoal) -> Unit,
    onDelete: (SavingsGoal) -> Unit,
) {
    HogarCard(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(goal.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText, maxLines = 2)
                Text(
                    "${FinanceRules.formatMoney(goal.currentAmount)} / ${FinanceRules.formatMoney(goal.targetAmount)}",
                    fontSize = 14.sp,
                    color = HogarGoColors.secondaryText,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PillButton(L10n.financeGoalContributeButton, onClick = { onContribute(goal) })
                    Box(Modifier.size(32.dp).clip(CircleShape).clickable { onDelete(goal) }, contentAlignment = Alignment.Center) {
                        Text("🗑", fontSize = 16.sp)
                    }
                }
            }
            SavingsJar(FinanceRules.goalProgress(goal.currentAmount, goal.targetAmount))
        }
    }
}

/** Frasco: tapa de 64x12 y cuerpo de 80x96 que se llena de abajo hacia arriba. */
@Composable
private fun SavingsJar(progress: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(width = 64.dp, height = 12.dp).clip(RoundedCornerShape(4.dp)).background(HogarGoColors.brandOrangeDeep))
        Box(
            Modifier
                .size(width = 80.dp, height = 96.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(HogarGoColors.surfaceContainer)
                .border(1.dp, HogarGoColors.borderSubtle, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)),
        ) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(progress)
                    .background(Brush.verticalGradient(listOf(HogarGoColors.brandOrange, HogarGoColors.brandOrangeDeep))),
            )
        }
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (String, ExpenseCategory, String, LocalDate) -> FinanceError?,
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.GROCERIES) }
    var date by remember { mutableStateOf(todayLocal()) }
    var error by remember { mutableStateOf<FinanceError?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(L10n.financeNewExpense, color = HogarGoColors.primaryText, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HogarTextField(
                    label = L10n.financeExpenseAmountLabel,
                    value = amount,
                    onValueChange = { amount = FinanceRules.filterAmountInput(it); error = null },
                    placeholder = "0.00",
                    kind = HogarFieldKind.AMOUNT,
                    error = error.takeIf { it == FinanceError.INVALID_AMOUNT }?.let { L10n.financeErrorMessage(it) },
                )
                HogarTextField(
                    label = L10n.financeExpenseNameLabel,
                    value = title,
                    onValueChange = { title = it; error = null },
                    placeholder = L10n.financeExpenseNameHint,
                    error = error.takeIf { it == FinanceError.EMPTY_TITLE }?.let { L10n.financeErrorMessage(it) },
                )
                Text(L10n.financeExpenseCategoryLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpenseCategory.entries.forEach { option ->
                        val selected = option == category
                        Row(
                            Modifier
                                .clip(CircleShape)
                                .background(if (selected) HogarGoColors.brandOrangeContainer else HogarGoColors.surfaceContainerLow)
                                .border(1.dp, if (selected) HogarGoColors.brandOrangeDeep else HogarGoColors.borderSubtle, CircleShape)
                                .clickable { category = option }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(categoryIcon.getValue(option), fontSize = 14.sp)
                            Text(L10n.categoryName(option), fontSize = 13.sp, color = HogarGoColors.primaryText)
                        }
                    }
                }
                HogarDateField(L10n.financeExpenseDateLabel, date) { date = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { error = onSave(title, category, amount, date) }) {
                Text(L10n.financeAdd, color = HogarGoColors.brandOrangeDeep, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(L10n.cancel) } },
    )
}

@Composable
private fun GoalDialog(onDismiss: () -> Unit, onSave: (String, String) -> FinanceError?) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<FinanceError?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(L10n.financeGoalCreateTitle, color = HogarGoColors.primaryText, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HogarTextField(
                    label = L10n.financeGoalNameLabel,
                    value = title,
                    onValueChange = { title = it; error = null },
                    placeholder = L10n.financeGoalNameHint,
                    error = error.takeIf { it == FinanceError.EMPTY_TITLE }?.let { L10n.financeErrorMessage(it) },
                )
                HogarTextField(
                    label = L10n.financeGoalTargetLabel,
                    value = target,
                    onValueChange = { target = FinanceRules.filterAmountInput(it); error = null },
                    placeholder = "0.00",
                    kind = HogarFieldKind.AMOUNT,
                    error = error.takeIf { it == FinanceError.INVALID_AMOUNT }?.let { L10n.financeErrorMessage(it) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { error = onSave(title, target) }) {
                Text(L10n.financeGoalCreateButton, color = HogarGoColors.brandOrangeDeep, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(L10n.cancel) } },
    )
}

@Composable
private fun ContributeDialog(goal: SavingsGoal, onDismiss: () -> Unit, onSave: (String) -> FinanceError?) {
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<FinanceError?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(L10n.financeGoalContributeTo(goal.title), color = HogarGoColors.primaryText, fontWeight = FontWeight.Bold) },
        text = {
            HogarTextField(
                label = L10n.financeGoalContributeAmountLabel,
                value = amount,
                onValueChange = { amount = FinanceRules.filterAmountInput(it); error = null },
                placeholder = "0.00",
                kind = HogarFieldKind.AMOUNT,
                error = error?.let { L10n.financeErrorMessage(it) },
            )
        },
        confirmButton = {
            TextButton(onClick = { error = onSave(amount) }) {
                Text(L10n.financeGoalContributeButton, color = HogarGoColors.brandOrangeDeep, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(L10n.cancel) } },
    )
}
