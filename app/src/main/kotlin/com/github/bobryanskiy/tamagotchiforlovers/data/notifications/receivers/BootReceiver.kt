package com.github.bobryanskiy.tamagotchiforlovers.data.notifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.bobryanskiy.tamagotchiforlovers.data.PetConstants.PetConstants.HUNGER_RATE
import com.github.bobryanskiy.tamagotchiforlovers.data.notifications.Notifications
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PetStorage
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            val petState = PetStorage(context).getPetState()
            Notifications.PetWantEat.schedule(context,
                max(0, (100 - petState.hunger) / HUNGER_RATE - (System.currentTimeMillis() - petState.lastUpdateTime).milliseconds.inWholeMinutes).toInt()
            )
        }
    }
}