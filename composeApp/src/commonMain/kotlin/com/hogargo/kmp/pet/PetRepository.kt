package com.hogargo.kmp.pet

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.hogargo.kmp.core.nowMillis
import com.hogargo.kmp.db.HogarGoDatabase
import com.hogargo.kmp.db.PetState
import com.hogargo.kmp.db.WardrobeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Estado de Zori y su armario. Las monedas se calculan siempre; no se guardan. */
class PetRepository(
    private val db: HogarGoDatabase,
    private val now: () -> Long = ::nowMillis,
) {
    private val q = db.petQueries

    fun observeState(householdId: String): Flow<PetState?> =
        q.petState(householdId).asFlow().mapToOneOrNull(Dispatchers.Default)

    fun observeWardrobe(householdId: String): Flow<List<WardrobeItem>> =
        q.wardrobe(householdId).asFlow().mapToList(Dispatchers.Default)

    fun observeEarnedCoins(householdId: String): Flow<Long> =
        combine(
            db.tasksQueries.earnedCoins(householdId).asFlow().mapToOne(Dispatchers.Default),
            db.taskQueries.earnedCoins(householdId).asFlow().mapToOne(Dispatchers.Default),
        ) { real, legacy -> real + legacy }

    fun state(householdId: String): PetState? = q.petState(householdId).executeAsOneOrNull()

    fun wardrobe(householdId: String): List<WardrobeItem> = q.wardrobe(householdId).executeAsList()

    fun coins(householdId: String): Int {
        val totalEarned = db.tasksQueries.earnedCoins(householdId).executeAsOne() +
            db.taskQueries.earnedCoins(householdId).executeAsOne()
        return PetRules.coins(
            totalEarned,
            wardrobe(householdId).filter { it.owned }.map { it.itemId },
        )
    }

    /** Bonificación de ánimo y cuidado a Zori cuando se completa una tarea del hogar. */
    fun boostHappinessOnTaskCompleted(householdId: String) = db.transaction {
        val current = ensure(householdId)
        val updated = current.copy(
            happiness = kotlin.math.min(1.0, current.happiness + 0.15),
            careActions = current.careActions + 1,
        )
        save(updated)
    }

    fun level(householdId: String): Int = PetRules.level(state(householdId)?.careActions ?: 0)

    /** Al abrir la pantalla: crea el estado y el armario si faltan y aplica el decaimiento. */
    fun refreshOnOpen(householdId: String) = db.transaction {
        val current = ensure(householdId)
        val decayed = PetRules.applyDecay(current, now())
        if (decayed != current) save(decayed)
    }

    fun care(householdId: String, kind: CareKind) = db.transaction {
        val current = ensure(householdId)
        val updated = PetRules.care(current, kind, now())
        if (updated != current) save(updated)
    }

    /** Falla (false) si no existe, nivel insuficiente, ya es tuyo o no alcanzan las monedas. */
    fun buy(householdId: String, itemId: String): Boolean = db.transactionWithResult {
        val entry = WardrobeCatalog.firstOrNull { it.id == itemId } ?: return@transactionWithResult false
        ensure(householdId)
        val row = wardrobe(householdId).firstOrNull { it.itemId == itemId } ?: return@transactionWithResult false
        if (!PetRules.canBuy(entry, level(householdId), row.owned, coins(householdId))) return@transactionWithResult false
        q.updateWardrobeItem(owned = true, equipped = false, householdId = householdId, itemId = itemId)
        true
    }

    /** Pone o quita un objeto ya comprado (se pueden llevar varios a la vez). */
    fun toggleEquipped(householdId: String, itemId: String): Boolean = db.transactionWithResult {
        val entry = WardrobeCatalog.firstOrNull { it.id == itemId } ?: return@transactionWithResult false
        ensure(householdId)
        val row = wardrobe(householdId).firstOrNull { it.itemId == itemId } ?: return@transactionWithResult false
        if (!PetRules.canToggle(entry, level(householdId), row.owned)) return@transactionWithResult false
        q.updateWardrobeItem(owned = true, equipped = !row.equipped, householdId = householdId, itemId = itemId)
        true
    }

    fun deleteAll(householdId: String) {
        q.deletePetState(householdId)
        q.deleteWardrobe(householdId)
    }

    private fun ensure(householdId: String): PetState {
        WardrobeCatalog.forEach { q.seedWardrobeItem(householdId, it.id) }
        return state(householdId) ?: PetRules.initial(householdId, now()).also(::save)
    }

    private fun save(s: PetState) =
        q.savePetState(s.householdId, s.happiness, s.satiety, s.careActions, s.lastFeedAt, s.lastPlayAt, s.lastDecayAt)
}
