package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckPairStatusUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): PairNavigationResult {
        val pairId = sessionRepository.getActivePairId()

        return if (pairId != null) {
            PairNavigationResult.ExistingPair(pairId)
        } else {
            PairNavigationResult.NoPair
        }
    }
}

sealed class PairNavigationResult {
    data class ExistingPair(val pairId: String) : PairNavigationResult()
    object NoPair : PairNavigationResult()
}