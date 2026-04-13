package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import javax.inject.Inject

class CreatePairUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(ownerId: String, pet: Pet): Result<Unit> {
        if (ownerId.isBlank()) return Result.failure(IllegalArgumentException("ID владельца пуст"))

        return pairRepository.createPair(ownerId, pet)
    }
}