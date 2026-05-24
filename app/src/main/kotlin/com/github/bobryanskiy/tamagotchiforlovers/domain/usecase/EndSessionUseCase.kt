package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import javax.inject.Inject

class EndSessionUseCase @Inject constructor(
    private val pairRepository: PairRepository,
    private val petRepository: PetRepository,
    private val sessionRepository: SessionRepository,
    private val logger: AppLogger
) {

    companion object {
        private const val TAG = "EndSessionUseCase"
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
            logger.w(TAG, "User $callerId tried to end pair $pairId but is not host")
            return DomainResult.Failure(PairError.CreatorOnly)
        }

        if (pair.status != PairStatus.ACTIVE) {
            logger.w(TAG, "Pair $pairId is not ACTIVE (status=${pair.status}), cannot end")
            return DomainResult.Failure(PairError.AlreadyEnded)
        }

        val endResult = pairRepository.endSession(pairId, callerId)
        if (endResult is DomainResult.Failure) {
            logger.e(TAG, "Failed to end pair $pairId in repository")
            return endResult
        }

        runCatching {
            petRepository.updatePairId(pair.currentPetId, null)
        }.onFailure { e ->
            logger.w(TAG, "Failed to clear pet's currentPairId: ${e.message}")
        }

        runCatching {
            sessionRepository.clearActivePairId()
            sessionRepository.clearPairStatus()
        }.onFailure { e ->
            logger.w(TAG, "Failed to clear host session: ${e.message}")
        }

        logger.d(TAG, "✅ Pair $pairId ended by host $callerId")
        return DomainResult.Success(Unit)
    }
}