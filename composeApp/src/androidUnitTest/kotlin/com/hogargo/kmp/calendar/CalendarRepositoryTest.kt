package com.hogargo.kmp.calendar

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hogargo.kmp.db.HogarGoDatabase
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CalendarRepositoryTest {
    private lateinit var db: HogarGoDatabase
    private lateinit var repo: CalendarRepository
    private var clock = 1_000L
    private val day = LocalDate(2026, 11, 20)

    @BeforeTest
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        HogarGoDatabase.Schema.create(driver)
        db = HogarGoDatabase(driver)
        repo = CalendarRepository(db) { clock++ }
    }

    @Test
    fun eventsKeepTheirTimeAndAllowNoTime() {
        repo.addEvent("h1", "  Cita médica ", day, "9:30")
        repo.addEvent("h1", "Sin hora", day, "")
        val events = repo.events("h1")
        assertEquals(setOf("09:30", null), events.map { it.timeLabel }.toSet())
        assertEquals("Cita médica", events.first { it.timeLabel != null }.title)
    }

    @Test
    fun invalidEventsAreRejected() {
        assertEquals(CalendarError.EMPTY_TITLE, assertFailsWith<CalendarException> { repo.addEvent("h1", " ", day, "10:00") }.error)
        assertEquals(CalendarError.INVALID_TIME, assertFailsWith<CalendarException> { repo.addEvent("h1", "Cita", day, "25:00") }.error)
        assertTrue(repo.events("h1").isEmpty())
    }

    @Test
    fun billsAreSortedByDueDateAndCanBePaid() {
        repo.addBill("h1", "Luz", "80,5", LocalDate(2026, 12, 5))
        repo.addBill("h1", "Internet", "50", LocalDate(2026, 11, 28))
        val bills = repo.bills("h1")
        assertEquals(listOf("Internet", "Luz"), bills.map { it.title })
        assertEquals(80.5, bills[1].amount)

        repo.setBillPaid("h1", bills[0].id, true)
        assertTrue(repo.bills("h1")[0].paid)
    }

    @Test
    fun invalidBillsAreRejected() {
        assertEquals(CalendarError.EMPTY_TITLE, assertFailsWith<CalendarException> { repo.addBill("h1", "", "5", day) }.error)
        assertEquals(CalendarError.INVALID_AMOUNT, assertFailsWith<CalendarException> { repo.addBill("h1", "Luz", "0", day) }.error)
    }

    @Test
    fun doneFlagTogglesAndDeleteRemoves() {
        repo.addEvent("h1", "Cumple", day, "10:00")
        val id = repo.events("h1").single().id
        repo.setEventDone("h1", id, true)
        assertTrue(repo.events("h1").single().done)
        repo.deleteEvent("h1", id)
        assertTrue(repo.events("h1").isEmpty())
    }

    @Test
    fun householdsAreIsolated() {
        repo.addEvent("h1", "Solo h1", day, "10:00")
        repo.addBill("h1", "Solo h1", "5", day)
        assertTrue(repo.events("h2").isEmpty() && repo.bills("h2").isEmpty())
        repo.deleteEvent("h2", repo.events("h1").single().id)
        assertEquals(1, repo.events("h1").size)
        repo.deleteAll("h1")
        assertTrue(repo.events("h1").isEmpty() && repo.bills("h1").isEmpty())
    }

    @Test
    fun tasksAreReadFromTheTasksModuleTable() {
        db.tasksQueries.insertTask("t1", "h1", "Lavar", "General", "m1", "Ana", false, 10, "2026-11-20", "18:00", true, 1_000L, 0)
        db.tasksQueries.insertTask("t2", "h1", "Pendiente", "General", "m1", "Ana", false, 10, "2026-11-20", "18:00", false, null, 0)
        db.tasksQueries.insertTask("t3", "h2", "Otro hogar", "General", "m1", "Ana", false, 10, "2026-11-20", "18:00", true, 1_000L, 0)
        val tasks = db.tasksQueries.tasksByHousehold("h1").executeAsList()
        assertEquals(setOf("t1", "t2"), tasks.map { it.id }.toSet())
        assertEquals(listOf("t2"), CalendarRules.pendingTasksFor(tasks, "m1").map { it.id })
    }
}
