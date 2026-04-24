package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class LeaveSessionUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(pairId: String, currentPair: Pair, leavingUserId: String): DomainResult<Unit> {
        if (leavingUserId.isBlank() || leavingUserId.isBlank()) return DomainResult.Failure(PairError.InvalidInput)
        if (currentPair.status != PairStatus.ACTIVE) return DomainResult.Failure(PairError.SessionNotActive)
        if (currentPair.userId2 != leavingUserId) return DomainResult.Failure(PairError.GuestOnly)

        return pairRepository.leaveSession(pairId, leavingUserId)
    }
}