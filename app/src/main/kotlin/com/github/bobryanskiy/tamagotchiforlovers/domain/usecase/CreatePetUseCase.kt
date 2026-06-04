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
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.NameLimits
import javax.inject.Inject

class CreatePetUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
    private val idGenerator: IdGenerator
) {
    companion object {
        private const val INITIAL_STAT_VALUE = 80
    }


    suspend operator fun invoke(name: String): PetResult<String> {
        val trimmed = name.trim()

        if (trimmed.length !in NameLimits.PET_NAME_MIN..NameLimits.PET_NAME_MAX) {
            return DomainResult.Failure(PetError.InvalidInput)
        }

        val ownerId = authRepository.getCurrentUserId()
        val now = clock.currentTimeMillis()
        val id = idGenerator.generate()

        val newPet = Pet(
            id = id,
            profile = PetProfile(
                name = trimmed,
                ownerUserId = ownerId,
                currentPairId = null,
                createdAt = now,
                abandonedAt = null
            ),
            stats = PetStats(
                INITIAL_STAT_VALUE,
                INITIAL_STAT_VALUE,
                INITIAL_STAT_VALUE,
                INITIAL_STAT_VALUE,
                now),
            lifeState = PetLifeState(PetLifeStatus.NORMAL, null),
            syncStatus = SyncStatus.SYNCED
        )

        val saveResult = petRepository.createPet(newPet)
        if (saveResult is DomainResult.Failure) return saveResult

        sessionRepository.saveActivePetId(newPet.id)
        return DomainResult.Success(newPet.id)
    }
}
