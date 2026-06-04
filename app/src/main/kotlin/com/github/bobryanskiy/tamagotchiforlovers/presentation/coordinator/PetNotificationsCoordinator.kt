package com.github.bobryanskiy.tamagotchiforlovers.presentation.coordinator

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.core.notification.NotificationHelper
import com.github.bobryanskiy.tamagotchiforlovers.core.notification.PetAlarmManager
import com.github.bobryanskiy.tamagotchiforlovers.core.usecase.CheckAndNotifyPetsWorkerUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Координирует всю работу с уведомлениями и алармами для питомца.
 * ViewModel больше не знает про PetAlarmManager, NotificationHelper и SettingsRepository.
 */
class PetNotificationsCoordinator @Inject constructor(
    private val petAlarmManager: PetAlarmManager,
    private val notificationHelper: NotificationHelper,
    private val settingsRepository: SettingsRepository,
    private val checkAndNotifyPetsWorkerUseCase: CheckAndNotifyPetsWorkerUseCase,
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "PetNotificationsCoordinator"
    }

    fun observeNotificationsEnabled(): Flow<Boolean> =
        settingsRepository.observeNotificationsEnabled()

    fun scheduleAlarm(pet: Pet, notificationsEnabled: Boolean) {
        petAlarmManager.cancelCheck(pet.id)
        petAlarmManager.scheduleSmartAlarm(
            petId = pet.id,
            pet = pet,
            notificationsEnabled = notificationsEnabled
        )
    }

    fun cancelAlarm(petId: String) {
        petAlarmManager.cancelCheck(petId)
    }

    suspend fun showTestNotification(pet: Pet) {
        notificationHelper.showPetNotification(
            NotificationHelper.NotificationData(
                petId = pet.id,
                petName = pet.profile.name,
                title = "🧪 Тест",
                message = "Уведомления работают!",
                isUrgent = true,
                status = pet.lifeState.status
            )
        )
        logger.d(TAG, "🧪 Test notification sent for ${pet.id}")
    }

    suspend fun triggerManualCheck() {
        logger.d(TAG, "🧪 Manual check triggered")
        runCatching { checkAndNotifyPetsWorkerUseCase() }
            .onSuccess { logger.d(TAG, "✅ Manual check completed") }
            .onFailure { e -> logger.e(TAG, "❌ Manual check failed", e) }
    }
}
