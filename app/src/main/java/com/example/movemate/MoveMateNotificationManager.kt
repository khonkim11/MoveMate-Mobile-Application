package com.example.movemate

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.Calendar
import kotlin.math.abs

object MoveMateNotificationManager {

    const val EXTRA_USER_ID =
        "movemate_notification_user_id"

    const val EXTRA_REMINDER_TYPE =
        "movemate_reminder_type"

    const val CHANNEL_REMINDERS =
        "movemate_reminders"

    const val CHANNEL_ACHIEVEMENTS =
        "movemate_achievements"

    const val CHANNEL_SUMMARIES =
        "movemate_summaries"

    private const val REQUEST_WORKOUT = 1000
    private const val REQUEST_HYDRATION = 2000
    private const val REQUEST_INACTIVITY = 3000
    private const val REQUEST_WEEKLY = 4000

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT <
            Build.VERSION_CODES.O
        ) {
            return
        }

        val manager = context.getSystemService(
            NotificationManager::class.java
        )

        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDERS,
            "Fitness reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description =
                "Workout, water and activity reminders"
        }

        val achievementChannel = NotificationChannel(
            CHANNEL_ACHIEVEMENTS,
            "Achievements",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description =
                "Workout awards and goal-complete alerts"
        }

        val summaryChannel = NotificationChannel(
            CHANNEL_SUMMARIES,
            "Progress summaries",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description =
                "Weekly fitness progress reminders"
        }

        manager.createNotificationChannels(
            listOf(
                reminderChannel,
                achievementChannel,
                summaryChannel
            )
        )
    }

    fun scheduleAll(
        context: Context,
        userId: Int,
        settings: MoveMateNotificationSettings
    ) {
        cancelAll(context, userId)

        if (!settings.enabled || userId <= 0) {
            return
        }

        if (settings.workoutReminderEnabled) {
            scheduleWorkoutReminder(
                context,
                userId,
                settings
            )
        }

        if (settings.hydrationReminderEnabled) {
            scheduleHydrationReminder(
                context,
                userId
            )
        }

        if (settings.inactivityReminderEnabled) {
            scheduleInactivityReminder(
                context,
                userId
            )
        }

        if (settings.weeklySummaryEnabled) {
            scheduleWeeklySummary(
                context,
                userId
            )
        }
    }

    fun cancelAll(
        context: Context,
        userId: Int
    ) {
        MoveMateReminderType.values().forEach {
            cancelReminder(
                context = context,
                userId = userId,
                type = it
            )
        }
    }

    fun scheduleWorkoutReminder(
        context: Context,
        userId: Int,
        settings: MoveMateNotificationSettings
    ) {
        val triggerAt = nextDailyTimeMillis(
            settings.workoutHour,
            settings.workoutMinute
        )

        scheduleAlarm(
            context,
            userId,
            MoveMateReminderType.WORKOUT,
            triggerAt
        )
    }

    fun scheduleHydrationReminder(
        context: Context,
        userId: Int
    ) {
        scheduleAlarm(
            context,
            userId,
            MoveMateReminderType.HYDRATION,
            nextHydrationTimeMillis()
        )
    }

    fun scheduleInactivityReminder(
        context: Context,
        userId: Int
    ) {
        scheduleAlarm(
            context,
            userId,
            MoveMateReminderType.INACTIVITY,
            nextDailyTimeMillis(
                hour = 19,
                minute = 0
            )
        )
    }

    fun scheduleWeeklySummary(
        context: Context,
        userId: Int
    ) {
        scheduleAlarm(
            context,
            userId,
            MoveMateReminderType.WEEKLY_SUMMARY,
            nextSundayEveningMillis()
        )
    }

    private fun scheduleAlarm(
        context: Context,
        userId: Int,
        type: MoveMateReminderType,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        val pendingIntent =
            reminderPendingIntent(
                context,
                userId,
                type
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.M
        ) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun cancelReminder(
        context: Context,
        userId: Int,
        type: MoveMateReminderType
    ) {
        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        alarmManager.cancel(
            reminderPendingIntent(
                context,
                userId,
                type
            )
        )
    }

    private fun reminderPendingIntent(
        context: Context,
        userId: Int,
        type: MoveMateReminderType
    ): PendingIntent {
        val intent = Intent(
            context,
            MoveMateReminderReceiver::class.java
        ).apply {
            action = type.actionValue
            putExtra(EXTRA_USER_ID, userId)
            putExtra(
                EXTRA_REMINDER_TYPE,
                type.actionValue
            )
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode(userId, type),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun requestCode(
        userId: Int,
        type: MoveMateReminderType
    ): Int {
        val base = when (type) {
            MoveMateReminderType.WORKOUT ->
                REQUEST_WORKOUT

            MoveMateReminderType.HYDRATION ->
                REQUEST_HYDRATION

            MoveMateReminderType.INACTIVITY ->
                REQUEST_INACTIVITY

            MoveMateReminderType.WEEKLY_SUMMARY ->
                REQUEST_WEEKLY
        }

        return base + abs(userId % 900)
    }

    fun postReminder(
        context: Context,
        userId: Int,
        title: String,
        message: String,
        channelId: String = CHANNEL_REMINDERS
    ) {
        if (!canPostNotifications(context)) {
            return
        }

        createChannels(context)

        val contentIntent = PendingIntent.getActivity(
            context,
            userId + 7000,
            Intent(
                context,
                MainActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notification =
            NotificationCompat.Builder(
                context,
                channelId
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(
                    if (
                        channelId ==
                        CHANNEL_ACHIEVEMENTS
                    ) {
                        NotificationCompat.PRIORITY_HIGH
                    } else {
                        NotificationCompat.PRIORITY_DEFAULT
                    }
                )
                .build()

        val notificationManager =
            NotificationManagerCompat.from(context)

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        val notificationId =
            (
                    System.currentTimeMillis() %
                            Int.MAX_VALUE.toLong()
                    ).toInt()

        notificationManager.notify(
            notificationId,
            notification
        )

        addMoveMateNotificationLog(
            context = context,
            userId = userId,
            title = title,
            message = message
        )
    }

    fun postTestNotification(
        context: Context,
        userId: Int
    ) {
        postReminder(
            context = context,
            userId = userId,
            title = "MoveMate notifications are ready",
            message =
                "Workout reminders, hydration prompts and achievements can now appear here."
        )
    }

    fun recordWorkoutCompleted(
        context: Context,
        userId: Int
    ) {
        recordMoveMateWorkoutDate(
            context,
            userId
        )
    }

    fun notifyWorkoutCompleted(
        context: Context,
        userId: Int,
        activityName: String,
        calories: Double,
        awardTier: String,
        todayCalories: Double,
        goalTarget: Double
    ) {
        val settings =
            loadMoveMateNotificationSettings(
                context,
                userId
            )

        if (!settings.enabled) {
            return
        }

        if (
            settings.achievementAlertsEnabled &&
            !awardTier.equals(
                "STARTER",
                ignoreCase = true
            )
        ) {
            val emoji = when (
                awardTier.uppercase()
            ) {
                "SILVER" -> "🥈"
                "GOLD" -> "🥇"
                "DIAMOND" -> "💎"
                else -> "🏅"
            }

            postReminder(
                context = context,
                userId = userId,
                title =
                    "$emoji $awardTier award earned",
                message =
                    "You completed $activityName and burned " +
                            "${calories.toInt()} kcal.",
                channelId =
                    CHANNEL_ACHIEVEMENTS
            )
        }

        if (
            settings.goalCompleteAlertsEnabled &&
            goalTarget > 0.0 &&
            todayCalories >= goalTarget
        ) {
            postReminder(
                context = context,
                userId = userId,
                title = "🎯 Daily workout goal complete",
                message =
                    "You reached ${todayCalories.toInt()} of " +
                            "${goalTarget.toInt()} kcal today.",
                channelId =
                    CHANNEL_ACHIEVEMENTS
            )
        }
    }

    fun canPostNotifications(
        context: Context
    ): Boolean {
        return Build.VERSION.SDK_INT <
                Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission
                        .POST_NOTIFICATIONS
                ) == PackageManager
            .PERMISSION_GRANTED
    }

    private fun nextDailyTimeMillis(
        hour: Int,
        minute: Int
    ): Long {
        val now = Calendar.getInstance()

        return Calendar.getInstance().apply {
            set(
                Calendar.HOUR_OF_DAY,
                hour.coerceIn(0, 23)
            )
            set(
                Calendar.MINUTE,
                minute.coerceIn(0, 59)
            )
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
    }

    private fun nextHydrationTimeMillis(): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance()

        val hour = now.get(
            Calendar.HOUR_OF_DAY
        )

        when {
            hour < 8 -> {
                next.set(
                    Calendar.HOUR_OF_DAY,
                    8
                )
                next.set(Calendar.MINUTE, 0)
            }

            hour >= 18 -> {
                next.add(
                    Calendar.DAY_OF_YEAR,
                    1
                )
                next.set(
                    Calendar.HOUR_OF_DAY,
                    8
                )
                next.set(Calendar.MINUTE, 0)
            }

            else -> {
                next.add(
                    Calendar.HOUR_OF_DAY,
                    3
                )
                next.set(Calendar.MINUTE, 0)

                if (
                    next.get(
                        Calendar.HOUR_OF_DAY
                    ) > 20
                ) {
                    next.add(
                        Calendar.DAY_OF_YEAR,
                        1
                    )
                    next.set(
                        Calendar.HOUR_OF_DAY,
                        8
                    )
                }
            }
        }

        next.set(Calendar.SECOND, 0)
        next.set(Calendar.MILLISECOND, 0)

        return next.timeInMillis
    }

    private fun nextSundayEveningMillis(): Long {
        val now = Calendar.getInstance()

        return Calendar.getInstance().apply {
            set(
                Calendar.DAY_OF_WEEK,
                Calendar.SUNDAY
            )
            set(
                Calendar.HOUR_OF_DAY,
                19
            )
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }.timeInMillis
    }
}