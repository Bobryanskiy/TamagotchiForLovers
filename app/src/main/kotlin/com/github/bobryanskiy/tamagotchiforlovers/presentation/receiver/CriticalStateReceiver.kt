package com.github.bobryanskiy.tamagotchiforlovers.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.bobryanskiy.tamagotchiforlovers.domain.provider.StringResourceProvider
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.PreparePetNotificationUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CriticalStateReceiver @Inject constructor(
    private val petRepository: PetRepository,
    private val prepareNotificationUseCase: PreparePetNotificationUseCase,
    private val stringProvider: StringResourceProvider
) : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val EXTRA_PET_ID = "extra_pet_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val petId = intent.getStringExtra(EXTRA_PET_ID)

        if (petId == null) {
            return
        }

        receiverScope.launch {
            try {
                when (val result = petRepository.getPetById(petId)) {
                    is DomainResult.Success -> {
                        val pet = result.data
                        if (pet != null) {
                            prepareNotificationUseCase.invoke(pet, stringProvider)
                        }
                    }
                    is DomainResult.Failure -> {
                        // Log error silently or use proper logging interface
                    }
                }
            } catch (e: Exception) {
                // Handle exception silently or use proper logging interface
            }
        }
    }
}