package com.github.bobryanskiy.tamagotchiforlovers.core.work

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.core.notification.NotificationHelper
import com.github.bobryanskiy.tamagotchiforlovers.core.notification.NotificationStringResolver
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.PreparePetNotificationUseCase
import javax.inject.Inject

/**
 * Составной use case: проверка всех петов + показ уведомлений.
 * Живёт в core.work потому что зависит от Android (NotificationHelper).
 */
class CheckAndNotifyPetsWorkerUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val prepareNotification: PreparePetNotificationUseCase,
    private val stringResolver: NotificationStringResolver,
    private val notificationHelper: NotificationHelper,
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "CheckAndNotifyPets"
    }

    suspend operator fun invoke(): Boolean {
        val result = petRepository.getAllActivePets()
        if (result !is DomainResult.Success) {
            logger.e(TAG, "Failed to get active pets")
            return false
        }

        result.data.forEach { pet ->
            try {
                val notification = prepareNotification(pet)
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
            } catch (e: Exception) {
                logger.e(TAG, "Failed to notify for pet ${pet.id}", e)
            }
        }
        return true
    }
}