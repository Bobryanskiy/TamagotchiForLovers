package com.github.bobryanskiy.tamagotchiforlovers.data.notifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("NotificationManager", "gfdg")
    }
}