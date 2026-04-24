package com.github.bobryanskiy.tamagotchiforlovers.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.local.AppSessionStorage
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CalculatePetAlertsUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.EvaluatePetCriticalStateUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalStatus
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BootEntryPoint {
    fun sessionStorage(): AppSessionStorage
    fun petRepository(): PetRepository
    fun calculateAlerts(): CalculatePetAlertsUseCase
    fun scheduler(): NotificationScheduler
    fun evaluateCritical(): EvaluatePetCriticalStateUseCase
}

class PetBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Реагируем на загрузку, разблокировку и обновление приложения
        if (intent.action !in listOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_LOCKED_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED
            )) return

        Log.d("TAMAGOTCHI", "BootReceiver: System booted. Restoring alarms...")

        val entryPoint = EntryPointAccessors.fromApplication(context, BootEntryPoint::class.java)
        val storage = entryPoint.sessionStorage()
        val petRepo = entryPoint.petRepository()
        val calc = entryPoint.calculateAlerts()
        val scheduler = entryPoint.scheduler()
        val eval = entryPoint.evaluateCritical()

        runBlocking {
            try {
                val petId = storage.activePetId.first()
                if (petId == null) {
                    Log.d("TAMAGOTCHI", "BootReceiver: No active pet. Skipping.")
                    return@runBlocking
                }

                scheduler.cancelAlertsForPet(petId)

                val pet = withTimeoutOrNull(3000L) { petRepo.observePet(petId).first() }

                if (pet == null) {
                    Log.w("TAMAGOTCHI", "BootReceiver: Pet data unavailable. Will restore on app open.")
                    return@runBlocking
                }

                val state = eval(pet.stats)

                if (state.status == PetCriticalStatus.DEAD || state.status == PetCriticalStatus.ESCAPED) {
                    Log.i("TAMAGOTCHI", "BootReceiver: Pet is ${state.status}. No alarms needed. UI will show Game Over.")
                    return@runBlocking
                }

                if (state.status == PetCriticalStatus.COLLAPSED) {
                    Log.i("TAMAGOTCHI", "BootReceiver: Pet is sleeping. No alarms until wake up.")
                    return@runBlocking
                }

                val schedules = calc(pet)
                scheduler.scheduleAlerts(schedules)

                Log.i("TAMAGOTCHI", "BootReceiver: Successfully restored ${schedules.size} alerts for pet $petId")

            } catch (e: Exception) {
                Log.e("TAMAGOTCHI", "BootReceiver: Failed to restore alarms", e)
            }
        }
    }
}