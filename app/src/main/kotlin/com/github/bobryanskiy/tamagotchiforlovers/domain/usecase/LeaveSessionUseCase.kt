package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import javax.inject.Inject

class LeaveSessionUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(pairId: String, guestId: String): PairResult<Unit> {
        return pairRepository.leaveSession(pairId, guestId)
    }
}