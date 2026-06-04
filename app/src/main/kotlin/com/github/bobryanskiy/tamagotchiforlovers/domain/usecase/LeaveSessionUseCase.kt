package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import javax.inject.Inject

class LeaveSessionUseCase @Inject constructor(
    private val pairRepository: PairRepository,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val logger: AppLogger
) {

    companion object {
        private const val TAG = "LeaveSessionUseCase"
    }

    suspend operator fun invoke(pairId: String, userId: String): PairResult<Unit> {

        if (pairId.isBlank() || userId.isBlank()) {
            logger.w(TAG, "Invalid input: pairId or userId is blank")
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

        if (pair.userId1 == userId) {
            logger.w(TAG, "User $userId is host, cannot leave — use EndSession instead")
            return DomainResult.Failure(PairError.CreatorOnly)
        }
        if (pair.userId2 != userId) {
            logger.w(TAG, "User $userId is not a member of pair $pairId")
            return DomainResult.Failure(PairError.GuestOnly)
        }

        if (pair.status != PairStatus.ACTIVE) {
            logger.w(TAG, "Pair $pairId is not ACTIVE (status=${pair.status})")
            return DomainResult.Failure(PairError.SessionNotActive)
        }

        val leaveResult = pairRepository.leaveSession(pairId, userId)
        if (leaveResult is DomainResult.Failure) {
            logger.e(TAG, "Failed to leave pair $pairId in repository")
            return leaveResult
        }

        runCatching {
            sessionRepository.clearActivePairId()
            sessionRepository.clearPairStatus()
        }.onFailure { e ->
            logger.w(TAG, "Failed to clear session: ${e.message}")
        }

        runCatching {
            userRepository.updateUserSession(userId, petId = null, pairId = null)
        }.onFailure { e ->
            logger.w(TAG, "Failed to update user session: ${e.message}")
        }

        logger.d(TAG, "✅ User $userId left pair $pairId")
        return DomainResult.Success(Unit)
    }
}
