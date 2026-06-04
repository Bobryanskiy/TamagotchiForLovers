package com.github.bobryanskiy.tamagotchiforlovers.presentation.coordinator

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.AccessResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.VerifyPetAccessUseCase
import javax.inject.Inject

/**
 * Управляет доступом пользователя к питомцу.
 * Инкапсулирует проверку прав и очистку при потере доступа.
 */
class PetAccessController @Inject constructor(
    private val verifyPetAccessUseCase: VerifyPetAccessUseCase,
    private val petRepository: PetRepository,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "PetAccessController"
    }

    suspend fun ensureAccess(pet: Pet): Boolean {
        return when (verifyPetAccessUseCase(pet)) {
            AccessResult.Owner -> {
                logger.d(TAG, "✅ User is owner of pet ${pet.id}")
                true
            }
            AccessResult.CoOwner -> {
                logger.d(TAG, "✅ User is co-owner of pet ${pet.id}")
                true
            }
            AccessResult.NoAccess -> {
                logger.w(TAG, "⚠️ User lost access to pet ${pet.id}, cleaning up")
                cleanup(pet.id)
                false
            }
            AccessResult.Unknown -> {
                logger.w(TAG, "⏸️ User ID unknown, keeping pet ${pet.id} alive")
                true
            }
        }
    }

    private suspend fun cleanup(petId: String) {
        runCatching { petRepository.deletePet(petId) }
        val currentUserId = userRepository.getCurrentUserId()
        if (currentUserId != null) {
            runCatching {
                userRepository.updateUserSession(
                    uid = currentUserId,
                    petId = null,
                    pairId = null
                )
                logger.d(TAG, "✅ User session cleaned in Firestore: uid=$currentUserId")
            }.onFailure { e ->
                logger.e(TAG, "❌ Failed to clean user session", e)
            }
        }
        sessionRepository.clearActivePetId()
        sessionRepository.clearActivePairId()
        sessionRepository.clearPairStatus()
    }
}
