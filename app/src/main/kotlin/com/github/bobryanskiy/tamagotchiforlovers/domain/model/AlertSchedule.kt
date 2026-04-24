package com.github.bobryanskiy.tamagotchiforlovers.domain.model

data class AlertSchedule(
    val petId: String,
    val alertType: PetAlertType,
    val triggerAtMillis: Long
)
