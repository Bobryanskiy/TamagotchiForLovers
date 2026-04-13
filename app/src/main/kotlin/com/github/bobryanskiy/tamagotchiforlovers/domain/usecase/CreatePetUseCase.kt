package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import javax.inject.Inject

class CreatePetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(name: String, ownerUserId: String): Result<String> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Имя не может быть пустым"))
        if (ownerUserId.isBlank()) return Result.failure(IllegalArgumentException("ID владельца не указан"))

        return petRepository.createPet(name, ownerUserId)
    }
}