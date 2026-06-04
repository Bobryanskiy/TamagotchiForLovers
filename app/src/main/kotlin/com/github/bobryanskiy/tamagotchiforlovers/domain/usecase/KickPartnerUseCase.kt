package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import javax.inject.Inject

class KickPartnerUseCase @Inject constructor(
    private val pairRepository: PairRepository,
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "KickPartnerUseCase"
    }

    suspend operator fun invoke(pairId: String, callerId: String): PairResult<Unit> {
        if (pairId.isBlank() || callerId.isBlank()) {
            logger.w(TAG, "Invalid input: pairId or callerId is blank")
            return DomainResult.Failure(PairError.InvalidInput)
        }

        val pairResult = pairRepository.getPair(pairId)
        if (pairResult is DomainResult.Failure) {
            logger.w(TAG, "Failed to get pair $pairId: ${pairResult.error}")
            return pairResult
        }

        val pair = (pairResult as DomainResult.Success).data
        if (pair == null) {
            logger.w(TAG, "Pair $pairId not found")
            return DomainResult.Failure(PairError.PairNotFound)
        }

        if (pair.userId1 != callerId) {
            logger.w(TAG, "User $callerId tried to kick from pair $pairId but is not host")
            return DomainResult.Failure(PairError.CreatorOnly)
        }

        if (pair.status != PairStatus.ACTIVE) {
            logger.w(TAG, "Pair $pairId is not ACTIVE (status=${pair.status}), cannot kick")
            return DomainResult.Failure(PairError.SessionNotActive)
        }

        val guestId = pair.userId2
        if (guestId == null) {
            logger.w(TAG, "Pair $pairId has no guest to kick")
            return DomainResult.Failure(PairError.PairNotFound)
        }

        val kickResult = pairRepository.kickPartner(pairId, callerId)
        if (kickResult is DomainResult.Failure) {
            logger.e(TAG, "Failed to kick guest from pair $pairId")
            return kickResult
        }

        logger.d(TAG, "✅ Host $callerId kicked guest $guestId from pair $pairId")
        return DomainResult.Success(Unit)
    }
}
