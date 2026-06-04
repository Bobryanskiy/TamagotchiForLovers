package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

sealed class AccessResult {
    data object Owner : AccessResult()
    data object CoOwner : AccessResult()
    data object NoAccess : AccessResult()
    data object Unknown : AccessResult()
}

class VerifyPetAccessUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(pet: Pet): AccessResult {
        val currentUserId = userRepository.getCurrentUserId() ?: return AccessResult.Unknown

        if (pet.profile.ownerUserId == currentUserId) {
            return AccessResult.Owner
        }

        val pairId = pet.profile.currentPairId ?: return AccessResult.NoAccess

        val pairResult = pairRepository.getPair(pairId)
        if (pairResult is DomainResult.Success) {
            val pair = pairResult.data
            if (pair != null &&
                pair.status == PairStatus.ACTIVE &&
                (pair.userId1 == currentUserId || pair.userId2 == currentUserId)
            ) {
                return AccessResult.CoOwner
            }
        }

        return AccessResult.NoAccess
    }
}
