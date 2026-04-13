package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ApplyPetActionUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(petId: String, pet: Pet, petAction: PetAction): Result<Unit> {
        val canProceed = when (petAction) {
            PetAction.Feed -> pet.stats.hunger < 100
            PetAction.Rest -> pet.stats.energy < 100
            PetAction.Clean -> pet.stats.cleanliness < 100
            PetAction.Play -> pet.stats.happiness < 100 && pet.stats.energy >= 10
        }

        if (!canProceed) return Result.failure(IllegalArgumentException("Невозможно выполнить действие: недостаточно ресурсов или параметр на максимуме"))

        return petRepository.applyPetAction(petId, petAction)
    }
}