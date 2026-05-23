package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class RequestJoinUseCase @Inject constructor(
    private val pairRepository: PairRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(inviteCode: String): DomainResult<Unit> {
        if (inviteCode.isBlank()) return DomainResult.Failure(PairError.InvalidInput)

        val pairResult = pairRepository.findPairByInviteKey(inviteCode)
        if (pairResult is DomainResult.Failure) return pairResult

        val pair = (pairResult as DomainResult.Success).data
        val guestId =userRepository.getCurrentUserId()
            ?: return DomainResult.Failure(PairError.Unknown)

        return pairRepository.requestJoin(pair.id, guestId)
    }
}