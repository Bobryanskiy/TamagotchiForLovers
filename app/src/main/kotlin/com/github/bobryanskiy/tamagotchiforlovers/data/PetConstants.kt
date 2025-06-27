package com.github.bobryanskiy.tamagotchiforlovers.data

import java.util.Calendar

// TODO : сделать с помощью RemoteConfig
class PetConstants {
    object PetConstants {
        // Пороги для уведомлений
        const val HUNGER_THRESHOLD = 80
        const val TIREDNESS_THRESHOLD = 70
        const val HAPPINESS_THRESHOLD = 20
        const val HEALTH_THRESHOLD = 30

        // Скорость изменения параметров (в единицах/минуту)
        const val HUNGER_RATE = 4
        const val TIREDNESS_RATE = 4
        const val HAPPINESS_RATE = -2
        const val HEALTH_RATE = -1

        // Скорость изменения во сне
        const val HUNGER_SLEEP_RATE = 8f
        const val TIREDNESS_SLEEP_RATE = -10
        const val HAPPINESS_SLEEP_RATE = -1
        const val HEALTH_SLEEP_RATE = 0

        // Пороги смерти
        val HUNGER_DEATH = Calendar.getInstance().add(Calendar.HOUR, 5)
        val TIREDNESS_DEATH = 95
        val HEALTH_DEATH = 10
        val DEATH_HOURS_THRESHOLD = 24

        // Возраст
        const val MAX_AGE = 80
        const val AGE_INCREMENT_RATE = 24 * 60 * 60 * 1000L  // +1 год в день
    }
}