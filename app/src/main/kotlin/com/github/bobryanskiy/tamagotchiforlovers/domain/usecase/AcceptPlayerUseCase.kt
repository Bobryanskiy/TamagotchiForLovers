package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import javax.inject.Inject

class AcceptPlayerUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(pairId: String, currentPair: Pair, guestId: String): Result<Unit> {
        if (guestId.isBlank()) return Result.failure(IllegalArgumentException("ID игрока не указан"))

        if (currentPair.status != PairStatus.PENDING) {
            return Result.failure(IllegalStateException("Пара уже активна или закрыта"))
        }
        if (currentPair.userId2 != null) {
            return Result.failure(IllegalStateException("Место второго игрока уже занято"))
        }
        if (currentPair.pendingRequest?.guestId != guestId) {
            return Result.failure(IllegalStateException("Запрос на вход от этого игрока не найден"))
        }

        return pairRepository.acceptPlayer(pairId, guestId)
    }
}