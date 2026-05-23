package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.UserError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PetResult
import javax.inject.Inject
import javax.inject.Singleton

sealed class LinkResult {
    object Success : LinkResult()
    data class Conflict(val localPet: Pet, val remotePet: Pet) : LinkResult()
    object Error : LinkResult()
}

@Singleton
class LinkAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val petRepository: PetRepository,
    private val sessionRepository: SessionRepository
) {

    suspend operator fun invoke(): PetResult<LinkResult> {
        val currentUserId = authRepository.getCurrentUserId()
            ?: return DomainResult.Failure(PetError.NotAuthenticated)

        // 1. Получаем локального питомца (если есть активный в сессии)
        val activePetId = sessionRepository.getActivePetId()
        val localPet = if (activePetId != null) {
            petRepository.getPetById(activePetId).getOrNull()
        } else {
            null
        }

        // 2. Синхронизируем/получаем питомцев из облака для этого юзера
        val remotePetsResult = petRepository.syncPetsForOwner(currentUserId)

        if (remotePetsResult is DomainResult.Failure) {
            return DomainResult.Failure(remotePetsResult.error)
        }

        val remotePet = (remotePetsResult as DomainResult.Success).data.firstOrNull()

        return when {
            // Сценарий А: Есть только локальный (Гость -> Регистрация)
            localPet != null && remotePet == null -> {
                val updatedPet = localPet.copy(profile = localPet.profile.copy(ownerUserId = currentUserId))
                petRepository.savePet(updatedPet) // Сохранит в Room и запушит в Firebase
                sessionRepository.saveActivePetId(updatedPet.id)
                DomainResult.Success(LinkResult.Success)
            }

            // Сценарий Б: Есть только удаленный (Новое устройство / После очистки локалки)
            localPet == null && remotePet != null -> {
                // Он уже сохранен в Room внутри syncPetsForOwner
                sessionRepository.saveActivePetId(remotePet.id)
                DomainResult.Success(LinkResult.Success)
            }

            // Сценарий В: КОНФЛИКТ (Есть и там, и там)
            localPet != null && remotePet != null -> {
                val timeDiff = Math.abs(localPet.stats.updatedAt - remotePet.stats.updatedAt)

                // Если разница небольшая (< 5 сек), считаем это рассинхроном сети и берем свежее
                if (timeDiff < 5000L) {
                    val winner = if (remotePet.stats.updatedAt > localPet.stats.updatedAt) remotePet else localPet
                    petRepository.savePet(winner)
                    sessionRepository.saveActivePetId(winner.id)
                    DomainResult.Success(LinkResult.Success)
                } else {
                    // Разница большая. Требуется выбор пользователя.
                    DomainResult.Success(LinkResult.Conflict(localPet, remotePet))
                }
            }

            // Нет питомцев нигде
            else -> {
                DomainResult.Success(LinkResult.Success)
            }
        }
    }
}