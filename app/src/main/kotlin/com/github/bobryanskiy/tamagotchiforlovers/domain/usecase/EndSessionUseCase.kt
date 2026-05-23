package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import javax.inject.Inject

class EndSessionUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(pairId: String, callerId: String): PairResult<Unit> {
        return pairRepository.endSession(pairId, callerId)
    }
}