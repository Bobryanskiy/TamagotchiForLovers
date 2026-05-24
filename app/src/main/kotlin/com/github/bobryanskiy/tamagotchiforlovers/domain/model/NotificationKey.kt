package com.github.bobryanskiy.tamagotchiforlovers.domain.model

sealed class NotificationKey {
    data object Dead : NotificationKey()
    data object Escaped : NotificationKey()
    data object CriticalHunger : NotificationKey()
    data object CriticalEnergy : NotificationKey()
    data object CriticalCleanliness : NotificationKey()
    data object CriticalHappiness : NotificationKey()
    data object WarningHunger : NotificationKey()
    data object WarningEnergy : NotificationKey()
    data object WarningCleanliness : NotificationKey()
    data object WarningHappiness : NotificationKey()
}