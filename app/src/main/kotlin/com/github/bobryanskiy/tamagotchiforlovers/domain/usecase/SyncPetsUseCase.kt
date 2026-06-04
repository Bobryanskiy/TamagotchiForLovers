package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.SyncError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class SyncPetsUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(ownerId: String? = null): DomainResult<Int, SyncError> {
        if (ownerId != null) {
            return when (val result = petRepository.syncPetsForOwner(ownerId)) {
                is DomainResult.Success -> DomainResult.Success(result.data.size)
                is DomainResult.Failure -> DomainResult.Failure(SyncError.NetworkError)
            }
        }
        return petRepository.syncAllPending()
    }
}
