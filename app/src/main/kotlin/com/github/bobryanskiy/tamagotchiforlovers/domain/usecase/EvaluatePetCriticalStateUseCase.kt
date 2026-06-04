package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.DeathCause
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.BalanceConfigRepository
import javax.inject.Inject

class EvaluatePetCriticalStateUseCase @Inject constructor(
    private val balanceConfigRepository: BalanceConfigRepository
) {
    operator fun invoke(stats: PetStats, currentTime: Long): PetLifeState {
        val config = balanceConfigRepository.get()

        val deathCause = when {
            stats.hunger <= config.deathThreshold -> DeathCause.HUNGER
            stats.energy <= config.deathThreshold -> DeathCause.EXHAUSTION
            stats.cleanliness <= config.deathThreshold -> DeathCause.DISEASE
            stats.happiness <= config.deathThreshold -> DeathCause.ESCAPED
            else -> null
        }

        if (deathCause != null) {
            return PetLifeState(
                status = PetLifeStatus.DEAD,
                deathCause = deathCause
            )
        }

        return PetLifeState(status = PetLifeStatus.NORMAL)
    }
}