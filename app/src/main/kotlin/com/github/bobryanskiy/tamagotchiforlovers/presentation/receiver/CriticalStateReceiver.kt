package com.github.bobryanskiy.tamagotchiforlovers.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.core.notification.NotificationHelper
import com.github.bobryanskiy.tamagotchiforlovers.core.notification.NotificationStringResolver
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.PreparePetNotificationUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CriticalStateReceiver : BroadcastReceiver() {

    @Inject lateinit var petRepository: PetRepository
    @Inject lateinit var prepareNotificationUseCase: PreparePetNotificationUseCase
    @Inject lateinit var notificationStringResolver: NotificationStringResolver
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var logger: AppLogger

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "CriticalStateReceiver"
        const val EXTRA_PET_ID = "extra_pet_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        val petId = intent.getStringExtra(EXTRA_PET_ID)
        if (petId == null) {
            pendingResult.finish()
            return
        }

        logger.d(TAG, "📥 onReceive for pet: $petId")

        receiverScope.launch {
            try {
                when (val result = petRepository.getPetById(petId)) {
                    is DomainResult.Success -> {
                        val pet = result.data
                        if (pet == null) {
                            logger.w(TAG, "Pet $petId not found in DB")
                            return@launch
                        }

                        val notification = prepareNotificationUseCase(pet)
                        if (!notification.shouldShow) {
                            logger.d(TAG, "⏭️ Notification not needed for ${pet.profile.name}")
                            return@launch
                        }

                        val title = notificationStringResolver.resolveTitle(notification)
                        val message = notificationStringResolver.resolveMessage(notification)

                        logger.d(TAG, "🔔 Showing notification: $title")

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

                        logger.d(TAG, "✅ Notification shown successfully")
                    }
                    is DomainResult.Failure -> {
                        logger.w(TAG, "Failed to get pet $petId: ${result.error}")
                    }
                }
            } catch (e: Exception) {
                logger.e(TAG, "Receiver error", e)
            } finally {
                // ✅ ОБЯЗАТЕЛЬНО: завершаем ресивер после работы
                pendingResult.finish()
            }
        }
    }
}
