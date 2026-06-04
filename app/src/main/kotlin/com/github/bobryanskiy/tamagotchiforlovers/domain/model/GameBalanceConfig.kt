package com.github.bobryanskiy.tamagotchiforlovers.domain.model

data class GameBalanceConfig(
    // Decay rates (скорость падения статов)
    val secondsPerHungerPoint: Long = 60L,
    val secondsPerEnergyPoint: Long = 120L,
    val secondsPerCleanlinessPoint: Long = 60L,
    val secondsPerHappinessPoint: Long = 60L,

    // Thresholds
    val criticalThreshold: Int = 15,
    val warningThreshold: Int = 30,
    val deathThreshold: Int = 0,

    // Action boosts (бонусы от действий)
    val feedHungerBoost: Int = 30,
    val feedHappinessBoost: Int = 10,
    val feedEnergyCost: Int = 2,
    val feedCleanlinessCost: Int = 1,

    val playHappinessBoost: Int = 25,
    val playEnergyCost: Int = 5,
    val playHungerCost: Int = 3,
    val playCleanlinessCost: Int = 2,

    val cleanCleanlinessBoost: Int = 40,
    val cleanEnergyCost: Int =3,
    val cleanHappinessCost: Int = 2,

    val restEnergyBoost: Int = 35,
    val restHungerCost: Int = 3,

    // Alarm intervals
    val defaultAlarmIntervalMs: Long = 3600_000L,
    val debugAlarmIntervalMs: Long = 30_000L,
    val minAlarmIntervalMs: Long = 60_000L,

    // Life state check interval
    val lifeStateCheckIntervalMs: Long = 30_000L,

    // Death prevention
    val deathPreventionThreshold: Int = 5,
) {
    companion object {
        val DEFAULT = GameBalanceConfig()
    }
}
