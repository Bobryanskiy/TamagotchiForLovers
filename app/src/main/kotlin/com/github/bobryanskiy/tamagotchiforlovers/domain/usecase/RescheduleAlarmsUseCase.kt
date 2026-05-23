package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.core.notification.PetAlarmManager
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RescheduleAlarmsUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val alarmManager: PetAlarmManager
) {
    suspend operator fun invoke(): Boolean {
        return when (val result = petRepository.getAllActivePets()) {
            is DomainResult.Success -> {
                val pets = result.data
                if (pets.isEmpty()) {
                    return true
                }

                var allSuccess = true
                pets.forEach { pet ->
                    try {
                        alarmManager.scheduleCheck(pet.id, pet.lifeState)
                    } catch (e: Exception) {
                        allSuccess = false
                    }
                }
                allSuccess
            }
            is DomainResult.Failure -> false
        }
    }
}