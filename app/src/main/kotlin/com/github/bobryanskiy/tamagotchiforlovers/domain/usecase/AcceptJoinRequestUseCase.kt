package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import javax.inject.Inject

class AcceptJoinRequestUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(
        pairId: String,
        guestId: String,
        callerId: String
    ): PairResult<Unit> {
        if (pairId.isBlank() || guestId.isBlank() || callerId.isBlank()) {
            return DomainResult.Failure(PairError.InvalidInput)
        }

        val pair = pairRepository.getPair(pairId)
            ?: return DomainResult.Failure(PairError.PairNotFound)

        if (pair.userId1 != callerId) {
            return DomainResult.Failure(PairError.CreatorOnly)
        }

        if (!pair.canAcceptRequests) {
            return DomainResult.Failure(PairError.AlreadyJoined)
        }

        return pairRepository.acceptJoinRequest(pairId, guestId, callerId)
    }
}