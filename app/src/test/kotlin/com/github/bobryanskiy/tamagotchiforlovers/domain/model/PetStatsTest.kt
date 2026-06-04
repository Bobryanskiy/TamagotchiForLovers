package com.github.bobryanskiy.tamagotchiforlovers.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PetStatsTest {

    private val initialStats = PetStats(
        hunger = 50, energy = 50, cleanliness = 50, happiness = 50, updatedAt = 0L
    )
    private val defaultConfig = GameBalanceConfig.DEFAULT

    @Test
    fun `Feed should increase hunger by configured amount`() {
        val result = initialStats.applyAction(PetAction.Feed, currentTime = 1000L, config = defaultConfig)

        assertEquals(80, result.hunger)       // 50 + 30
        assertEquals(45, result.energy)       // 50 - 5
        assertEquals(47, result.cleanliness)  // 50 - 3
        assertEquals(60, result.happiness)    // 50 + 10
        assertEquals(1000L, result.updatedAt)
    }

    @Test
    fun `Play should increase happiness by configured amount`() {
        val result = initialStats.applyAction(PetAction.Play, currentTime = 2000L, config = defaultConfig)

        assertEquals(75, result.happiness)
        assertEquals(35, result.energy)
        assertEquals(40, result.hunger)
        assertEquals(45, result.cleanliness)
        assertEquals(2000L, result.updatedAt)
    }

    @Test
    fun `stats should not exceed 100 or drop below 0`() {
        val maxStats = PetStats(hunger = 90, energy = 90, cleanliness = 90, happiness = 90, updatedAt = 0L)
        val result = maxStats.applyAction(PetAction.Feed, currentTime = 1000L, config = defaultConfig)

        assertEquals(100, result.hunger)
    }

    @Test
    fun `custom config should affect action result`() {
        val customConfig = GameBalanceConfig(feedHungerBoost = 50)
        val result = initialStats.applyAction(PetAction.Feed, currentTime = 1000L, config = customConfig)

        assertEquals(100, result.hunger) // 50 + 50 = 100
    }
}
