package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class CreatePairUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(ownerId: String, pairName: String, petId: String): DomainResult<String> {
        if (ownerId.isBlank()) return DomainResult.Failure(PairError.InvalidInput)

        return pairRepository.createPair(ownerId, pairName, petId)
    }
}