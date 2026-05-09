package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DetermineEntryPointUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(): DomainEntryPoint = withContext(Dispatchers.IO) {
        // 1. Проверяем авторизацию (локально, мгновенно)
        val isLoggedIn = authRepository.isLoggedIn()
        if (!isLoggedIn) return@withContext DomainEntryPoint.Auth

        // 2. Проверяем наличие питомца в Room (локально, мгновенно)
        val petId = petRepository.getLocalPetId()
        if (petId != null) return@withContext DomainEntryPoint.Pet(petId)

        // 3. Пользователь залогинен, но питомца нет → на главный экран
        return@withContext DomainEntryPoint.Main
    }
}