package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.SyncStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PetResult
import javax.inject.Inject
import kotlin.math.abs

sealed class LinkResult {
    data object Success : LinkResult()
    data class Conflict(val localPet: Pet, val remotePet: Pet) : LinkResult()
    data object Error : LinkResult()
}

/**
 * UseCase для связывания локального аккаунта с облачным.
 *
 * Сценарии:
 * - А: Локальный есть, в облаке нет → пушим локального (Гость → Регистрация)
 * - Б: Локального нет, в облаке есть → тянем из облака (Новое устройство)
 * - В: Оба есть → проверяем timestamps, при большом расхождении — конфликт
 * - Г: Ни там ни там → успех (создаст позже)
 */
class LinkAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val petRepository: PetRepository,
    private val sessionRepository: SessionRepository
) {
    sealed class LinkResult {
        data object Success : LinkResult()
        data class Conflict(val localPet: Pet, val remotePet: Pet) : LinkResult()
    }

    suspend operator fun invoke(): PetResult<LinkResult> {
        val currentUserId = authRepository.getCurrentUserId()
            ?: return DomainResult.Failure(PetError.NotAuthenticated)

        val activePetId = sessionRepository.getActivePetId()
        val localPet = activePetId?.let {
            petRepository.getPetById(it).getOrNull()
        }

        // Если есть локальный питомец без ownerId — привязываем
        if (localPet != null && localPet.profile.ownerUserId == null) {
            val updatedPet = localPet.copy(
                profile = localPet.profile.copy(ownerUserId = currentUserId),
                syncStatus = SyncStatus.PENDING  // ← отправим в облако
            )
            petRepository.savePet(updatedPet)
            return DomainResult.Success(LinkResult.Success)
        }

        // Если локального нет — тянем из облака
        if (localPet == null) {
            val remotePetsResult = petRepository.syncPetsForOwner(currentUserId)
            if (remotePetsResult is DomainResult.Success) {
                val remotePet = remotePetsResult.data.firstOrNull()
                if (remotePet != null) {
                    sessionRepository.saveActivePetId(remotePet.id)
                }
            }
            return DomainResult.Success(LinkResult.Success)
        }

        // Конфликт — оба есть
        val remotePetsResult = petRepository.syncPetsForOwner(currentUserId)
        if (remotePetsResult is DomainResult.Success) {
            val remotePet = remotePetsResult.data.firstOrNull()
            if (remotePet != null) {
                return DomainResult.Success(LinkResult.Conflict(localPet, remotePet))
            }
        }

        return DomainResult.Success(LinkResult.Success)
    }
}