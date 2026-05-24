package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetProfile
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.SyncStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PetResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.IdGenerator
import javax.inject.Inject

/**
 * UseCase создания нового питомца.
 *
 * Создаёт доменную модель с начальными статами и сохраняет через Repository.
 * Если пользователь авторизован — привязывает питомца к его ownerId.
 */
class CreatePetUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository,  // можно убрать если не нужен ownerId
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
    private val idGenerator: IdGenerator
) {
    suspend operator fun invoke(name: String): PetResult<String> {
        if (name.isBlank()) {
            return DomainResult.Failure(PetError.InvalidInput)
        }

        val ownerId = authRepository.getCurrentUserId()
        val now = clock.currentTimeMillis()
        val id = idGenerator.generate()

        val newPet = Pet(
            id = id,
            profile = PetProfile(
                name = name.trim(),
                ownerUserId = ownerId,
                currentPairId = null,
                createdAt = now,
                abandonedAt = null
            ),
            stats = PetStats(
                hunger = 80, energy = 80,
                cleanliness = 80, happiness = 80,
                updatedAt = now
            ),
            lifeState = PetLifeState(
                status = PetLifeStatus.NORMAL,
                isActionsBlocked = false,
                recoveryEndTime = null,
                decayMultiplier = 1.0f
            ),
            syncStatus = if (ownerId == null) SyncStatus.LOCAL_ONLY else SyncStatus.SYNCED
        )

        val saveResult = petRepository.createPet(newPet)
        if (saveResult is DomainResult.Failure) return saveResult

        sessionRepository.saveActivePetId(newPet.id)
        return DomainResult.Success(newPet.id)
    }
}