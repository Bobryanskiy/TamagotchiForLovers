package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PetResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import javax.inject.Inject

/**
 * Применяет действие к питомцу: обновляет статы и критическое состояние.
 *
 * Порядок:
 * 1. Получаем пета
 * 2. Проверяем блокировку действий
 * 3. Применяем действие (PetStats.applyAction)
 * 4. Оцениваем новое состояние (EvaluatePetCriticalStateUseCase)
 * 5. Сохраняем изменения
 */
class ApplyPetActionUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val evaluateStateUseCase: EvaluatePetCriticalStateUseCase,
    private val calculateLiveStatsUseCase: CalculateLiveStatsUseCase,
    private val clock: Clock
) {
    companion object {
        private const val DEATH_PREVENTION_THRESHOLD = 5
    }


    suspend operator fun invoke(petId: String, action: PetAction): PetResult<Unit> {
        val pet = when (val result = petRepository.getPetById(petId)) {
            is DomainResult.Success -> result.data ?: return DomainResult.Failure(PetError.PetNotFound)
            is DomainResult.Failure -> return result
        }

        if (pet.lifeState.status in listOf(PetLifeStatus.DEAD, PetLifeStatus.ESCAPED)) {
            return DomainResult.Failure(PetError.ActionBlocked)
        }

        val currentTime = clock.currentTimeMillis()
        val livePet = calculateLiveStatsUseCase(pet, currentTime)
        val newStats = livePet.stats.applyAction(action, currentTime)

        if (wouldCauseDeath(newStats)) {
            return DomainResult.Failure(PetError.ActionWouldKillPet)
        }
        val newState = evaluateStateUseCase(newStats, currentTime)

        val statsResult = petRepository.updateStats(
            petId = petId,
            hunger = newStats.hunger,
            energy = newStats.energy,
            cleanliness = newStats.cleanliness,
            happiness = newStats.happiness
        )
        if (statsResult is DomainResult.Failure) return statsResult

        return petRepository.updateCriticalState(petId, newState)
    }

    private fun wouldCauseDeath(stats: PetStats): Boolean {
        return stats.hunger <= DEATH_PREVENTION_THRESHOLD
                || stats.energy <= DEATH_PREVENTION_THRESHOLD
                || stats.cleanliness <= DEATH_PREVENTION_THRESHOLD
                || stats.happiness <= DEATH_PREVENTION_THRESHOLD
    }
}