package com.github.bobryanskiy.tamagotchiforlovers.data.notifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.bobryanskiy.tamagotchiforlovers.data.PetConstants.PetConstants.HUNGER_RATE
import com.github.bobryanskiy.tamagotchiforlovers.data.notifications.Notifications
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PetStorage

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            Notifications.PetWantEat.schedule(context, (100 - PetStorage(context).getPetState().hunger) / HUNGER_RATE)
        }
    }
}