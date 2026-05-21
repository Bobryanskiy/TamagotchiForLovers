package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvaluatePetCriticalStateUseCase @Inject constructor() {
    operator fun invoke(stats: PetStats, currentTime: Long = System.currentTimeMillis()): PetLifeState {
        return when {
            stats.hunger <= 0 -> PetLifeState(PetLifeStatus.DEAD, isActionsBlocked = true)
            stats.happiness <= 0 -> PetLifeState(PetLifeStatus.ESCAPED, isActionsBlocked = true)
            stats.energy <= 0 -> PetLifeState(
                status = PetLifeStatus.COLLAPSED,
                recoveryEndTime = currentTime + TimeUnit.HOURS.toMillis(2),
                isActionsBlocked = true
            )
            stats.cleanliness <= 0 -> PetLifeState(PetLifeStatus.SICK, decayMultiplier = 2.0f)
            else -> PetLifeState(PetLifeStatus.NORMAL, decayMultiplier = 1.0f)
        }
    }
}