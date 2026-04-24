package com.github.bobryanskiy.tamagotchiforlovers.data.notification

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.AlertSchedule

interface NotificationScheduler {
    suspend fun scheduleAlerts(schedules: List<AlertSchedule>)
    fun cancelAlertsForPet(petId: String)
}