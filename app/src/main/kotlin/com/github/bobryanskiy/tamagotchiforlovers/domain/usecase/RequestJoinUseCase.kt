package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class RequestJoinUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(pairId: String, guestId: String): DomainResult<Unit> {
        if (pairId.isBlank() || guestId.isBlank()) return DomainResult.Failure(PairError.InvalidInput)

        return pairRepository.requestJoin(pairId, guestId)
    }
}