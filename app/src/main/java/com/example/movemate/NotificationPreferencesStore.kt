package com.example.movemate

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val NOTIFICATION_PREFS =
    "movemate_notification_preferences"

private const val KNOWN_USERS_KEY =
    "known_notification_user_ids"

private fun notificationKey(
    userId: Int,
    name: String
): String {
    return "user_${userId}_$name"
}

fun loadMoveMateNotificationSettings(
    context: Context,
    userId: Int
): MoveMateNotificationSettings {
    val prefs = context.getSharedPreferences(
        NOTIFICATION_PREFS,
        Context.MODE_PRIVATE
    )

    return MoveMateNotificationSettings(
        enabled = prefs.getBoolean(
            notificationKey(userId, "enabled"),
            false
        ),
        workoutReminderEnabled = prefs.getBoolean(
            notificationKey(
                userId,
                "workout_reminder_enabled"
            ),
            false
        ),
        workoutHour = prefs.getInt(
            notificationKey(userId, "workout_hour"),
            18
        ).coerceIn(0, 23),
        workoutMinute = prefs.getInt(
            notificationKey(userId, "workout_minute"),
            0
        ).coerceIn(0, 59),
        hydrationReminderEnabled = prefs.getBoolean(
            notificationKey(
                userId,
                "hydration_reminder_enabled"
            ),
            false
        ),
        inactivityReminderEnabled = prefs.getBoolean(
            notificationKey(
                userId,
                "inactivity_reminder_enabled"
            ),
            false
        ),
        weeklySummaryEnabled = prefs.getBoolean(
            notificationKey(
                userId,
                "weekly_summary_enabled"
            ),
            false
        ),
        achievementAlertsEnabled = prefs.getBoolean(
            notificationKey(
                userId,
                "achievement_alerts_enabled"
            ),
            true
        ),
        goalCompleteAlertsEnabled = prefs.getBoolean(
            notificationKey(
                userId,
                "goal_complete_alerts_enabled"
            ),
            true
        )
    )
}

fun saveMoveMateNotificationSettings(
    context: Context,
    userId: Int,
    settings: MoveMateNotificationSettings
) {
    if (userId <= 0) {
        return
    }

    val prefs = context.getSharedPreferences(
        NOTIFICATION_PREFS,
        Context.MODE_PRIVATE
    )

    prefs.edit()
        .putBoolean(
            notificationKey(userId, "enabled"),
            settings.enabled
        )
        .putBoolean(
            notificationKey(
                userId,
                "workout_reminder_enabled"
            ),
            settings.workoutReminderEnabled
        )
        .putInt(
            notificationKey(userId, "workout_hour"),
            settings.workoutHour
        )
        .putInt(
            notificationKey(userId, "workout_minute"),
            settings.workoutMinute
        )
        .putBoolean(
            notificationKey(
                userId,
                "hydration_reminder_enabled"
            ),
            settings.hydrationReminderEnabled
        )
        .putBoolean(
            notificationKey(
                userId,
                "inactivity_reminder_enabled"
            ),
            settings.inactivityReminderEnabled
        )
        .putBoolean(
            notificationKey(
                userId,
                "weekly_summary_enabled"
            ),
            settings.weeklySummaryEnabled
        )
        .putBoolean(
            notificationKey(
                userId,
                "achievement_alerts_enabled"
            ),
            settings.achievementAlertsEnabled
        )
        .putBoolean(
            notificationKey(
                userId,
                "goal_complete_alerts_enabled"
            ),
            settings.goalCompleteAlertsEnabled
        )
        .apply()

    val knownUsers = prefs.getStringSet(
        KNOWN_USERS_KEY,
        emptySet()
    ).orEmpty().toMutableSet()

    knownUsers.add(userId.toString())

    prefs.edit()
        .putStringSet(
            KNOWN_USERS_KEY,
            knownUsers
        )
        .apply()
}

fun loadKnownNotificationUserIds(
    context: Context
): List<Int> {
    return context.getSharedPreferences(
        NOTIFICATION_PREFS,
        Context.MODE_PRIVATE
    )
        .getStringSet(
            KNOWN_USERS_KEY,
            emptySet()
        )
        .orEmpty()
        .mapNotNull(String::toIntOrNull)
        .filter { it > 0 }
}

fun recordMoveMateWorkoutDate(
    context: Context,
    userId: Int
) {
    context.getSharedPreferences(
        NOTIFICATION_PREFS,
        Context.MODE_PRIVATE
    )
        .edit()
        .putString(
            notificationKey(
                userId,
                "last_workout_date"
            ),
            currentMoveMateDate()
        )
        .apply()
}

fun hasMoveMateWorkoutToday(
    context: Context,
    userId: Int
): Boolean {
    val savedDate = context.getSharedPreferences(
        NOTIFICATION_PREFS,
        Context.MODE_PRIVATE
    )
        .getString(
            notificationKey(
                userId,
                "last_workout_date"
            ),
            ""
        )
        .orEmpty()

    return savedDate == currentMoveMateDate()
}

private fun currentMoveMateDate(): String {
    return SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    ).format(Date())
}

fun addMoveMateNotificationLog(
    context: Context,
    userId: Int,
    title: String,
    message: String
) {
    val prefs = context.getSharedPreferences(
        NOTIFICATION_PREFS,
        Context.MODE_PRIVATE
    )

    val key = notificationKey(
        userId,
        "notification_log"
    )

    val existing = try {
        JSONArray(
            prefs.getString(key, "[]")
                .orEmpty()
        )
    } catch (_: Exception) {
        JSONArray()
    }

    val updated = JSONArray()

    updated.put(
        JSONObject().apply {
            put(
                "id",
                System.currentTimeMillis()
            )
            put("title", title)
            put("message", message)
            put(
                "timestamp",
                System.currentTimeMillis()
            )
        }
    )

    for (
    index in 0 until
            minOf(existing.length(), 19)
    ) {
        updated.put(existing.optJSONObject(index))
    }

    prefs.edit()
        .putString(
            key,
            updated.toString()
        )
        .apply()
}

fun loadMoveMateNotificationLog(
    context: Context,
    userId: Int
): List<MoveMateNotificationLogEntry> {
    val raw = context.getSharedPreferences(
        NOTIFICATION_PREFS,
        Context.MODE_PRIVATE
    )
        .getString(
            notificationKey(
                userId,
                "notification_log"
            ),
            "[]"
        )
        .orEmpty()

    val array = try {
        JSONArray(raw)
    } catch (_: Exception) {
        JSONArray()
    }

    return buildList {
        for (index in 0 until array.length()) {
            val item =
                array.optJSONObject(index) ?: continue

            add(
                MoveMateNotificationLogEntry(
                    id = item.optLong(
                        "id",
                        index.toLong()
                    ),
                    title = item.optString(
                        "title",
                        "MoveMate"
                    ),
                    message = item.optString(
                        "message",
                        ""
                    ),
                    timestampMillis =
                        item.optLong(
                            "timestamp",
                            0L
                        )
                )
            )
        }
    }
}

fun clearMoveMateNotificationLog(
    context: Context,
    userId: Int
) {
    context.getSharedPreferences(
        NOTIFICATION_PREFS,
        Context.MODE_PRIVATE
    )
        .edit()
        .remove(
            notificationKey(
                userId,
                "notification_log"
            )
        )
        .apply()
}