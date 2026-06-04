package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.BalanceConfigRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.github.bobryanskiy.tamagotchiforlovers.util.ValidationUtils.getPairNameErrorResId
import javax.inject.Inject

class CreatePairWithInviteUseCase @Inject constructor(
    private val pairRepository: PairRepository,
    private val userRepository: UserRepository,
    private val clock: Clock,
    private val balanceConfigRepository: BalanceConfigRepository,
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "CreatePairUseCase"
    }

    suspend operator fun invoke(pairName: String, petId: String): PairResult<PairInviteData> {
        val config = balanceConfigRepository.get()

        val trimmed = pairName.trim()
        logger.d(TAG, "invoke: name='$trimmed', petId='$petId'")

        val errorResId = getPairNameErrorResId(trimmed)
        if (errorResId != null) {
            logger.w(TAG, "Validation failed: $errorResId")
            return DomainResult.Failure(PairError.InvalidInput)
        }

        val creatorId = userRepository.getCurrentUserId()
        if (creatorId == null) {
            logger.e(TAG, "❌ Not authenticated!")
            return DomainResult.Failure(PairError.NotAuthenticated)
        }
        logger.d(TAG, "creatorId=$creatorId")

        logger.d(TAG, "Calling pairRepository.createPair...")
        val pairIdResult = pairRepository.createPair(creatorId, trimmed, petId)
        if (pairIdResult is DomainResult.Failure) {
            logger.e(TAG, "❌ createPair failed: ${pairIdResult.error}")
            return pairIdResult
        }

        val pairId = (pairIdResult as DomainResult.Success).data
        logger.d(TAG, "pairId=$pairId, generating invite key...")

        return when (val codeResult = pairRepository.generateInviteKey(pairId)) {
            is DomainResult.Success -> {
                logger.d(TAG, "✅ Invite code generated")
                DomainResult.Success(
                    PairInviteData(
                        pairId = pairId,
                        inviteCode = codeResult.data,
                        expiresAt = clock.currentTimeMillis() + config.inviteValidityMs,
                        creatorId = creatorId
                    )
                )
            }

            is DomainResult.Failure -> {
                logger.e(TAG, "❌ generateInviteKey failed: ${codeResult.error}")
                codeResult
            }
        }
    }
}

data class PairInviteData(
    val pairId: String,
    val inviteCode: String,
    val expiresAt: Long,
    val creatorId: String
)
