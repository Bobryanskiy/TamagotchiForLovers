package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.BalanceConfigRepository
import javax.inject.Inject

/**
 * Вычисляет "живые" статы пета на основе времени, прошедшего с последнего обновления.
 */
class CalculateLiveStatsUseCase @Inject constructor(
    private val balanceConfigRepository: BalanceConfigRepository
) {
    operator fun invoke(pet: Pet, currentTime: Long): Pet {
        if (pet.lifeState.isTerminal()) {
            return pet
        }

        val elapsedSeconds = (currentTime - pet.stats.updatedAt) / 1000
        if (elapsedSeconds <= 0) return pet

        val config = balanceConfigRepository.get()

        val hungerDrop = (elapsedSeconds / config.secondsPerHungerPoint).toInt()
        val energyDrop = (elapsedSeconds / config.secondsPerEnergyPoint).toInt()
        val cleanlinessDrop = (elapsedSeconds / config.secondsPerCleanlinessPoint).toInt()
        val happinessDrop = (elapsedSeconds / config.secondsPerHappinessPoint).toInt()

        if (hungerDrop == 0 && energyDrop == 0) return pet

        val newStats = PetStats(
            hunger = (pet.stats.hunger - hungerDrop).coerceIn(0, 100),
            energy = (pet.stats.energy - energyDrop).coerceIn(0, 100),
            cleanliness = (pet.stats.cleanliness - cleanlinessDrop).coerceIn(0, 100),
            happiness = (pet.stats.happiness - happinessDrop).coerceIn(0, 100),
            updatedAt = pet.stats.updatedAt
        )

        return pet.copy(stats = newStats)
    }
}
