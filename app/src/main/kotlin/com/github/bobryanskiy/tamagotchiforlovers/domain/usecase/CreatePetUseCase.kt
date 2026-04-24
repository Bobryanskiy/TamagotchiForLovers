package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class CreatePetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(name: String, ownerUserId: String): DomainResult<String> {
        if (name.isBlank() || ownerUserId.isBlank()) return DomainResult.Failure(PetError.InvalidInput)

        return petRepository.createPet(name.trim(), ownerUserId)
    }
}