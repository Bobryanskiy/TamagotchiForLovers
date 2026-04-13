package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import javax.inject.Inject

class RequestJoinUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(pairId: String, guestId: String): Result<Unit> {
        if (guestId.isBlank()) return Result.failure(IllegalArgumentException("ID гостя не указан"))

        return pairRepository.requestJoin(pairId, guestId)
    }
}