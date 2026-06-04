package com.github.bobryanskiy.tamagotchiforlovers.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.github.bobryanskiy.tamagotchiforlovers.BuildConfig
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.BalanceConfigRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.github.bobryanskiy.tamagotchiforlovers.presentation.MainActivity
import com.github.bobryanskiy.tamagotchiforlovers.presentation.receiver.CriticalStateReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetAlarmManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val clock: Clock,
    private val balanceConfigRepository: BalanceConfigRepository,
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "PetAlarmManager"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleSmartAlarm(
        petId: String,
        pet: Pet,
        notificationsEnabled: Boolean
    ) {
        val config = balanceConfigRepository.get()

        if (!notificationsEnabled) {
            cancelCheck(petId)
            return
        }

        if (pet.lifeState.isTerminal()) {
            cancelCheck(petId)
            logger.d(TAG, "🗑️ Cancelled alarms for terminal pet: $petId")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && !alarmManager.canScheduleExactAlarms()) {
            logger.w(TAG, "⚠️ No exact alarm permission for $petId")
            return
        }

        val currentTime = clock.currentTimeMillis()

        val triggerTime = if (BuildConfig.DEBUG) {
            currentTime + config.debugAlarmIntervalMs
        } else {
            val warningTime = calculateTimeToThreshold(
                pet = pet,
                threshold = config.warningThreshold,
                currentTime = currentTime
            )
            val criticalTime = calculateTimeToThreshold(
                pet = pet,
                threshold = config.criticalThreshold,
                currentTime = currentTime
            )

            val smartInterval = (minOfNotNull(warningTime, criticalTime)
                ?: (currentTime + config.defaultAlarmIntervalMs)) - currentTime

            currentTime + smartInterval.coerceAtLeast(config.minAlarmIntervalMs)
        }

        val interval = (triggerTime - currentTime).coerceAtLeast(config.minAlarmIntervalMs)
        val finalTriggerTime = currentTime + interval

        logger.d(TAG, "⏰ Smart alarm for $petId in ${interval / 1000}s")

        val broadcastIntent = Intent(context, CriticalStateReceiver::class.java).apply {
            putExtra(CriticalStateReceiver.EXTRA_PET_ID, petId)
            action = "ACTION_CHECK_PET_$petId"
        }
        val alarmPendingIntent = PendingIntent.getBroadcast(
            context,
            "${petId}_critical".hashCode(),
            broadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("petId", petId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            "${petId}_show".hashCode(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmInfo = AlarmManager.AlarmClockInfo(finalTriggerTime, showPendingIntent)
            alarmManager.setAlarmClock(alarmInfo, alarmPendingIntent)
            logger.d(TAG, "✅ Alarm clock set for $petId at $finalTriggerTime")
        } catch (e: SecurityException) {
            logger.e(TAG, "❌ Failed to set alarm clock for $petId: ${e.message}", e)
        }
    }

    private fun calculateTimeToThreshold(
        pet: Pet,
        threshold: Int,
        currentTime: Long
    ): Long {
        val config = balanceConfigRepository.get()
        val stats = pet.stats

        val hungerPointsToLose = (stats.hunger - threshold).coerceAtLeast(0)
        val hungerSeconds = (hungerPointsToLose * config.secondsPerHungerPoint)

        val energyPointsToLose = (stats.energy - threshold).coerceAtLeast(0)
        val energySeconds = (energyPointsToLose * config.secondsPerEnergyPoint)

        val cleanlinessPointsToLose = (stats.cleanliness - threshold).coerceAtLeast(0)
        val cleanlinessSeconds = (cleanlinessPointsToLose * config.secondsPerCleanlinessPoint)

        val happinessPointsToLose = (stats.happiness - threshold).coerceAtLeast(0)
        val happinessSeconds = (happinessPointsToLose * config.secondsPerHappinessPoint)

        val minSeconds = minOf(hungerSeconds, energySeconds, cleanlinessSeconds, happinessSeconds)

        logger.d(TAG, "petId: ${pet.id}, " +
                "hunger=${hungerSeconds}s, energy=${energySeconds}s, " +
                "clean=${cleanlinessSeconds}s, happy=${happinessSeconds}s")

        return currentTime + (minSeconds * 1000)
    }

    private fun minOfNotNull(vararg values: Long?): Long? {
        return values.filterNotNull().minOrNull()
    }

    fun cancelCheck(petId: String) {
        val intent = Intent(context, CriticalStateReceiver::class.java).apply {
            action = "ACTION_CHECK_PET_$petId"
        }
        val requestCode = "${petId}_critical".hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            logger.d(TAG, "🗑️ Cancelled alarm for $petId")
        }
    }
}
