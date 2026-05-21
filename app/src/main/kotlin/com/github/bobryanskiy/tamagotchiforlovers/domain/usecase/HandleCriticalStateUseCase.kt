package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HandleCriticalStateUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(petId: String): DomainResult<Pet> {
        val petResult = petRepository.getPetById(petId)

        if (petResult is DomainResult.Failure) {
            return petResult
        }

        val pet = petResult.getOrNull() ?: return DomainResult.Failure(PetError.PetNotFound)

        // Здесь можно добавить дополнительную бизнес-логику:
        // - Проверить, не истёк ли recoveryEndTime
        // - Обновить статус, если прошло достаточно времени
        // - Логировать событие

        return DomainResult.Success(pet)
    }
}