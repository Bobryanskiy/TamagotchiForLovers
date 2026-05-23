package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.UserError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.onFailure
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.onSuccess
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import javax.inject.Inject

class CreatePairWithInviteUseCase @Inject constructor(
    private val pairRepository: PairRepository,
    private val userRepository: UserRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        pairName: String,
        petId: String
    ): PairResult<PairInviteData> {
        val creatorId = userRepository.getCurrentUserId()
        if (pairName.isBlank()) return DomainResult.Failure(PairError.InvalidInput)
        if (creatorId == null) return DomainResult.Failure(PairError.NotAuthenticated)

        val pairIdResult = pairRepository.createPair(creatorId, pairName, petId)
        if (pairIdResult is DomainResult.Failure) return pairIdResult

        val pairId = (pairIdResult as DomainResult.Success).data

        return when (val codeResult = pairRepository.generateInviteKey(pairId)) {
            is DomainResult.Success -> DomainResult.Success(
                PairInviteData(
                    pairId = pairId,
                    inviteCode = codeResult.data,
                    expiresAt = clock.currentTimeMillis() + INVITE_VALIDITY_MS,
                    creatorId = creatorId
                )
            )
            is DomainResult.Failure -> codeResult
        }
    }

    companion object {
        private const val INVITE_VALIDITY_MS = 5 * 60 * 1000L // 5 минут
    }
}

data class PairInviteData(
    val pairId: String,
    val inviteCode: String,
    val expiresAt: Long,
    val creatorId: String
)