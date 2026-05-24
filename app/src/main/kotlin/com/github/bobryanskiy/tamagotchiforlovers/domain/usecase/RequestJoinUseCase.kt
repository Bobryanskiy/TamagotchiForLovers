package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import javax.inject.Inject

class RequestJoinUseCase @Inject constructor(
    private val pairRepository: PairRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(pairId: String, guestId: String): PairResult<Unit> {
        if (pairId.isBlank() || guestId.isBlank()) {
            return DomainResult.Failure(PairError.InvalidInput)
        }

        // Сохраняем pairId в сессию гостя (для ожидания одобрения)
        userRepository.updateUserSession(guestId, null, pairId)

        return pairRepository.requestJoin(pairId, guestId)
    }
}