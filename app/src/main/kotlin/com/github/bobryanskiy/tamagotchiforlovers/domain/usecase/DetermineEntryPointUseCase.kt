package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import javax.inject.Inject

class DetermineEntryPointUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
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
