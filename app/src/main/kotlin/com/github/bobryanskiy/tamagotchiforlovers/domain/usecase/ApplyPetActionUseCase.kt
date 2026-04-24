package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class ApplyPetActionUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(pet: Pet, petAction: PetAction): DomainResult<Unit> {
        if (pet.id.isBlank()) return DomainResult.Failure(PetError.InvalidInput)
        val allowed = when (petAction) {
            PetAction.Feed -> pet.stats.hunger < 100
            PetAction.Rest -> pet.stats.energy < 100
            PetAction.Clean -> pet.stats.cleanliness < 100
            PetAction.Play -> pet.stats.happiness < 100 && pet.stats.energy >= 10
        }

        if (!allowed) return DomainResult.Failure(PetError.ActionNotAllowed)

        return petRepository.applyAction(pet.id, petAction)
    }
}