package com.hogargo.kmp.finance

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hogargo.kmp.db.HogarGoDatabase
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FinanceRepositoryTest {
    private lateinit var repo: FinanceRepository
    private var clock = 1_000L

    @BeforeTest
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        HogarGoDatabase.Schema.create(driver)
        repo = FinanceRepository(HogarGoDatabase(driver), now = { clock++ }, today = { LocalDate(2026, 11, 20) })
    }

    @Test
    fun expensesAreOrderedByDateThenNewestFirst() {
        repo.addExpense("h1", "viejo", ExpenseCategory.OTHER, "1", LocalDate(2026, 11, 1))
        repo.addExpense("h1", "primero hoy", ExpenseCategory.OTHER, "1")
        repo.addExpense("h1", "segundo hoy", ExpenseCategory.OTHER, "1")
        assertEquals(listOf("segundo hoy", "primero hoy", "viejo"), repo.expenses("h1").map { it.title })
    }

    @Test
    fun invalidExpensesAreRejectedAndNotSaved() {
        assertEquals(FinanceError.EMPTY_TITLE, assertFailsWith<FinanceException> {
            repo.addExpense("h1", " ", ExpenseCategory.OTHER, "5")
        }.error)
        assertEquals(FinanceError.INVALID_AMOUNT, assertFailsWith<FinanceException> {
            repo.addExpense("h1", "Pan", ExpenseCategory.OTHER, "0")
        }.error)
        assertTrue(repo.expenses("h1").isEmpty())
    }

    @Test
    fun dataIsIsolatedPerHousehold() {
        repo.addExpense("h1", "de uno", ExpenseCategory.PET, "5")
        repo.addGoal("h1", "Viaje", "100")
        assertTrue(repo.expenses("h2").isEmpty())
        assertTrue(repo.goals("h2").isEmpty())
        repo.deleteExpense("h2", repo.expenses("h1").first().id)
        assertEquals(1, repo.expenses("h1").size)
    }

    @Test
    fun contributionsCanExceedTheTarget() {
        repo.addGoal("h1", "Meta", "100")
        val id = repo.goals("h1").single().id
        repo.contribute("h1", id, "30")
        repo.contribute("h1", id, "90")
        val goal = repo.goals("h1").single()
        assertEquals(120.0, goal.currentAmount)
        assertEquals(1f, FinanceRules.goalProgress(goal.currentAmount, goal.targetAmount))
    }

    @Test
    fun goalValidation() {
        assertEquals(FinanceError.EMPTY_TITLE, assertFailsWith<FinanceException> { repo.addGoal("h1", "", "10") }.error)
        assertEquals(FinanceError.INVALID_AMOUNT, assertFailsWith<FinanceException> { repo.addGoal("h1", "A", "0") }.error)
    }

    @Test
    fun deleteAllRemovesOnlyThatHousehold() {
        repo.addExpense("h1", "a", ExpenseCategory.OTHER, "1")
        repo.addGoal("h1", "g", "10")
        repo.addExpense("h2", "b", ExpenseCategory.OTHER, "1")
        repo.deleteAll("h1")
        assertTrue(repo.expenses("h1").isEmpty() && repo.goals("h1").isEmpty())
        assertEquals(1, repo.expenses("h2").size)
    }
}
