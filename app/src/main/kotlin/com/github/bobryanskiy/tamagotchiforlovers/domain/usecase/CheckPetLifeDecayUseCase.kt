package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import javax.inject.Inject

data class DecayCheckResult(
    val pet: Pet,
    val newState: PetLifeState,
    val hasChanged: Boolean
)

class CheckPetLifeDecayUseCase @Inject constructor(
    private val calculateLiveStatsUseCase: CalculateLiveStatsUseCase,
    private val evaluatePetCriticalStateUseCase: EvaluatePetCriticalStateUseCase,
    private val clock: Clock
) {
    operator fun invoke(pet: Pet): DecayCheckResult {
        if (pet.lifeState.isTerminal()) {
            return DecayCheckResult(pet, pet.lifeState, hasChanged = false)
        }

        val currentTime = clock.currentTimeMillis()
        val decayedPet = calculateLiveStatsUseCase(pet, currentTime)
        val newState = evaluatePetCriticalStateUseCase(decayedPet.stats, currentTime)
        val hasChanged = newState.status != pet.lifeState.status

        return DecayCheckResult(
            pet = if (hasChanged) decayedPet.copy(lifeState = newState) else decayedPet,
            newState = newState,
            hasChanged = hasChanged
        )
    }
}
