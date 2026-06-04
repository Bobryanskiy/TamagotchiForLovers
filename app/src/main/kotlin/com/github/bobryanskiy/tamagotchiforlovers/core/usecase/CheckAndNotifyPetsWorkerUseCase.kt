package com.github.bobryanskiy.tamagotchiforlovers.core.usecase

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.core.notification.NotificationHelper
import com.github.bobryanskiy.tamagotchiforlovers.core.notification.NotificationStringResolver
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SettingsRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.PreparePetNotificationUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Рабочий UseCase для проверки всех петов и показа уведомлений.
 *
 * Вызывается из CriticalStateWorker (WorkManager).
 *
 * Логика:
 * 1. Проверка settingsRepository — если уведомления выключены, выходим
 * 2. Получаем список всех активных петов
 * 3. Для каждого: PreparePetNotificationUseCase → PetNotification
 * 4. Если shouldShow=true → через NotificationStringResolver получаем строки
 * 5. Отправляем в NotificationHelper
 */
class CheckAndNotifyPetsWorkerUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val prepareNotification: PreparePetNotificationUseCase,
    private val stringResolver: NotificationStringResolver,
    private val notificationHelper: NotificationHelper,
    private val settingsRepository: SettingsRepository,
    private val logger: AppLogger
) {

    companion object {
        private const val TAG = "CheckAndNotifyPets"
    }

    suspend operator fun invoke() {
        val notificationsEnabled = settingsRepository.observeNotificationsEnabled().first()
        if (!notificationsEnabled) {
            logger.d(TAG, "🔕 Notifications disabled, skipping worker")
            return
        }

        val pets = petRepository.getAllActivePets().getOrNull() ?: run {
            logger.w(TAG, "No active pets to check")
            return
        }

        if (pets.isEmpty()) {
            logger.d(TAG, "No active pets, nothing to notify")
            return
        }

        logger.d(TAG, "🔍 Checking ${pets.size} pets for notifications")

        var notificationsShown = 0

        pets.forEach { pet ->
            val notification = prepareNotification(pet)

            if (!notification.shouldShow) {
                logger.d(TAG, "⏭️ Skipping ${pet.profile.name}: status=${notification.status}")
                return@forEach
            }

            val title = stringResolver.resolveTitle(notification)
            val message = stringResolver.resolveMessage(notification)

            notificationHelper.showPetNotification(
                NotificationHelper.NotificationData(
                    petId = notification.petId,
                    petName = notification.petName,
                    title = title,
                    message = message,
                    isUrgent = notification.isUrgent,
                    status = notification.status
                )
            )

            notificationsShown++
            logger.d(TAG, "🔔 Notified for ${pet.profile.name}: ${notification.key}")
        }

        logger.d(TAG, "✅ Worker done: $notificationsShown/${pets.size} notifications shown")
        return
    }
}
