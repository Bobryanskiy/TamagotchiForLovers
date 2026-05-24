package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import javax.inject.Inject

class DetermineEntryPointUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val pairRepository: PairRepository
) {
    sealed class EntryPoint {
        data object Main : EntryPoint()
        data class Pet(val petId: String) : EntryPoint()
    }

    suspend operator fun invoke(): EntryPoint {
        val activePetId = sessionRepository.getActivePetId()
        return if (activePetId != null) {
            EntryPoint.Pet(activePetId)
        } else {
            EntryPoint.Main
        }
    }
}