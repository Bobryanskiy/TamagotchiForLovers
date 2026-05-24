package com.github.bobryanskiy.tamagotchiforlovers.domain.model

data class PetNotification(
    val petId: String,
    val petName: String,
    val titleKey: NotificationKey,
    val messageKey: NotificationKey,
    val isUrgent: Boolean,
    val status: PetLifeStatus
)