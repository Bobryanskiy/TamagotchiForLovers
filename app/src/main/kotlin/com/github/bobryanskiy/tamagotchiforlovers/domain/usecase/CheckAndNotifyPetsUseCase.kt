package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.provider.StringResourceProvider
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckAndNotifyPetsUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val prepareNotificationUseCase: PreparePetNotificationUseCase
) {

    suspend operator fun invoke(stringProvider: StringResourceProvider): Boolean {
        val petsResult = petRepository.getAllActivePets()

        if (petsResult is DomainResult.Failure) {
            return false
        }

        val pets = petsResult.getOrNull() ?: return false
        if (pets.isEmpty()) {
            return true
        }

        var hasErrors = false

        pets.forEach { pet ->
            try {
                prepareNotificationUseCase.invoke(pet, stringProvider)
            } catch (e: Exception) {
                hasErrors = true
            }
        }

        return !hasErrors
    }
}