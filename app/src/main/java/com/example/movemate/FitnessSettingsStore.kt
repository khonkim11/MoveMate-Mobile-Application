package com.example.movemate

import android.content.Context

data class FitnessAppSettings(
    val useMetricUnits: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val workoutRemindersEnabled: Boolean = true,
    val weeklySummaryEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

private const val FITNESS_SETTINGS_PREFS =
    "movemate_fitness_settings"

private fun userSettingKey(
    userId: Int,
    settingName: String
): String {
    return "user_${userId}_$settingName"
}

fun loadFitnessAppSettings(
    context: Context,
    userId: Int
): FitnessAppSettings {
    val prefs = context.getSharedPreferences(
        FITNESS_SETTINGS_PREFS,
        Context.MODE_PRIVATE
    )

    return FitnessAppSettings(
        useMetricUnits = prefs.getBoolean(
            userSettingKey(userId, "metric_units"),
            true
        ),
        notificationsEnabled = prefs.getBoolean(
            userSettingKey(userId, "notifications"),
            true
        ),
        workoutRemindersEnabled = prefs.getBoolean(
            userSettingKey(userId, "workout_reminders"),
            true
        ),
        weeklySummaryEnabled = prefs.getBoolean(
            userSettingKey(userId, "weekly_summary"),
            true
        ),
        soundEnabled = prefs.getBoolean(
            userSettingKey(userId, "sound"),
            true
        ),
        vibrationEnabled = prefs.getBoolean(
            userSettingKey(userId, "vibration"),
            true
        )
    )
}

fun saveFitnessAppSettings(
    context: Context,
    userId: Int,
    settings: FitnessAppSettings
) {
    context
        .getSharedPreferences(
            FITNESS_SETTINGS_PREFS,
            Context.MODE_PRIVATE
        )
        .edit()
        .putBoolean(
            userSettingKey(userId, "metric_units"),
            settings.useMetricUnits
        )
        .putBoolean(
            userSettingKey(userId, "notifications"),
            settings.notificationsEnabled
        )
        .putBoolean(
            userSettingKey(userId, "workout_reminders"),
            settings.workoutRemindersEnabled
        )
        .putBoolean(
            userSettingKey(userId, "weekly_summary"),
            settings.weeklySummaryEnabled
        )
        .putBoolean(
            userSettingKey(userId, "sound"),
            settings.soundEnabled
        )
        .putBoolean(
            userSettingKey(userId, "vibration"),
            settings.vibrationEnabled
        )
        .apply()
}