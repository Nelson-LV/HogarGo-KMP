package com.hogargo.kmp.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hogargo.kmp.calendar.CalendarError
import com.hogargo.kmp.calendar.CalendarRules
import com.hogargo.kmp.calendar.CalendarViewModel
import com.hogargo.kmp.calendar.DayCell
import com.hogargo.kmp.calendar.DayDot
import com.hogargo.kmp.db.Bill
import com.hogargo.kmp.db.CalendarEvent
import com.hogargo.kmp.finance.FinanceRules
import com.hogargo.kmp.household.ui.HogarCard
import com.hogargo.kmp.household.ui.HogarFieldKind
import com.hogargo.kmp.household.ui.HogarGoColors
import com.hogargo.kmp.household.ui.HogarTextField
import com.hogargo.kmp.l10n.L10n
import com.hogargo.kmp.l10n.calendarAddBill
import com.hogargo.kmp.l10n.calendarAddEvent
import com.hogargo.kmp.l10n.calendarBillCreate
import com.hogargo.kmp.l10n.calendarBillDueDateLabel
import com.hogargo.kmp.l10n.calendarBillNameHint
import com.hogargo.kmp.l10n.calendarBillNameLabel
import com.hogargo.kmp.l10n.calendarBillsDue
import com.hogargo.kmp.l10n.calendarBillsEmpty
import com.hogargo.kmp.l10n.calendarDayBills
import com.hogargo.kmp.l10n.calendarDayEmpty
import com.hogargo.kmp.l10n.calendarDayEvents
import com.hogargo.kmp.l10n.calendarDayTasks
import com.hogargo.kmp.l10n.calendarDeleteBill
import com.hogargo.kmp.l10n.calendarDeleteEvent
import com.hogargo.kmp.l10n.calendarDueToday
import com.hogargo.kmp.l10n.calendarErrorMessage
import com.hogargo.kmp.l10n.calendarEventCreate
import com.hogargo.kmp.l10n.calendarEventDateLabel
import com.hogargo.kmp.l10n.calendarEventNameHint
import com.hogargo.kmp.l10n.calendarEventNameLabel
import com.hogargo.kmp.l10n.calendarEventTimeLabel
import com.hogargo.kmp.l10n.calendarEventsEmpty
import com.hogargo.kmp.l10n.calendarLegendBills
import com.hogargo.kmp.l10n.calendarLegendEvents
import com.hogargo.kmp.l10n.calendarLegendTasks
import com.hogargo.kmp.l10n.calendarMonthYear
import com.hogargo.kmp.l10n.calendarNextMonth
import com.hogargo.kmp.l10n.calendarPrevMonth
import com.hogargo.kmp.l10n.calendarSubtitle
import com.hogargo.kmp.l10n.calendarTitle
import com.hogargo.kmp.l10n.calendarUpcoming
import com.hogargo.kmp.l10n.daysOfWeekShort
import com.hogargo.kmp.l10n.financeExpenseAmountLabel
import com.hogargo.kmp.l10n.longDate
import com.hogargo.kmp.l10n.shortDate
import com.hogargo.kmp.ui.common.HogarDateField
import com.hogargo.kmp.ui.common.PillButton
import com.hogargo.kmp.ui.common.ScreenHeader
import kotlinx.datetime.LocalDate

private val EventBlue = Color(0xFF3B82C4)
private val TaskGreen = Color(0xFF4C9A5F)

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val state by viewModel.state.collectAsState()
    var eventDialogDate by remember { mutableStateOf<LocalDate?>(null) }
    var showBillDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HogarGoColors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ScreenHeader(L10n.calendarTitle, L10n.calendarSubtitle, centered = true)
        }

        // Mes
        HogarCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    RoundButton("‹", L10n.calendarPrevMonth, viewModel::previousMonth)
                    Text(
                        L10n.calendarMonthYear(state.monthStart.monthNumber, state.monthStart.year),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = HogarGoColors.primaryText,
                    )
                    RoundButton("›", L10n.calendarNextMonth, viewModel::nextMonth)
                }
                Row(Modifier.fillMaxWidth()) {
                    L10n.daysOfWeekShort.forEach {
                        Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = HogarGoColors.secondaryText)
                    }
                }
                state.cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { cell -> DayBox(cell, Modifier.weight(1f), onClick = { viewModel.select(it) }) }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    LegendItem(EventBlue, L10n.calendarLegendEvents)
                    LegendItem(HogarGoColors.brandOrangeDeep, L10n.calendarLegendBills)
                    LegendItem(TaskGreen, L10n.calendarLegendTasks)
                }
            }
        }

        // Detalle del dia
        HogarCard(background = HogarGoColors.surfaceContainer) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(L10n.longDate(state.selected), Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
                    RoundButton("＋", L10n.calendarAddEvent) { eventDialogDate = state.selected }
                }
                if (state.selectedEvents.isEmpty() && state.selectedBills.isEmpty() && state.selectedTasks.isEmpty()) {
                    Text(L10n.calendarDayEmpty, fontSize = 14.sp, color = HogarGoColors.secondaryText)
                }
                if (state.selectedEvents.isNotEmpty()) {
                    SubTitle(L10n.calendarDayEvents)
                    state.selectedEvents.forEach { event ->
                        EventRow(event, subtitle = event.timeLabel, onDone = { viewModel.setEventDone(event.id, it) }, onDelete = { viewModel.deleteEvent(event.id) })
                    }
                }
                if (state.selectedBills.isNotEmpty()) {
                    SubTitle(L10n.calendarDayBills)
                    state.selectedBills.forEach { bill ->
                        BillRow(bill, state.today, showDate = true, onPaid = { viewModel.setBillPaid(bill.id, it) }, onDelete = { viewModel.deleteBill(bill.id) })
                    }
                }
                if (state.selectedTasks.isNotEmpty()) {
                    SubTitle(L10n.calendarDayTasks)
                    state.selectedTasks.forEach { Text("• " + it.title, fontSize = 14.sp, color = HogarGoColors.primaryText) }
                }
            }
        }

        // Proximos eventos
        HogarCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(L10n.calendarUpcoming, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
                if (state.upcoming.isEmpty()) {
                    Text(L10n.calendarEventsEmpty, fontSize = 14.sp, color = HogarGoColors.secondaryText)
                } else {
                    state.upcoming.forEach { event ->
                        val date = LocalDate.parse(event.date)
                        val sub = L10n.shortDate(date) + (event.timeLabel?.let { " • $it" } ?: "")
                        EventRow(event, subtitle = sub, onDone = { viewModel.setEventDone(event.id, it) }, onDelete = { viewModel.deleteEvent(event.id) })
                    }
                }
            }
        }

        // Facturas
        HogarCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(L10n.calendarBillsDue, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
                    RoundButton("＋", L10n.calendarAddBill) { showBillDialog = true }
                }
                if (state.bills.isEmpty()) {
                    Text(L10n.calendarBillsEmpty, fontSize = 14.sp, color = HogarGoColors.secondaryText)
                } else {
                    state.bills.forEach { bill ->
                        BillRow(bill, state.today, showDate = false, onPaid = { viewModel.setBillPaid(bill.id, it) }, onDelete = { viewModel.deleteBill(bill.id) })
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    eventDialogDate?.let { initial ->
        AddEventDialog(
            initialDate = initial,
            onDismiss = { eventDialogDate = null },
            onSave = { title, date, time -> viewModel.addEvent(title, date, time).also { if (it == null) eventDialogDate = null } },
        )
    }
    if (showBillDialog) {
        AddBillDialog(
            initialDate = state.today,
            onDismiss = { showBillDialog = false },
            onSave = { title, amount, due -> viewModel.addBill(title, amount, due).also { if (it == null) showBillDialog = false } },
        )
    }
}

@Composable
private fun RoundButton(symbol: String, description: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(HogarGoColors.brandOrangeContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // description se usa como etiqueta de accesibilidad en la app nativa; aqui el simbolo basta.
        Text(symbol, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
    }
}

@Composable
private fun SubTitle(text: String) =
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.secondaryText)

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 12.sp, color = HogarGoColors.secondaryText)
    }
}

@Composable
private fun DayBox(cell: DayCell?, modifier: Modifier, onClick: (LocalDate) -> Unit) {
    Box(modifier.aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
        if (cell == null) return@Box
        Column(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(if (cell.isSelected) HogarGoColors.brandOrangeContainer else Color.Transparent)
                .then(if (cell.isToday) Modifier.border(2.dp, HogarGoColors.brandOrangeDeep, RoundedCornerShape(12.dp)) else Modifier)
                .clickable { onClick(cell.date) },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                cell.date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (cell.isToday) FontWeight.Bold else FontWeight.Normal,
                color = HogarGoColors.primaryText,
            )
            Row(Modifier.height(8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                cell.dots.forEach { dot ->
                    val color = when (dot) {
                        DayDot.BILL -> HogarGoColors.brandOrangeDeep
                        DayDot.BILL_OVERDUE -> HogarGoColors.alertRed
                        DayDot.EVENT -> EventBlue
                        DayDot.TASK -> TaskGreen
                    }
                    Box(Modifier.size(5.dp).clip(CircleShape).background(color))
                }
            }
        }
    }
}

@Composable
private fun Check(checked: Boolean, onChange: (Boolean) -> Unit) = Checkbox(
    checked = checked,
    onCheckedChange = onChange,
    colors = CheckboxDefaults.colors(checkedColor = HogarGoColors.brandOrangeDeep),
)

@Composable
private fun EventRow(event: CalendarEvent, subtitle: String?, onDone: (Boolean) -> Unit, onDelete: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Check(event.done, onDone)
        Column(Modifier.weight(1f)) {
            Text(
                event.title,
                fontWeight = FontWeight.SemiBold,
                color = HogarGoColors.primaryText,
                textDecoration = if (event.done) TextDecoration.LineThrough else null,
            )
            if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = HogarGoColors.secondaryText)
        }
        Box(Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onDelete), contentAlignment = Alignment.Center) {
            Text("🗑", fontSize = 16.sp)
        }
    }
}

@Composable
private fun BillRow(bill: Bill, today: LocalDate, showDate: Boolean, onPaid: (Boolean) -> Unit, onDelete: () -> Unit) {
    val dueNow = CalendarRules.isDueNow(bill, today)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Check(bill.paid, onPaid)
        Column(Modifier.weight(1f)) {
            Text(
                bill.title,
                fontWeight = FontWeight.SemiBold,
                color = HogarGoColors.primaryText,
                textDecoration = if (bill.paid) TextDecoration.LineThrough else null,
            )
            if (!showDate) {
                if (dueNow) Text(L10n.calendarDueToday, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HogarGoColors.alertRed)
                else Text(L10n.shortDate(LocalDate.parse(bill.dueDate)), fontSize = 12.sp, color = HogarGoColors.secondaryText)
            }
        }
        Text(FinanceRules.formatMoney(bill.amount), fontWeight = FontWeight.Bold, color = HogarGoColors.primaryText)
        Box(Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onDelete), contentAlignment = Alignment.Center) {
            Text("🗑", fontSize = 16.sp)
        }
    }
}

@Composable
private fun AddEventDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (String, LocalDate, String) -> CalendarError?,
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf("10:00") }
    var error by remember { mutableStateOf<CalendarError?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(L10n.calendarAddEvent, color = HogarGoColors.primaryText, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HogarTextField(
                    label = L10n.calendarEventNameLabel,
                    value = title,
                    onValueChange = { title = it; error = null },
                    placeholder = L10n.calendarEventNameHint,
                    error = error.takeIf { it == CalendarError.EMPTY_TITLE }?.let { L10n.calendarErrorMessage(it) },
                )
                HogarDateField(L10n.calendarEventDateLabel, date) { date = it }
                HogarTextField(
                    label = L10n.calendarEventTimeLabel,
                    value = time,
                    onValueChange = { time = it.take(5); error = null },
                    placeholder = "10:00",
                    kind = HogarFieldKind.RECOVERY_USER,
                    error = error.takeIf { it == CalendarError.INVALID_TIME }?.let { L10n.calendarErrorMessage(it) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { error = onSave(title, date, time) }, enabled = title.isNotBlank()) {
                Text(L10n.calendarEventCreate, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(L10n.cancel) } },
    )
}

@Composable
private fun AddBillDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (String, String, LocalDate) -> CalendarError?,
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var due by remember { mutableStateOf(initialDate) }
    var error by remember { mutableStateOf<CalendarError?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(L10n.calendarAddBill, color = HogarGoColors.primaryText, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HogarTextField(
                    label = L10n.calendarBillNameLabel,
                    value = title,
                    onValueChange = { title = it; error = null },
                    placeholder = L10n.calendarBillNameHint,
                    error = error.takeIf { it == CalendarError.EMPTY_TITLE }?.let { L10n.calendarErrorMessage(it) },
                )
                HogarTextField(
                    label = L10n.financeExpenseAmountLabel,
                    value = amount,
                    onValueChange = { amount = FinanceRules.filterAmountInput(it); error = null },
                    placeholder = "0.00",
                    kind = HogarFieldKind.AMOUNT,
                    error = error.takeIf { it == CalendarError.INVALID_AMOUNT }?.let { L10n.calendarErrorMessage(it) },
                )
                HogarDateField(L10n.calendarBillDueDateLabel, due) { due = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { error = onSave(title, amount, due) }) {
                Text(L10n.calendarBillCreate, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(L10n.cancel) } },
    )
}
