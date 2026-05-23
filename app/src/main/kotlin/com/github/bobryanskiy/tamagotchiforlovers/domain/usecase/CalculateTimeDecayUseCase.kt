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

        val newLifeState = checkLifeState(newStats, pet.lifeState, currentTime)

        return pet.copy(stats = newStats, lifeState = newLifeState)
    }

    private fun checkLifeState(stats: PetStats, currentLifeState: PetLifeState, currentTime: Long): PetLifeState {
        // Проверяем смерть - если голод 0
        if (stats.hunger <= 0) {
            return PetLifeState(status = PetLifeStatus.DEAD, isActionsBlocked = true)
        }
        // Проверяем побег - если счастье 0
        if (stats.happiness <= 0) {
            return PetLifeState(status = PetLifeStatus.ESCAPED, isActionsBlocked = true)
        }
        // Проверяем коллапс - если энергия 0
        if (stats.energy <= 0) {
            return PetLifeState(status = PetLifeStatus.COLLAPSED, recoveryEndTime = currentTime + 3600000, isActionsBlocked = true)
        }
        // Проверяем болезнь - если чистота 0
        if (stats.cleanliness <= 0) {
            return PetLifeState(status = PetLifeStatus.SICK, decayMultiplier = 2.0f)
        }
        return PetLifeState(status = PetLifeStatus.NORMAL, decayMultiplier = 1.0f)
    }
}