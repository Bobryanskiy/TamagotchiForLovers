package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EvaluatePetCriticalStateUseCase @Inject constructor() {
    operator fun invoke(stats: PetStats, currentTime: Long = System.currentTimeMillis()): PetCriticalState {
        return when {
            stats.hunger <= 0 -> PetCriticalState(PetCriticalStatus.DEAD, isActionsBlocked = true)
            stats.happiness <= 0 -> PetCriticalState(PetCriticalStatus.ESCAPED, isActionsBlocked = true)
            stats.cleanliness <= 0 -> PetCriticalState(PetCriticalStatus.SICK, decayMultiplier = 2.0f)
            stats.energy <= 0 -> PetCriticalState(
                status = PetCriticalStatus.COLLAPSED,
                recoveryEndTime = currentTime + TimeUnit.HOURS.toMillis(2),
                isActionsBlocked = true
            )
            else -> PetCriticalStatus.NORMAL.run { PetCriticalState(this) }
        }
    }
}