package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import android.util.Log
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

    private val tag = "RescheduleAlarmsUseCase"

    suspend operator fun invoke(): Boolean {
        return when (val result = petRepository.getAllActivePets()) {
            is DomainResult.Success -> {
                val pets = result.data
                if (pets.isEmpty()) {
                    Log.d(tag, "No active pets found")
                    return true
                }

                var allSuccess = true
                pets.forEach { pet ->
                    try {
                        alarmManager.scheduleCheck(pet.id, pet.lifeState)
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to schedule for ${pet.id}", e)
                        allSuccess = false
                    }
                }
                allSuccess
            }
            is DomainResult.Failure -> {
                Log.e(tag, "Failed to load pets: ${result.error}")
                false
            }
        }
    }
}