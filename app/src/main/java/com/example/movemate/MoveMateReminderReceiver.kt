package com.example.movemate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MoveMateReminderReceiver :
    BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val userId = intent.getIntExtra(
            MoveMateNotificationManager
                .EXTRA_USER_ID,
            0
        )

        if (userId <= 0) {
            return
        }

        val settings =
            loadMoveMateNotificationSettings(
                context,
                userId
            )

        if (!settings.enabled) {
            MoveMateNotificationManager
                .cancelAll(
                    context,
                    userId
                )
            return
        }

        val typeValue =
            intent.getStringExtra(
                MoveMateNotificationManager
                    .EXTRA_REMINDER_TYPE
            )
                ?: intent.action
                ?: return

        val type =
            MoveMateReminderType.values()
                .firstOrNull {
                    it.actionValue == typeValue
                }
                ?: return

        when (type) {
            MoveMateReminderType.WORKOUT -> {
                if (
                    settings
                        .workoutReminderEnabled
                ) {
                    MoveMateNotificationManager
                        .postReminder(
                            context,
                            userId,
                            "🏃 Time for your workout",
                            "Open MoveMate and continue toward today's fitness goal."
                        )

                    MoveMateNotificationManager
                        .scheduleWorkoutReminder(
                            context,
                            userId,
                            settings
                        )
                }
            }

            MoveMateReminderType.HYDRATION -> {
                if (
                    settings
                        .hydrationReminderEnabled
                ) {
                    MoveMateNotificationManager
                        .postReminder(
                            context,
                            userId,
                            "💧 Hydration reminder",
                            "Drink some water and keep your body ready for movement."
                        )

                    MoveMateNotificationManager
                        .scheduleHydrationReminder(
                            context,
                            userId
                        )
                }
            }

            MoveMateReminderType.INACTIVITY -> {
                if (
                    settings
                        .inactivityReminderEnabled
                ) {
                    if (
                        !hasMoveMateWorkoutToday(
                            context,
                            userId
                        )
                    ) {
                        MoveMateNotificationManager
                            .postReminder(
                                context,
                                userId,
                                "🚶 Add some movement today",
                                "No completed workout is recorded today. A short walk still counts."
                            )
                    }

                    MoveMateNotificationManager
                        .scheduleInactivityReminder(
                            context,
                            userId
                        )
                }
            }

            MoveMateReminderType.WEEKLY_SUMMARY -> {
                if (
                    settings
                        .weeklySummaryEnabled
                ) {
                    MoveMateNotificationManager
                        .postReminder(
                            context,
                            userId,
                            "📊 Your weekly fitness review",
                            "Open Progress to review workouts, calories, streaks and your best day.",
                            MoveMateNotificationManager
                                .CHANNEL_SUMMARIES
                        )

                    MoveMateNotificationManager
                        .scheduleWeeklySummary(
                            context,
                            userId
                        )
                }
            }
        }
    }
}

class MoveMateBootReceiver :
    BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (
            intent.action !=
            Intent.ACTION_BOOT_COMPLETED
        ) {
            return
        }

        MoveMateNotificationManager
            .createChannels(context)

        loadKnownNotificationUserIds(
            context
        ).forEach { userId ->
            val settings =
                loadMoveMateNotificationSettings(
                    context,
                    userId
                )

            MoveMateNotificationManager
                .scheduleAll(
                    context,
                    userId,
                    settings
                )
        }
    }
}