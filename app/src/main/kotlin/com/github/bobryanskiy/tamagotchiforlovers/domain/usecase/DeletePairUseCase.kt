package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import javax.inject.Inject

class DeletePairUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(pairId: String): Result<Unit> {
        return pairRepository.deletePair(pairId)
    }
}