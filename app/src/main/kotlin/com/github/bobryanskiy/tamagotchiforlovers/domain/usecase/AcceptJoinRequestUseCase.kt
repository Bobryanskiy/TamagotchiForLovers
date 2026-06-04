package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import javax.inject.Inject

class AcceptJoinRequestUseCase @Inject constructor(
    private val pairRepository: PairRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        pairId: String,
        guestId: String,
        callerId: String
    ): PairResult<Unit> {
        if (pairId.isBlank() || guestId.isBlank() || callerId.isBlank()) {
            return DomainResult.Failure(PairError.InvalidInput)
        }

        val pairResult = pairRepository.getPair(pairId)
        if (pairResult is DomainResult.Failure) return pairResult
        val pair = (pairResult as DomainResult.Success).data
            ?: return DomainResult.Failure(PairError.PairNotFound)

        if (pair.userId1 != callerId) {
            return DomainResult.Failure(PairError.CreatorOnly)
        }

        if (!pair.canAcceptRequests) {
            return DomainResult.Failure(PairError.AlreadyJoined)
        }

        val pendingRequest = pair.pendingRequest
            ?: return DomainResult.Failure(PairError.InvalidRequest)

        if (pendingRequest.guestId != guestId) {
            return DomainResult.Failure(PairError.InvalidRequest)
        }

        val hostPetId = pair.currentPetId
        val result = pairRepository.acceptJoinRequest(pairId, guestId, callerId)

        if (result is DomainResult.Success) {
            userRepository.updateUserSession(callerId, hostPetId, pairId)
        }

        return result
    }
}
