package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EvaluatePetCriticalStateUseCase @Inject constructor() {
    operator fun invoke(stats: PetStats, currentTime: Long): PetLifeState {
        val minStat = minOf(stats.hunger, stats.energy, stats.cleanliness, stats.happiness)

        return when {
            minStat <= 0 -> PetLifeState(
                status = PetLifeStatus.DEAD,
                recoveryEndTime = null,
                decayMultiplier = 0f
            )
            minStat <= 10 -> PetLifeState(
                status = PetLifeStatus.COLLAPSED,
                recoveryEndTime = currentTime + 30 * 60 * 1000,
                decayMultiplier = 2.0f
            )
            minStat <= 25 -> PetLifeState(
                status = PetLifeStatus.SICK,
                recoveryEndTime = currentTime + 60 * 60 * 1000,
                decayMultiplier = 1.5f
            )
            else -> PetLifeState(
                status = PetLifeStatus.NORMAL,
                recoveryEndTime = null,
                decayMultiplier = 1.0f
            )
        }
    }
}