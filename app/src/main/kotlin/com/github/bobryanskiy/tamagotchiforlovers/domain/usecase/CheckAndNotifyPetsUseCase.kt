package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckAndNotifyPetsUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val prepareNotificationUseCase: PreparePetNotificationUseCase
) {

    private val tag = "CheckAndNotifyPetsUseCase"

    suspend operator fun invoke(): Boolean {
        val petsResult = petRepository.getAllActivePets()

        if (petsResult is DomainResult.Failure) {
            Log.e(tag, "Failed to load active pets: ${petsResult.error}")
            return false
        }

        val pets = petsResult.getOrNull() ?: return false
        if (pets.isEmpty()) {
            Log.d(tag, "No active pets to check")
            return true
        }

        Log.d(tag, "Checking ${pets.size} active pets for notifications")

        var hasErrors = false

        pets.forEach { pet ->
            try {
                prepareNotificationUseCase.invoke(pet)
            } catch (e: Exception) {
                Log.e(tag, "Failed to process notification for pet: ${pet.id}", e)
                hasErrors = true
            }
        }

        return !hasErrors
    }
}