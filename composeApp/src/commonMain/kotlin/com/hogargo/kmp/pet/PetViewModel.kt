package com.hogargo.kmp.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hogargo.kmp.core.nowMillis
import com.hogargo.kmp.db.PetState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/** Una tarjeta del armario ya resuelta con nivel, monedas y estado. */
data class WardrobeCard(
    val entry: WardrobeEntry,
    val owned: Boolean,
    val equipped: Boolean,
    val locked: Boolean,
    val canBuy: Boolean,
)

data class PetUiState(
    val happiness: Float = 0.5f,
    val satiety: Float = 0.5f,
    val level: Int = 1,
    val coins: Int = 0,
    val items: List<WardrobeCard> = emptyList(),
    val equippedIds: Set<String> = emptySet(),
    val feedRemainingMs: Long = 0,
    val playRemainingMs: Long = 0,
)

class PetViewModel(
    private val repository: PetRepository,
    private val householdId: String,
    private val now: () -> Long = ::nowMillis,
) : ViewModel() {

    /** Reloj que refresca el cooldown solo, sin tener que tocar nada. */
    private val ticker = flow {
        while (true) {
            emit(now())
            delay(10_000)
        }
    }

    val state: StateFlow<PetUiState> = combine(
        repository.observeState(householdId),
        repository.observeWardrobe(householdId),
        repository.observeEarnedCoins(householdId),
        ticker,
    ) { pet, wardrobe, earned, nowMs -> build(pet, wardrobe.associateBy { it.itemId }, earned, nowMs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PetUiState())

    /** Al abrir la pantalla: crea lo que falte y aplica el decaimiento. */
    fun onOpen() = repository.refreshOnOpen(householdId)

    fun feed() = repository.care(householdId, CareKind.FEED)

    fun play() = repository.care(householdId, CareKind.PLAY)

    fun buy(itemId: String) = repository.buy(householdId, itemId)

    fun toggleEquipped(itemId: String) = repository.toggleEquipped(householdId, itemId)

    private fun build(
        pet: PetState?,
        wardrobe: Map<String, com.hogargo.kmp.db.WardrobeItem>,
        earned: Long,
        nowMs: Long,
    ): PetUiState {
        if (pet == null) return PetUiState()
        val level = PetRules.level(pet.careActions)
        val coins = PetRules.coins(earned, wardrobe.values.filter { it.owned }.map { it.itemId })
        val cards = WardrobeCatalog.map { entry ->
            val row = wardrobe[entry.id]
            val owned = row?.owned == true
            WardrobeCard(
                entry = entry,
                owned = owned,
                equipped = owned && row?.equipped == true,
                locked = level < entry.unlockLevel,
                canBuy = PetRules.canBuy(entry, level, owned, coins),
            )
        }
        return PetUiState(
            happiness = pet.happiness.toFloat().coerceIn(0f, 1f),
            satiety = pet.satiety.toFloat().coerceIn(0f, 1f),
            level = level,
            coins = coins,
            items = cards,
            equippedIds = cards.filter { it.equipped }.map { it.entry.id }.toSet(),
            feedRemainingMs = PetRules.remainingFor(pet, CareKind.FEED, nowMs),
            playRemainingMs = PetRules.remainingFor(pet, CareKind.PLAY, nowMs),
        )
    }
}
