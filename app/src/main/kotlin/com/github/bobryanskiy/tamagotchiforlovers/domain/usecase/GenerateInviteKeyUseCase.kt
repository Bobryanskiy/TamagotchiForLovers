package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.time.Clock
import javax.inject.Inject

class GenerateInviteKeyUseCase @Inject constructor(
    private val pairRepository: PairRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(pairId: String): DomainResult<String> {
        if (pairId.isBlank()) return DomainResult.Failure(PairError.InvalidInput)
        val pair = pairRepository.getPair(pairId)
            ?: return DomainResult.Failure(PairError.NotFound)

        if (pair.status != PairStatus.PENDING) return DomainResult.Failure(PairError.SessionNotActive)

        pair.inviteKey?.let { key ->
            if (key.expiresAt > clock.currentTimeMillis()) return DomainResult.Success(key.code)
        }

        return pairRepository.generateInviteKey(pairId)
    }
}