package com.example.movemate

data class MoveMateNotificationSettings(
    val enabled: Boolean = false,
    val workoutReminderEnabled: Boolean = false,
    val workoutHour: Int = 18,
    val workoutMinute: Int = 0,
    val hydrationReminderEnabled: Boolean = false,
    val inactivityReminderEnabled: Boolean = false,
    val weeklySummaryEnabled: Boolean = false,
    val achievementAlertsEnabled: Boolean = true,
    val goalCompleteAlertsEnabled: Boolean = true
)

data class MoveMateNotificationLogEntry(
    val id: Long,
    val title: String,
    val message: String,
    val timestampMillis: Long
)

enum class MoveMateReminderType(
    val actionValue: String
) {
    WORKOUT("workout_reminder"),
    HYDRATION("hydration_reminder"),
    INACTIVITY("inactivity_reminder"),
    WEEKLY_SUMMARY("weekly_summary")
}