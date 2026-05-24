package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PetResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import javax.inject.Inject

/**
 * Применяет действие к питомцу: обновляет статы и критическое состояние.
 *
 * Порядок:
 * 1. Получаем пета
 * 2. Проверяем блокировку действий
 * 3. Применяем действие (PetStats.applyAction)
 * 4. Оцениваем новое состояние (EvaluatePetCriticalStateUseCase)
 * 5. Сохраняем изменения
 */
class ApplyPetActionUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val evaluateStateUseCase: EvaluatePetCriticalStateUseCase,
    private val calculateLiveStatsUseCase: CalculateLiveStatsUseCase,
    private val clock: Clock
) {
    suspend operator fun invoke(petId: String, action: PetAction): PetResult<Unit> {
        // 1. Получаем пета
        val pet = when (val result = petRepository.getPetById(petId)) {
            is DomainResult.Success -> result.data ?: return DomainResult.Failure(PetError.PetNotFound)
            is DomainResult.Failure -> return result
        }

        // 2. Проверяем блокировку
        if (pet.lifeState.isActionsBlocked) {
            return DomainResult.Failure(PetError.ActionBlocked)
        }

        // 3. Вычисляем новые статы и состояние
        val currentTime = clock.currentTimeMillis()

        val livePet = calculateLiveStatsUseCase(pet, currentTime)

        val newStats = livePet.stats.applyAction(action, currentTime)
        val newState = evaluateStateUseCase(newStats, currentTime)

        // 4. Обновляем статы
        when (val statsResult = petRepository.updateStats(
            petId = petId,
            hunger = newStats.hunger,
            energy = newStats.energy,
            cleanliness = newStats.cleanliness,
            happiness = newStats.happiness
        )) {
            is DomainResult.Failure -> return statsResult
            is DomainResult.Success -> Unit  // продолжаем
        }

        // 5. Обновляем критическое состояние
        return petRepository.updateCriticalState(petId, newState)
    }
}