package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PetResult
import javax.inject.Inject
import kotlin.math.abs

sealed class LinkAccountResult {
    data object Success : LinkAccountResult()
    data class Conflict(val localPet: Pet, val remotePet: Pet) : LinkAccountResult()
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
    suspend operator fun invoke(): PetResult<LinkAccountResult> {
        val currentUserId = authRepository.getCurrentUserId()
            ?: return DomainResult.Failure(PetError.NotAuthenticated)

        val activePetId = sessionRepository.getActivePetId()
        val localPet = activePetId?.let { petRepository.getPetById(it).getOrNull() }

        val remotePetsResult = petRepository.syncPetsForOwner(currentUserId)
        if (remotePetsResult is DomainResult.Failure) {
            return DomainResult.Failure(remotePetsResult.error)
        }
        val remotePet = (remotePetsResult as DomainResult.Success).data.firstOrNull()

        return when {
            // Сценарий А: Только локальный → пушим в облако
            localPet != null && remotePet == null -> {
                val updatedPet = localPet.copy(
                    profile = localPet.profile.copy(ownerUserId = currentUserId)
                )
                petRepository.savePet(updatedPet)
                DomainResult.Success(LinkAccountResult.Success)
            }

            // Сценарий Б: Только remote → используем его
            localPet == null && remotePet != null -> {
                sessionRepository.saveActivePetId(remotePet.id)
                DomainResult.Success(LinkAccountResult.Success)
            }

            // Сценарий В: КОНФЛИКТ — оба есть
            localPet != null && remotePet != null -> {
                val timeDiff = abs(localPet.stats.updatedAt - remotePet.stats.updatedAt)
                if (timeDiff < 60_000L) {
                    // Разница < 5 сек — берём свежее
                    val winner = if (remotePet.stats.updatedAt > localPet.stats.updatedAt)
                        remotePet else localPet
                    petRepository.savePet(winner)
                    DomainResult.Success(LinkAccountResult.Success)
                } else {
                    // Большая разница — пусть пользователь выбирает
                    DomainResult.Success(LinkAccountResult.Conflict(localPet, remotePet))
                }
            }

            else -> DomainResult.Success(LinkAccountResult.Success)
        }
    }
}
