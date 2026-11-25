package com.hogargo.kmp.pet

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PetRulesTest {
    private val hour = 3_600_000L
    private val t0 = 10_000_000L

    private fun fresh() = PetRules.initial("h1", t0)

    @Test
    fun levelIsOnePlusActionsPerFive() {
        assertEquals(1, PetRules.level(0))
        assertEquals(1, PetRules.level(4))
        assertEquals(2, PetRules.level(5))
        assertEquals(3, PetRules.level(10))
    }

    @Test
    fun decayKeepsTheRemainingTime() {
        val decayed = PetRules.applyDecay(fresh(), t0 + 5 * hour)
        assertEquals(0.40, decayed.happiness, 1e-9)
        assertEquals(0.40, decayed.satiety, 1e-9)
        assertEquals(t0 + 4 * hour, decayed.lastDecayAt)
    }

    @Test
    fun noDecayBeforeTwoHours() {
        val state = fresh()
        assertEquals(state, PetRules.applyDecay(state, t0 + hour))
    }

    @Test
    fun decayNeverGoesBelowZero() {
        val decayed = PetRules.applyDecay(fresh(), t0 + 100 * hour)
        assertEquals(0.0, decayed.happiness)
        assertEquals(0.0, decayed.satiety)
    }

    @Test
    fun feedingRespectsItsOwnCooldown() {
        val fed = PetRules.care(fresh(), CareKind.FEED, t0)
        assertEquals(0.65, fed.satiety, 1e-9)
        assertEquals(1, fed.careActions)

        val tooSoon = PetRules.care(fed, CareKind.FEED, t0 + 10 * 60_000)
        assertEquals(fed, tooSoon)

        val later = PetRules.care(fed, CareKind.FEED, t0 + 16 * 60_000)
        assertEquals(2, later.careActions)
    }

    @Test
    fun feedAndPlayCooldownsAreIndependent() {
        val fed = PetRules.care(fresh(), CareKind.FEED, t0)
        val played = PetRules.care(fed, CareKind.PLAY, t0 + 1_000)
        assertEquals(2, played.careActions)
        assertEquals(0.65, played.happiness, 1e-9)
    }

    @Test
    fun valuesAreCappedAtOne() {
        var state = fresh().copy(satiety = 0.95)
        state = PetRules.care(state, CareKind.FEED, t0)
        assertEquals(1.0, state.satiety)
    }

    @Test
    fun cooldownMinutesRoundUp() {
        // Regla de Android: N = restante / 60 000 + 1
        assertEquals(15, PetRules.minutesLabelValue(PetRules.COOLDOWN_MS - 1_000))
        assertEquals(1, PetRules.minutesLabelValue(30_000))
        assertEquals(0L, PetRules.cooldownRemaining(0, 10 * PetRules.COOLDOWN_MS))
    }

    @Test
    fun coinsAreEarnedMinusSpentAndNeverNegative() {
        assertEquals(10, PetRules.coins(30, listOf("scarf")))
        assertEquals(0, PetRules.coins(10, listOf("scarf", "ball")))
    }

    @Test
    fun buyingNeedsLevelCoinsAndNotOwning() {
        val scarf = WardrobeCatalog.first { it.id == "scarf" }
        val ball = WardrobeCatalog.first { it.id == "ball" }
        val glasses = WardrobeCatalog.first { it.id == "glasses" }
        assertTrue(PetRules.canBuy(scarf, level = 1, owned = false, coins = 30))
        assertEquals(false, PetRules.canBuy(ball, level = 1, owned = false, coins = 10))
        assertEquals(false, PetRules.canBuy(glasses, level = 1, owned = false, coins = 500))
        assertEquals(false, PetRules.canBuy(scarf, level = 1, owned = true, coins = 500))
    }
}
