package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String) {
        userRepository.createUser(uid)
    }
}