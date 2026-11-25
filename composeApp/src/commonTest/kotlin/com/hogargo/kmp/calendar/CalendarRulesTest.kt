package com.hogargo.kmp.calendar

import com.hogargo.kmp.db.Bill
import com.hogargo.kmp.db.CalendarEvent
import com.hogargo.kmp.db.HouseholdTask
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CalendarRulesTest {
    private val today = LocalDate(2026, 11, 20)

    private fun bill(due: String, paid: Boolean = false) = Bill("b-$due-$paid", "h1", "Internet", 50.0, due, paid, 0)
    private fun event(date: String, time: String? = null, created: Long = 0) =
        CalendarEvent("e-$date-$time", "h1", "Evento", date, time, false, created)
    private fun task(date: String) = HouseholdTask("t-$date", "h1", "Tarea", true, date, 10, 0)

    @Test
    fun monthStartingOnWednesdayHasThreeLeadingBlanks() {
        // Julio de 2026 empieza en miercoles
        val cells = CalendarRules.monthCells(LocalDate(2026, 7, 1))
        assertEquals(3, cells.takeWhile { it == null }.size)
        assertEquals(LocalDate(2026, 7, 1), cells[3])
        assertEquals(0, cells.size % 7)
    }

    @Test
    fun thirtyDayMonthStartingOnSundayFillsFiveRows() {
        // Noviembre de 2026 empieza en domingo y tiene 30 dias
        val cells = CalendarRules.monthCells(LocalDate(2026, 11, 1))
        assertEquals(LocalDate(2026, 11, 1), cells.first())
        assertEquals(30, cells.count { it != null })
        assertEquals(35, cells.size)
        assertEquals(5, cells.count { it == null })
    }

    @Test
    fun monthOffsetsCrossYearBoundaries() {
        assertEquals(LocalDate(2027, 1, 1), CalendarRules.monthStart(today, 2))
        assertEquals(LocalDate(2026, 10, 1), CalendarRules.monthStart(today, -1))
        assertEquals(LocalDate(2025, 12, 1), CalendarRules.monthStart(LocalDate(2026, 2, 10), -2))
        assertEquals(29, CalendarRules.daysInMonth(LocalDate(2028, 2, 1)))
    }

    @Test
    fun dotsFollowTheOrderBillEventTask() {
        val dots = CalendarRules.dotsFor(
            LocalDate(2026, 11, 25),
            events = listOf(event("2026-11-25")),
            bills = listOf(bill("2026-11-25")),
            tasks = listOf(task("2026-11-25")),
            today = today,
        )
        assertEquals(listOf(DayDot.BILL, DayDot.EVENT, DayDot.TASK), dots)
    }

    @Test
    fun unpaidOverdueBillIsRedAndPaidBillHasNoDot() {
        val overdue = CalendarRules.dotsFor(LocalDate(2026, 11, 10), emptyList(), listOf(bill("2026-11-10")), emptyList(), today)
        assertEquals(listOf(DayDot.BILL_OVERDUE), overdue)
        val paid = CalendarRules.dotsFor(LocalDate(2026, 11, 10), emptyList(), listOf(bill("2026-11-10", paid = true)), emptyList(), today)
        assertEquals(emptyList(), paid)
    }

    @Test
    fun upcomingEventsStartTodayAndAreSorted() {
        val list = listOf(
            event("2026-11-19", "09:00"),
            event("2026-11-21", "08:00"),
            event("2026-11-20", "10:00"),
            event("2026-11-20", "08:30"),
        )
        val upcoming = CalendarRules.upcomingEvents(list, today)
        assertEquals(listOf("08:30", "10:00", "08:00"), upcoming.map { it.timeLabel })
    }

    @Test
    fun dueNowOnlyForUnpaidBillsThatAreDueOrLate() {
        assertEquals(true, CalendarRules.isDueNow(bill("2026-11-20"), today))
        assertEquals(true, CalendarRules.isDueNow(bill("2026-11-01"), today))
        assertEquals(false, CalendarRules.isDueNow(bill("2026-11-21"), today))
        assertEquals(false, CalendarRules.isDueNow(bill("2026-11-20", paid = true), today))
    }

    @Test
    fun timeIsNormalizedTo24Hours() {
        assertEquals("10:00", CalendarRules.normalizeTime("10:00"))
        assertEquals("09:30", CalendarRules.normalizeTime("9:30"))
        assertNull(CalendarRules.normalizeTime("24:00"))
        assertNull(CalendarRules.normalizeTime("10:60"))
        assertNull(CalendarRules.normalizeTime("10"))
        assertNull(CalendarRules.normalizeTime("ab:cd"))
    }
}
