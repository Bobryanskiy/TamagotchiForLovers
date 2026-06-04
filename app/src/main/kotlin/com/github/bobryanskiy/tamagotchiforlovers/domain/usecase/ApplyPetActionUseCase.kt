package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.GameBalanceConfig
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.BalanceConfigRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PetResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import javax.inject.Inject

class ApplyPetActionUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val evaluateStateUseCase: EvaluatePetCriticalStateUseCase,
    private val calculateLiveStatsUseCase: CalculateLiveStatsUseCase,
    private val balanceConfigRepository: BalanceConfigRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(petId: String, action: PetAction): PetResult<Unit> {
        val config = balanceConfigRepository.get()

        val pet = when (val result = petRepository.getPetById(petId)) {
            is DomainResult.Success -> result.data ?: return DomainResult.Failure(PetError.PetNotFound)
            is DomainResult.Failure -> return result
        }

        if (pet.lifeState.status == PetLifeStatus.DEAD) {
            return DomainResult.Failure(PetError.ActionBlocked)
        }

        val currentTime = clock.currentTimeMillis()
        val livePet = calculateLiveStatsUseCase(pet, currentTime)
        val newStats = livePet.stats.applyAction(action, currentTime, config)

        if (wouldCauseDeath(newStats, config)) {
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

    private fun wouldCauseDeath(stats: PetStats, config: GameBalanceConfig): Boolean {
        val threshold = config.deathPreventionThreshold
        return stats.hunger <= threshold
                || stats.energy <= threshold
                || stats.cleanliness <= threshold
                || stats.happiness <= threshold
    }
}
