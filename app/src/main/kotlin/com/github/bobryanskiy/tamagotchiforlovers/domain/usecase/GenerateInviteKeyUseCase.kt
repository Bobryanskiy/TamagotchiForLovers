package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import javax.inject.Inject

class GenerateInviteKeyUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(pairId: String, currentPair: Pair): Result<String> {
        if (currentPair.status != PairStatus.PENDING) {
            return Result.failure(IllegalStateException("Ключ можно сгенерировать только для ожидающей пары"))
        }
        if (currentPair.inviteKey != null && currentPair.inviteKey.expiresAt > System.currentTimeMillis()) {
            return Result.success(currentPair.inviteKey.code)
        }

        return pairRepository.generateInviteKey(pairId)
    }
}