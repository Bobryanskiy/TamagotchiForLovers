package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PetResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckExistingPetUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(ownerId: String): PetResult<String?> {
        return when (val petsResult = petRepository.getAllPetsByOwner(ownerId)) {
            is DomainResult.Success -> {
                val pets = petsResult.data
                val firstPet = pets.firstOrNull()

                if (firstPet != null) {
                    sessionRepository.saveActivePetId(firstPet.id)
                    DomainResult.Success(firstPet.id)
                } else {
                    DomainResult.Success(null)
                }
            }
            is DomainResult.Failure -> DomainResult.Failure(petsResult.error)
        }
    }
}