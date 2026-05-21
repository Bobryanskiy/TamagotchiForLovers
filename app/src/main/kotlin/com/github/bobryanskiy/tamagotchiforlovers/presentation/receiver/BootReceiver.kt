package com.github.bobryanskiy.tamagotchiforlovers.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.RescheduleAlarmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rescheduleAlarmsUseCase: RescheduleAlarmsUseCase

    private val tag = "BootReceiver"

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            Log.d(tag, "Ignoring action: ${intent.action}")
            return
        }

        Log.d(tag, "Boot completed. Rescheduling alarms...")

        receiverScope.launch {
            try {
                rescheduleAlarmsUseCase.invoke()
                Log.d(tag, "Alarms rescheduled successfully")
            } catch (e: Exception) {
                Log.e(tag, "Critical error during alarm rescheduling", e)
            }
        }
    }
}