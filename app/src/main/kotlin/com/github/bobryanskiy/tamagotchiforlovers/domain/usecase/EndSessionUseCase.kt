package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class EndSessionUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(pairId: String, currentPair: Pair, creatorId: String): DomainResult<Unit> {
        if (pairId.isBlank() || creatorId.isBlank()) return DomainResult.Failure(PairError.InvalidInput)
        if (currentPair.userId1 != creatorId) return DomainResult.Failure(PairError.CreatorOnly)
        if (currentPair.status == PairStatus.ENDED) return DomainResult.Failure(PairError.AlreadyEnded)

        return pairRepository.endSession(pairId)
    }
}