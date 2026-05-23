package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckUserStatusUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): UserNavigationResult {
        val userId = userRepository.getCurrentUserId()

        return if (userId != null) {
            UserNavigationResult.Logged(userId)
        } else {
            UserNavigationResult.Unlogged
        }
    }
}

sealed class UserNavigationResult {
    data class Logged(val pairId: String) : UserNavigationResult()
    object Unlogged : UserNavigationResult()
}