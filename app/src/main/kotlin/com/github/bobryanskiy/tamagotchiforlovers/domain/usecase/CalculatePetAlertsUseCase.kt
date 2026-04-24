package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.AlertSchedule
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAlertType
import javax.inject.Inject
import kotlin.math.max

class CalculatePetAlertsUseCase @Inject constructor() {
    private data class StatThreshold(
        val currentValue: Int,
        val decayRatePerMin: Float,
        val warning: Pair<Int, PetAlertType>,
        val critical: Pair<Int, PetAlertType>,
        val zeroConsequence: PetAlertType
    )

    operator fun invoke(pet: Pet, now: Long = System.currentTimeMillis()): List<AlertSchedule> {
        val elapsedMin = max(0f, (now - pet.stats.updatedAt) / 60_000f)
        val schedules = mutableListOf<AlertSchedule>()

        val configs = listOf(
            StatThreshold(pet.stats.hunger, 0.5f, 20 to PetAlertType.HUNGER_WARNING, 5 to PetAlertType.HUNGER_CRITICAL, PetAlertType.HUNGER_DEATH),
            StatThreshold(pet.stats.energy, 0.2f, 20 to PetAlertType.ENERGY_WARNING, 5 to PetAlertType.ENERGY_COLLAPSE, PetAlertType.ENERGY_COLLAPSE),
            StatThreshold(pet.stats.cleanliness, 0.3f, 20 to PetAlertType.CLEANLINESS_WARNING, 5 to PetAlertType.CLEANLINESS_SICK, PetAlertType.CLEANLINESS_SICK),
            StatThreshold(pet.stats.happiness, 0.4f, 20 to PetAlertType.HAPPINESS_WARNING, 5 to PetAlertType.HAPPINESS_RUN_AWAY, PetAlertType.HAPPINESS_RUN_AWAY)
        )

        for (config in configs) {
            val current = max(0f, config.currentValue - (elapsedMin * config.decayRatePerMin))

            val thresholds = listOf(config.warning, config.critical, 0 to config.zeroConsequence)

            for ((threshold, type) in thresholds) {
                if (current <= threshold) continue
                val minutesUntil = (current - threshold) / config.decayRatePerMin
                val triggerAt = now + (minutesUntil * 60_000L)
                schedules.add(AlertSchedule(pet.id, type, triggerAt.toLong()))
            }
        }
        return schedules
    }
}