package com.hogargo.kmp.household

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hogargo.kmp.calendar.CalendarRepository
import com.hogargo.kmp.db.HogarGoDatabase
import com.hogargo.kmp.finance.ExpenseCategory
import com.hogargo.kmp.finance.FinanceRepository
import com.hogargo.kmp.household.data.HouseholdRepository
import com.hogargo.kmp.household.data.HouseholdRepository.LeaveOutcome
import com.hogargo.kmp.pet.PetRepository
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Cuando sale la ultima persona del hogar, se borran tambien los datos de cada modulo. */
class CascadeDeleteTest {
    private val db: HogarGoDatabase = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        .also { HogarGoDatabase.Schema.create(it) }
        .let { HogarGoDatabase(it) }
    private val households = HouseholdRepository(db)
    private val finance = FinanceRepository(db)
    private val pet = PetRepository(db)
    private val calendar = CalendarRepository(db)
    private val day = LocalDate(2026, 11, 20)

    private fun fill(householdId: String) {
        finance.addExpense(householdId, "Pan", ExpenseCategory.GROCERIES, "5", day)
        finance.addGoal(householdId, "Viaje", "100")
        pet.refreshOnOpen(householdId)
        calendar.addEvent(householdId, "Cita", day, "10:00")
        calendar.addBill(householdId, "Luz", "40", day)
        db.taskQueries.insertTask("t-$householdId", householdId, "Tarea", true, day.toString(), 10, 0)
    }

    @Test
    fun leavingAsLastMemberDeletesEveryModulesData() {
        val ana = households.createHousehold("Ana", "Casa", "ana_rec", "HGR789")
        val other = households.createHousehold("Beto", "Otra", "beto_rec", "HGR111")
        fill(ana.householdId)
        fill(other.householdId)

        assertEquals(LeaveOutcome.HOUSEHOLD_DELETED, households.leaveHousehold(ana.id))

        val id = ana.householdId
        assertTrue(finance.expenses(id).isEmpty() && finance.goals(id).isEmpty())
        assertNull(pet.state(id))
        assertTrue(pet.wardrobe(id).isEmpty())
        assertTrue(calendar.events(id).isEmpty() && calendar.bills(id).isEmpty())
        assertTrue(db.taskQueries.completedTasks(id).executeAsList().isEmpty())

        // El otro hogar conserva todo
        val keep = other.householdId
        assertEquals(1, finance.expenses(keep).size)
        assertEquals(1, calendar.events(keep).size)
        assertEquals(4, pet.wardrobe(keep).size)
    }
}
