package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class RequestJoinUseCase @Inject constructor(
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(inviteCode: String): DomainResult<Unit> {
        if (inviteCode.isBlank()) return DomainResult.Failure(PairError.InvalidInput)

        // Сначала находим пару по коду приглашения
        val pairResult = pairRepository.findPairByInviteKey(inviteCode)
        if (pairResult is DomainResult.Failure) return pairResult

        val pair = (pairResult as DomainResult.Success).data
        val guestId = pairRepository.getCurrentUserId()
            ?: return DomainResult.Failure(PairError.Unknown)

        // Затем запрашиваем присоединение
        return pairRepository.requestJoin(pair.id, guestId)
    }
}