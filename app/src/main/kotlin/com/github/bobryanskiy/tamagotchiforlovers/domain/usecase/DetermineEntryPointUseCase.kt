package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetermineEntryPointUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val petRepository: PetRepository,
    private val pairRepository: PairRepository
) {
    suspend operator fun invoke(): EntryPoint {
        val activePetId = sessionRepository.getActivePetId() ?: return EntryPoint.Main

        val pet = when (val petResult = petRepository.getPetById(activePetId)) {
            is com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult.Success -> petResult.data
            else -> null
        }

        if (pet == null) {
            sessionRepository.clearActivePetId()
            return EntryPoint.Main
        }

        val activePairId = sessionRepository.getActivePairId()
        var validPairId: String? = null

        if (activePairId != null) {
            val pair = pairRepository.getPair(activePairId)
            if (pair?.status == com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus.ACTIVE) {
                validPairId = activePairId
            } else {
                sessionRepository.clearActivePairId()
            }
        }

        return EntryPoint.Pet(activePetId, validPairId ?: "")
    }

    sealed class EntryPoint {
        object Main : EntryPoint()
        data class Pet(val petId: String, val pairId: String) : EntryPoint()
    }
}