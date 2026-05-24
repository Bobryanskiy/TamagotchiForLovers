package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetPair
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import javax.inject.Inject

class FindPairByInviteKeyUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(inviteCode: String): PairResult<PetPair> {
        if (inviteCode.isBlank()) {
            return DomainResult.Failure(PairError.InvalidInput)
        }

        // Репозиторий уже проверяет:
        // 1. Существует ли пара с таким кодом
        // 2. Не истёк ли срок действия invite_key.expires_at
        // 3. Может ли пара принимать гостей (canAcceptRequests)
        return pairRepository.findPairByInviteKey(inviteCode.uppercase())
    }
}