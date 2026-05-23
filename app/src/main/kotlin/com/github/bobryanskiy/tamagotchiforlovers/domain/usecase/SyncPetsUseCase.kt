package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import javax.inject.Inject

/**
 * UseCase для синхронизации локальных изменений питомцев с удалённым сервером.
 *
 * Этот UseCase инкапсулирует логику синхронизации, позволяя Presentation слою
 * запускать синхронизацию без прямой зависимости от реализаций слоя Data.
 * Соответствует принципу Dependency Inversion и правилам Clean Architecture.
 *
 * @param petRepository Репозиторий питомцев из доменного слоя
 */
class SyncPetsUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    /**
     * Запускает процесс синхронизации всех ожидающих изменений питомцев.
     * Делегирует вызов репозиторию, который содержит реализацию через PetSyncManager.
     *
     * @return true если все изменения успешно синхронизированы, false в случае ошибок
     */
    suspend operator fun invoke(): Boolean {
        return petRepository.syncPendingChanges()
    }
}