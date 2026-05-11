package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class AcceptPlayerUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(pairId: String, guestId: String): DomainResult<Unit> {
        if (pairId.isBlank() || guestId.isBlank()) return DomainResult.Failure(PairError.InvalidInput)

        val pair = pairRepository.getPair(pairId) ?: return DomainResult.Failure(PairError.NotFound)

        if (pair.status != PairStatus.PENDING) return DomainResult.Failure(PairError.SessionNotActive)
        if (pair.userId2 != null) return DomainResult.Failure(PairError.InvalidRequest)
        if (pair.pendingRequest?.guestId != guestId) return DomainResult.Failure(PairError.InvalidRequest)

        return pairRepository.acceptPlayer(pairId, guestId)
    }
}