package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import javax.inject.Inject

class KickPartnerUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(
        pairId: String,
        callerId: String
    ): PairResult<Unit> {
        if (pairId.isBlank() || callerId.isBlank()) {
            return DomainResult.Failure(PairError.InvalidInput)
        }

        val currentPair = pairRepository.getPair(pairId)
            ?: return DomainResult.Failure(PairError.PairNotFound)

        if (currentPair.status != PairStatus.ACTIVE) {
            return DomainResult.Failure(PairError.SessionNotActive)
        }
        if (currentPair.userId1 != callerId) {
            return DomainResult.Failure(PairError.CreatorOnly)
        }

        return pairRepository.kickPartner(pairId, callerId)
    }
}