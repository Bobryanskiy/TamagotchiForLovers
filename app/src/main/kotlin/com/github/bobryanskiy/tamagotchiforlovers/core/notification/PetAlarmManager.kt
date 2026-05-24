package com.github.bobryanskiy.tamagotchiforlovers.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.github.bobryanskiy.tamagotchiforlovers.BuildConfig
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
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
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "PetAlarmManager"
        private const val CRITICAL_THRESHOLD = 15
        private const val WARNING_THRESHOLD = 30

        /** Сколько единиц стата теряется в минуту (примерно, при decay_multiplier=1.0) */
        private const val DECAY_PER_MINUTE = 1.0

        /** В DEBUG режиме аларм всегда через 30 секунд для быстрого теста */
        private const val DEBUG_INTERVAL_MS = 30_000L

        /** Если все статы высокие — профилактический аларм через 1 час */
        private const val DEFAULT_INTERVAL_MS = 60 * 60 * 1000L

        /** Минимум 1 минута между алармами (чтобы не спамить) */
        private const val MIN_INTERVAL_MS = 60_000L
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Планирует "умный" аларм на основе прогноза когда статы упадут.
     *
     * Логика:
     * 1. В DEBUG — всегда через 30 секунд (для быстрого теста)
     * 2. В PROD — рассчитываем когда худший стат достигнет WARNING (30) или CRITICAL (15)
     * 3. Берём самое раннее время
     * 4. Ставим setAlarmClock → появится иконка ⏰ в status bar
     *
     * @param pet — полный пет для анализа статов
     * @param notificationsEnabled — флаг из SettingsRepository (передаётся из ViewModel)
     */
    fun scheduleSmartAlarm(
        petId: String,
        pet: Pet,
        notificationsEnabled: Boolean
    ) {
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
            currentTime + DEBUG_INTERVAL_MS
        } else {
            val warningTime = calculateTimeToThreshold(pet, WARNING_THRESHOLD, currentTime)
            val criticalTime = calculateTimeToThreshold(pet, CRITICAL_THRESHOLD, currentTime)

            val smartInterval = (minOfNotNull(warningTime, criticalTime)
                ?: (currentTime + DEFAULT_INTERVAL_MS)) - currentTime

            currentTime + smartInterval.coerceAtLeast(MIN_INTERVAL_MS)
        }

        val interval = (triggerTime - currentTime).coerceAtLeast(MIN_INTERVAL_MS)
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

    /**
     * Рассчитывает когда худший стат достигнет порога.
     *
     * @return timestamp когда это произойдёт, или null если все статы уже ниже порога
     *         (значит аларм должен быть СЕЙЧАС, но мы всё равно вернём currentTime)
     */
    private fun calculateTimeToThreshold(
        pet: Pet,
        threshold: Int,
        currentTime: Long
    ): Long {
        val stats = pet.stats
        val decayMultiplier = pet.lifeState.decayMultiplier.toDouble().coerceAtLeast(1.0)

        val worstStat = minOf(stats.hunger, stats.energy, stats.cleanliness, stats.happiness)

        // Уже ниже порога — аларм должен быть сейчас
        if (worstStat <= threshold) return currentTime

        // Сколько единиц нужно потерять
        val pointsToLose = worstStat - threshold

        // Время в минутах с учётом множителя скорости (SICK = 1.5x быстрее)
        val minutesToThreshold = pointsToLose / (DECAY_PER_MINUTE * decayMultiplier)

        return currentTime + (minutesToThreshold * 60 * 1000).toLong()
    }

    private fun minOfNotNull(vararg values: Long?): Long? {
        return values.filterNotNull().minOrNull()
    }

    /**
     * Отменяет все алармы для пета.
     * Вызывается при:
     * - Отключении уведомлений
     * - Терминальном состоянии (DEAD, ESCAPED)
     * - Потере доступа к пету (кик, logout)
     * - Удалении пета
     */
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