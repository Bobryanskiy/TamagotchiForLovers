package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.data.local.AppSessionStorage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DetermineEntryPointUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    private val sessionStorage: AppSessionStorage
) {
    suspend operator fun invoke(): DomainEntryPoint = withContext(Dispatchers.IO) {
        if (auth.currentUser == null) return@withContext DomainEntryPoint.Auth

        val petId = sessionStorage.activePetId.first()
        return@withContext if (petId != null) {
            DomainEntryPoint.Pet(petId)
        } else {
            DomainEntryPoint.Main
        }
    }
}