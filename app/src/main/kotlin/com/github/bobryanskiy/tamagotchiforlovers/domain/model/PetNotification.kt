package com.github.bobryanskiy.tamagotchiforlovers.domain.model

data class PetNotification(
    val petId: String,
    val petName: String,
    val key: NotificationKey,
    val isUrgent: Boolean,
    val status: PetLifeStatus,
    val shouldShow: Boolean
) {
//    val shouldShow: Boolean
//        get() {
//            return status in listOf(PetLifeStatus.DEAD, PetLifeStatus.ESCAPED,
//                PetLifeStatus.SICK, PetLifeStatus.COLLAPSED)
//        }
}