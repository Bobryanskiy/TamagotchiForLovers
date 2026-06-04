package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetPair
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import javax.inject.Inject

class FindPairByInviteKeyUseCase @Inject constructor(
    private val pairRepository: PairRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(inviteCode: String): PairResult<PetPair> {
        if (userRepository.getCurrentUserId() == null) {
            return DomainResult.Failure(PairError.NotAuthenticated)
        }

        if (inviteCode.isBlank()) {
            return DomainResult.Failure(PairError.InvalidInput)
        }

        return pairRepository.findPairByInviteKey(inviteCode.uppercase())
    }
}
