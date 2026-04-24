package com.github.bobryanskiy.tamagotchiforlovers.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAlertType

class PetAlertReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PET_ID = "petId"
        const val EXTRA_ALERT_TYPE = "alertType"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val petId = intent.getStringExtra(EXTRA_PET_ID)
        val alertTypeName = intent.getStringExtra(EXTRA_ALERT_TYPE)

        if (petId == null || alertTypeName == null) {
            Log.w("TAMAGOTCHI","PetAlertReceiver: Missing extras. Aborting.")
            return
        }

        val alertType = try {
            PetAlertType.valueOf(alertTypeName)
        } catch (e: IllegalArgumentException) {
            Log.e("TAMAGOTCHI", "PetAlertReceiver: Invalid alert type: $alertTypeName", e)
            return
        }

        Log.d("TAMAGOTCHI","PetAlertReceiver: Triggering notification | petId=$petId | type=$alertType")
        NotificationHelper.showNotification(context, petId, alertType)
    }
}