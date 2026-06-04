package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.core.notification.PetAlarmManager
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RescheduleAlarmsUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val petAlarmManager: PetAlarmManager,
    private val settingsRepository: SettingsRepository,
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "RescheduleAlarmsUseCase"
    }

    suspend operator fun invoke(): Boolean {
        // Проверка настроек перед перепланированием
        val notificationsEnabled = settingsRepository.observeNotificationsEnabled().first()
        if (!notificationsEnabled) {
            logger.d(TAG, "🔕 Notifications disabled, skipping alarm reschedule")
            return true  // успех, просто ничего не делаем
        }

        val pets = petRepository.getAllActivePets().getOrNull() ?: run {
            logger.w(TAG, "No active pets to reschedule")
            return false
        }

        logger.d(TAG, "Rescheduling alarms for ${pets.size} pets")

        pets.forEach { pet ->
            petAlarmManager.scheduleSmartAlarm(
                petId = pet.id,
                pet = pet,
                notificationsEnabled = true
            )
        }

        return true
    }
}
