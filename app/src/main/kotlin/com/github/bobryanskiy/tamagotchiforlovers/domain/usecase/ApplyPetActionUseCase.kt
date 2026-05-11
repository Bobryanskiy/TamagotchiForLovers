package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class ApplyPetActionUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(petId: String, petAction: PetAction): DomainResult<Unit> {
        if (petId.isBlank()) return DomainResult.Failure(PetError.InvalidInput)

        return petRepository.applyAction(petId, petAction)
    }
}