// domain/usecase/CalculateLiveStatsUseCase.kt
package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import javax.inject.Inject

/**
 * Вычисляет "живые" статы пета на основе времени, прошедшего с последнего обновления.
 *
 * Чистая функция, НЕ трогает БД, НЕ эмитит ничего.
 * Вызывается из UI при каждом рендере.
 */
class CalculateLiveStatsUseCase @Inject constructor() {

    companion object {
        // Игровые константы баланса
        const val SECONDS_PER_HUNGER_POINT = 60L
        const val SECONDS_PER_ENERGY_POINT = 120L
        const val CLEANLINESS_RATIO = 0.5f
        const val HAPPINESS_RATIO = 0.3f
    }

    /**
     * @param pet исходный питомец (из БД)
     * @param currentTime текущее время в мс
     * @return новый Pet с пересчитанными статами (immutable copy)
     */
    operator fun invoke(pet: Pet, currentTime: Long): Pet {
        if (pet.lifeState.isTerminal() || pet.lifeState.status == PetLifeStatus.COLLAPSED) {
            return pet
        }

        val elapsedSeconds = (currentTime - pet.stats.updatedAt) / 1000
        if (elapsedSeconds <= 0) return pet

        val hungerDrop = (elapsedSeconds / SECONDS_PER_HUNGER_POINT).toInt()
        val energyDrop = (elapsedSeconds / SECONDS_PER_ENERGY_POINT).toInt()

        if (hungerDrop == 0 && energyDrop == 0) return pet

        val multiplier = if (pet.lifeState.status == PetLifeStatus.SICK) 2.0f else 1.0f

        val newStats = PetStats(
            hunger = (pet.stats.hunger - (hungerDrop * multiplier)).toInt().coerceIn(0, 100),
            energy = (pet.stats.energy - (energyDrop * multiplier)).toInt().coerceIn(0, 100),
            cleanliness = (pet.stats.cleanliness - (hungerDrop * CLEANLINESS_RATIO * multiplier)).toInt().coerceIn(0, 100),
            happiness = (pet.stats.happiness - (hungerDrop * HAPPINESS_RATIO * multiplier)).toInt().coerceIn(0, 100),
            updatedAt = pet.stats.updatedAt
        )

        return pet.copy(stats = newStats)
    }
}