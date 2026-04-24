package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class KickPlayerUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(pairId: String, currentPair: Pair, kickedUserId: String, creatorId: String): DomainResult<Unit> {
        if (pairId.isBlank() || kickedUserId.isBlank() || creatorId.isBlank()) return DomainResult.Failure(PairError.InvalidInput)
        if (currentPair.status != PairStatus.ACTIVE) return DomainResult.Failure(PairError.SessionNotActive)
        if (currentPair.userId1 != creatorId) return DomainResult.Failure(PairError.CreatorOnly)
        if (currentPair.userId2 != kickedUserId) return DomainResult.Failure(PairError.InvalidRequest)

        return pairRepository.kickPlayer(pairId, kickedUserId)
    }
}