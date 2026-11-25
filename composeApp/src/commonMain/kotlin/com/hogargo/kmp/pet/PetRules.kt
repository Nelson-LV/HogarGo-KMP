package com.hogargo.kmp.pet

import com.hogargo.kmp.db.PetState
import kotlin.math.max
import kotlin.math.min

/** Objeto del armario de Zori. */
data class WardrobeEntry(val id: String, val emoji: String, val unlockLevel: Int, val price: Int)

val WardrobeCatalog = listOf(
    WardrobeEntry("scarf", "🧣", unlockLevel = 1, price = 20),
    WardrobeEntry("ball", "⚽", unlockLevel = 1, price = 30),
    WardrobeEntry("glasses", "🕶️", unlockLevel = 3, price = 60),
    WardrobeEntry("skateboard", "🛹", unlockLevel = 5, price = 100),
)

enum class CareKind { FEED, PLAY }

/** Reglas de cuidado de Zori, puras (reciben "ahora" como parametro). */
object PetRules {
    const val DECAY_INTERVAL_MS = 7_200_000L // 2 horas
    const val DECAY_AMOUNT = 0.05
    const val CARE_GAIN = 0.15
    const val COOLDOWN_MS = 900_000L // 15 minutos
    const val ACTIONS_PER_LEVEL = 5
    const val INITIAL_LEVEL_VALUE = 0.5

    fun initial(householdId: String, now: Long) =
        PetState(householdId, INITIAL_LEVEL_VALUE, INITIAL_LEVEL_VALUE, 0, 0, 0, now)

    fun level(careActions: Long): Int = 1 + (careActions / ACTIONS_PER_LEVEL).toInt()

    /**
     * Baja felicidad y saciedad 0.05 por cada intervalo de 2 h completo.
     * lastDecayAt avanza en multiplos exactos para no perder el resto de tiempo.
     */
    fun applyDecay(state: PetState, now: Long): PetState {
        val chunks = (now - state.lastDecayAt) / DECAY_INTERVAL_MS
        if (chunks <= 0) return state
        return state.copy(
            happiness = max(0.0, state.happiness - DECAY_AMOUNT * chunks),
            satiety = max(0.0, state.satiety - DECAY_AMOUNT * chunks),
            lastDecayAt = state.lastDecayAt + chunks * DECAY_INTERVAL_MS,
        )
    }

    /** Milisegundos que faltan para poder repetir la accion (0 si ya se puede). */
    fun cooldownRemaining(lastAt: Long, now: Long): Long = max(0L, COOLDOWN_MS - (now - lastAt))

    fun remainingFor(state: PetState, kind: CareKind, now: Long): Long =
        cooldownRemaining(if (kind == CareKind.FEED) state.lastFeedAt else state.lastPlayAt, now)

    /** "Disponible en N min": N se redondea hacia arriba. */
    fun minutesLabelValue(remainingMs: Long): Long = remainingMs / 60_000 + 1

    /** Alimentar o jugar: primero el decaimiento; durante el cooldown no cambia nada mas. */
    fun care(state: PetState, kind: CareKind, now: Long): PetState {
        val decayed = applyDecay(state, now)
        if (remainingFor(decayed, kind, now) > 0) return decayed
        return when (kind) {
            CareKind.FEED -> decayed.copy(
                satiety = min(1.0, decayed.satiety + CARE_GAIN),
                lastFeedAt = now,
                careActions = decayed.careActions + 1,
            )
            CareKind.PLAY -> decayed.copy(
                happiness = min(1.0, decayed.happiness + CARE_GAIN),
                lastPlayAt = now,
                careActions = decayed.careActions + 1,
            )
        }
    }

    /** Monedas = ganadas con tareas - precio de lo comprado (nunca negativo). */
    fun coins(earned: Long, ownedItemIds: Collection<String>): Int {
        val spent = WardrobeCatalog.filter { it.id in ownedItemIds }.sumOf { it.price }
        return max(0L, earned - spent).toInt()
    }

    fun canBuy(entry: WardrobeEntry, level: Int, owned: Boolean, coins: Int): Boolean =
        level >= entry.unlockLevel && !owned && coins >= entry.price

    fun canToggle(entry: WardrobeEntry, level: Int, owned: Boolean): Boolean =
        level >= entry.unlockLevel && owned
}
