package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class RejectJoinRequestUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(
        pairId: String,
        guestId: String,
        callerId: String
    ): DomainResult<Unit> {
        if (pairId.isBlank() || callerId.isBlank()) {
            return DomainResult.Failure(PairError.InvalidInput)
        }

        val pair = pairRepository.getPair(pairId)
            ?: return DomainResult.Failure(PairError.PairNotFound)

        if (pair.userId1 != callerId) {
            return DomainResult.Failure(PairError.CreatorOnly)
        }

        return pairRepository.rejectJoinRequest(pairId, guestId, callerId)
    }
}