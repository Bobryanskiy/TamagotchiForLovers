package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PetResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplyPetActionUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val evaluateStateUseCase: EvaluatePetCriticalStateUseCase,
    private val clock: Clock
) {

    suspend operator fun invoke(petId: String, action: PetAction): PetResult<Unit> {
        val petResult = petRepository.getPetById(petId)
        if (petResult is DomainResult.Failure) return petResult
        val pet = petResult.getOrNull() ?: return DomainResult.Failure(PetError.PetNotFound)

        if (pet.lifeState.isActionsBlocked) {
            return DomainResult.Failure(PetError.ActionBlocked)
        }

        val currentTime = clock.currentTimeMillis()
        val newStats = pet.stats.applyAction(action, currentTime)
        val newState = evaluateStateUseCase(newStats, currentTime)

        val statsResult = petRepository.updateStats(
            petId = petId,
            hunger = newStats.hunger,
            energy = newStats.energy,
            cleanliness = newStats.cleanliness,
            happiness = newStats.happiness
        )
        if (statsResult is DomainResult.Failure) return statsResult

        val stateResult = petRepository.updateCriticalState(petId, newState)
        if (stateResult is DomainResult.Failure) return stateResult

        return DomainResult.Success(Unit)
    }
}