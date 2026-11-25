package com.hogargo.kmp.pet

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hogargo.kmp.db.HogarGoDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PetRepositoryTest {
    private lateinit var db: HogarGoDatabase
    private lateinit var repo: PetRepository
    private var clock = 1_000_000L

    @BeforeTest
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        HogarGoDatabase.Schema.create(driver)
        db = HogarGoDatabase(driver)
        repo = PetRepository(db) { clock }
    }

    private fun completedTask(householdId: String, id: String, reward: Long = 10) =
        db.taskQueries.insertTask(id, householdId, "tarea $id", true, "2026-11-20", reward, 0)

    private fun careUntilLevel(householdId: String, actions: Int) {
        repeat(actions) {
            clock += PetRules.COOLDOWN_MS + 1
            repo.care(householdId, if (it % 2 == 0) CareKind.FEED else CareKind.PLAY)
        }
    }

    @Test
    fun openingCreatesStateAndWardrobe() {
        repo.refreshOnOpen("h1")
        val state = repo.state("h1")!!
        assertEquals(0.5, state.happiness)
        assertEquals(4, repo.wardrobe("h1").size)
        assertTrue(repo.wardrobe("h1").none { it.owned || it.equipped })
    }

    @Test
    fun careRaisesLevelEveryFiveActions() {
        repo.refreshOnOpen("h1")
        careUntilLevel("h1", 5)
        assertEquals(2, repo.level("h1"))
    }

    @Test
    fun coinsComeFromCompletedTasksMinusPurchases() {
        repo.refreshOnOpen("h1")
        completedTask("h1", "a"); completedTask("h1", "b"); completedTask("h1", "c")
        assertEquals(30, repo.coins("h1"))

        assertTrue(repo.buy("h1", "scarf"))
        assertEquals(10, repo.coins("h1"))
        assertFalse(repo.buy("h1", "ball"), "30 monedas no alcanzan con saldo 10")
        assertFalse(repo.buy("h1", "scarf"), "no se compra dos veces")
    }

    @Test
    fun glassesNeedLevelEvenWithEnoughCoins() {
        repo.refreshOnOpen("h1")
        completedTask("h1", "rico", reward = 500)
        assertFalse(repo.buy("h1", "glasses"))
        careUntilLevel("h1", 10)
        assertEquals(3, repo.level("h1"))
        assertTrue(repo.buy("h1", "glasses"))
    }

    @Test
    fun equippingRequiresOwningAndToggles() {
        repo.refreshOnOpen("h1")
        assertFalse(repo.toggleEquipped("h1", "scarf"))
        completedTask("h1", "a", reward = 50)
        repo.buy("h1", "scarf")
        assertTrue(repo.toggleEquipped("h1", "scarf"))
        assertTrue(repo.wardrobe("h1").first { it.itemId == "scarf" }.equipped)
        assertTrue(repo.toggleEquipped("h1", "scarf"))
        assertFalse(repo.wardrobe("h1").first { it.itemId == "scarf" }.equipped)
    }

    @Test
    fun householdsDoNotShareCoinsOrPets() {
        repo.refreshOnOpen("h1")
        repo.refreshOnOpen("h2")
        completedTask("h1", "a", reward = 100)
        assertEquals(0, repo.coins("h2"))
        careUntilLevel("h1", 5)
        assertEquals(1, repo.level("h2"))
    }

    @Test
    fun decayIsAppliedWhenReopening() {
        repo.refreshOnOpen("h1")
        clock += 5 * 3_600_000L
        repo.refreshOnOpen("h1")
        assertEquals(0.40, repo.state("h1")!!.happiness, 1e-9)
    }
}
