package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalculateTimeDecayUseCase @Inject constructor() {

    private val secondsPerHungerPoint = 60L // 1 ед. голода в минуту
    private val secondsPerEnergyPoint = 120L // 1 ед. энергии в 2 минуты

    operator fun invoke(pet: Pet, currentTime: Long): Pet {
        if (pet.lifeState.isTerminal() || pet.lifeState.status == PetLifeStatus.COLLAPSED) {
            return pet
        }

        val lastUpdate = pet.stats.updatedAt
        val elapsedSeconds = (currentTime - lastUpdate) / 1000 // Переводим мс в секунды

        if (elapsedSeconds <= 0) return pet

        val hungerDrop = (elapsedSeconds / secondsPerHungerPoint).toInt()
        val energyDrop = (elapsedSeconds / secondsPerEnergyPoint).toInt()

        if (hungerDrop == 0 && energyDrop == 0) return pet

        val multiplier = if (pet.lifeState.status == PetLifeStatus.SICK) 2.0f else 1.0f

        val newStats = PetStats(
            hunger = (pet.stats.hunger - (hungerDrop * multiplier)).toInt().coerceIn(0, 100),
            energy = (pet.stats.energy - (energyDrop * multiplier)).toInt().coerceIn(0, 100),
            cleanliness = (pet.stats.cleanliness - (hungerDrop * 0.5f * multiplier)).toInt().coerceIn(0, 100),
            happiness = (pet.stats.happiness - (hungerDrop * 0.3f * multiplier)).toInt().coerceIn(0, 100),

            updatedAt = currentTime
        )

        val newLifeState = checkLifeState(newStats, pet.lifeState)

        return pet.copy(stats = newStats, lifeState = newLifeState)
    }

    private fun checkLifeState(stats: PetStats, currentLifeState: PetLifeState): PetLifeState {
        if (stats.hunger <= 0 && stats.energy <= 0) {
            return currentLifeState.copy(status = PetLifeStatus.DEAD)
        }
        if (stats.energy <= 0) {
            return currentLifeState.copy(status = PetLifeStatus.COLLAPSED, recoveryEndTime = System.currentTimeMillis() + 3600000)
        }
        return currentLifeState
    }
}