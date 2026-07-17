package com.example.movemate

import android.annotation.SuppressLint
import android.app.Notification
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class MoveMateAlertItem(
    val id: Long,
    val userId: Int,
    val eventKey: String,
    val title: String,
    val message: String,
    val emoji: String,
    val createdAtMillis: Long,
    val isRead: Boolean
)

object MoveMateAlertStore {

    private const val PREFERENCES_NAME =
        "movemate_alert_center"

    private const val KEY_ITEMS =
        "alert_items"

    private const val MAX_ITEMS =
        120

    val items =
        androidx.compose.runtime
            .mutableStateListOf<MoveMateAlertItem>()

    private var loaded =
        false

    fun ensureLoaded(
        context: Context
    ) {
        if (
            loaded
        ) {
            return
        }

        loaded =
            true

        val raw =
            context
                .getSharedPreferences(
                    PREFERENCES_NAME,
                    Context.MODE_PRIVATE
                )
                .getString(
                    KEY_ITEMS,
                    "[]"
                )
                .orEmpty()

        runCatching {
            val array =
                JSONArray(
                    raw
                )

            val restored =
                mutableListOf<MoveMateAlertItem>()

            for (
            index in
            0 until
                    array.length()
            ) {
                val item =
                    array.optJSONObject(
                        index
                    ) ?: continue

                restored.add(
                    MoveMateAlertItem(
                        id =
                            item.optLong(
                                "id",
                                0L
                            ),
                        userId =
                            item.optInt(
                                "user_id",
                                0
                            ),
                        eventKey =
                            item.optString(
                                "event_key",
                                ""
                            ),
                        title =
                            item.optString(
                                "title",
                                "MoveMate"
                            ),
                        message =
                            item.optString(
                                "message",
                                ""
                            ),
                        emoji =
                            item.optString(
                                "emoji",
                                "🔔"
                            ),
                        createdAtMillis =
                            item.optLong(
                                "created_at",
                                System.currentTimeMillis()
                            ),
                        isRead =
                            item.optBoolean(
                                "is_read",
                                false
                            )
                    )
                )
            }

            items.clear()

            items.addAll(
                restored.sortedByDescending {
                    it.createdAtMillis
                }
            )
        }
    }

    fun addIfMissing(
        context: Context,
        item: MoveMateAlertItem
    ): Boolean {
        ensureLoaded(
            context
        )

        if (
            items.any {
                it.eventKey ==
                        item.eventKey &&
                        it.userId ==
                        item.userId
            }
        ) {
            return false
        }

        items.add(
            0,
            item
        )

        while (
            items.size >
            MAX_ITEMS
        ) {
            items.removeAt(
                items.lastIndex
            )
        }

        persist(
            context
        )

        return true
    }

    fun unreadCount(
        userId: Int
    ): Int {
        return items.count {
            it.userId ==
                    userId &&
                    !it.isRead
        }
    }

    fun forUser(
        userId: Int
    ): List<MoveMateAlertItem> {
        return items.filter {
            it.userId ==
                    userId
        }
    }

    fun markRead(
        context: Context,
        id: Long
    ) {
        val index =
            items.indexOfFirst {
                it.id ==
                        id
            }

        if (
            index <
            0
        ) {
            return
        }

        items[index] =
            items[index].copy(
                isRead =
                    true
            )

        persist(
            context
        )
    }

    fun markAllRead(
        context: Context,
        userId: Int
    ) {
        for (
        index in
        items.indices
        ) {
            val item =
                items[index]

            if (
                item.userId ==
                userId &&
                !item.isRead
            ) {
                items[index] =
                    item.copy(
                        isRead =
                            true
                    )
            }
        }

        persist(
            context
        )
    }

    fun clearUser(
        context: Context,
        userId: Int
    ) {
        val remaining =
            items.filterNot {
                it.userId ==
                        userId
            }

        items.clear()

        items.addAll(
            remaining
        )

        persist(
            context
        )
    }

    private fun persist(
        context: Context
    ) {
        val array =
            JSONArray()

        items.forEach {
                item ->

            array.put(
                JSONObject()
                    .put(
                        "id",
                        item.id
                    )
                    .put(
                        "user_id",
                        item.userId
                    )
                    .put(
                        "event_key",
                        item.eventKey
                    )
                    .put(
                        "title",
                        item.title
                    )
                    .put(
                        "message",
                        item.message
                    )
                    .put(
                        "emoji",
                        item.emoji
                    )
                    .put(
                        "created_at",
                        item.createdAtMillis
                    )
                    .put(
                        "is_read",
                        item.isRead
                    )
            )
        }

        context
            .getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                KEY_ITEMS,
                array.toString()
            )
            .apply()
    }
}

object MoveMateAlertManager {

    private const val ALERT_CHANNEL_ID =
        "movemate_workout_alerts"

    private const val LIVE_CHANNEL_ID =
        "movemate_live_workout"

    private const val LIVE_NOTIFICATION_BASE =
        31_000

    fun createChannels(
        context: Context
    ) {
        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.O
        ) {
            return
        }

        val manager =
            context.getSystemService(
                NotificationManager::class.java
            )

        val alertChannel =
            NotificationChannel(
                ALERT_CHANNEL_ID,
                "Workout alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    description =
                        "Quick workout, halfway and goal alerts."

                    enableVibration(
                        true
                    )
                }

        val liveChannel =
            NotificationChannel(
                LIVE_CHANNEL_ID,
                "Live workout",
                NotificationManager.IMPORTANCE_LOW
            )
                .apply {
                    description =
                        "Ongoing workout time, distance and calories."
                }

        manager.createNotificationChannel(
            alertChannel
        )

        manager.createNotificationChannel(
            liveChannel
        )
    }

    fun postStoredAlert(
        context: Context,
        userId: Int,
        eventKey: String,
        title: String,
        message: String,
        emoji: String
    ) {
        createChannels(
            context
        )

        val now =
            System.currentTimeMillis()

        val added =
            MoveMateAlertStore
                .addIfMissing(
                    context =
                        context,
                    item =
                        MoveMateAlertItem(
                            id =
                                now,
                            userId =
                                userId,
                            eventKey =
                                eventKey,
                            title =
                                title,
                            message =
                                message,
                            emoji =
                                emoji,
                            createdAtMillis =
                                now,
                            isRead =
                                false
                        )
                )

        if (
            !added ||
            !canPostNotifications(
                context
            )
        ) {
            return
        }

        val alertNotification: Notification =
            NotificationCompat
                .Builder(
                    context,
                    ALERT_CHANNEL_ID
                )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    "$emoji $title"
                )
                .setContentText(
                    message
                )
                .setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(
                            message
                        )
                )
                .setContentIntent(
                    appPendingIntent(
                        context,
                        eventKey.hashCode()
                    )
                )
                .setAutoCancel(
                    true
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setCategory(
                    NotificationCompat.CATEGORY_REMINDER
                )
                .setColor(
                    0xFF2563EB.toInt()
                )
                .build()

        showNotificationSafely(
            context =
                context,
            notificationId =
                eventKey.hashCode(),
            notification =
                alertNotification
        )

    }

    fun updateLiveWorkout(
        context: Context,
        workoutId: String,
        activityName: String,
        levelName: String,
        elapsedMilliseconds: Long,
        distanceKm: Double,
        calories: Double,
        running: Boolean
    ) {
        createChannels(
            context
        )

        if (
            !canPostNotifications(
                context
            )
        ) {
            return
        }

        val text =
            buildString {
                append(
                    formatAlertDuration(
                        elapsedMilliseconds
                    )
                )

                if (
                    distanceKm >
                    0.0
                ) {
                    append(
                        " • "
                    )

                    append(
                        formatAlertDistance(
                            distanceKm
                        )
                    )
                }

                append(
                    " • "
                )

                append(
                    String.format(
                        Locale.US,
                        "%.2f kcal",
                        calories
                    )
                )
            }

        val liveNotification: Notification =
            NotificationCompat
                .Builder(
                    context,
                    LIVE_CHANNEL_ID
                )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    "$activityName • $levelName"
                )
                .setContentText(
                    text
                )
                .setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(
                            if (
                                running
                            ) {
                                "Workout active • $text"
                            } else {
                                "Workout paused • $text"
                            }
                        )
                )
                .setContentIntent(
                    appPendingIntent(
                        context,
                        liveNotificationId(
                            workoutId
                        )
                    )
                )
                .setOngoing(
                    running
                )
                .setOnlyAlertOnce(
                    true
                )
                .setPriority(
                    NotificationCompat.PRIORITY_LOW
                )
                .setCategory(
                    NotificationCompat.CATEGORY_PROGRESS
                )
                .setColor(
                    0xFF2563EB.toInt()
                )
                .build()

        showNotificationSafely(
            context =
                context,
            notificationId =
                liveNotificationId(
                    workoutId
                ),
            notification =
                liveNotification
        )

    }

    fun cancelLiveWorkout(
        context: Context,
        workoutId: String
    ) {
        NotificationManagerCompat
            .from(
                context
            )
            .cancel(
                liveNotificationId(
                    workoutId
                )
            )
    }

    @SuppressLint(
        "MissingPermission"
    )
    private fun showNotificationSafely(
        context: Context,
        notificationId: Int,
        notification: Notification
    ) {
        if (
            !canPostNotifications(
                context
            )
        ) {
            return
        }

        NotificationManagerCompat
            .from(
                context
            )
            .notify(
                notificationId,
                notification
            )
    }

    private fun canPostNotifications(
        context: Context
    ): Boolean {
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return NotificationManagerCompat
            .from(
                context
            )
            .areNotificationsEnabled()
    }

    private fun appPendingIntent(
        context: Context,
        requestCode: Int
    ): PendingIntent {
        val intent =
            Intent(
                context,
                MainActivity::class.java
            )
                .apply {
                    flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP

                    putExtra(
                        "open_notifications",
                        true
                    )
                }

        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun liveNotificationId(
        workoutId: String
    ): Int {
        return LIVE_NOTIFICATION_BASE +
                kotlin.math.abs(
                    workoutId.hashCode() %
                            20_000
                )
    }
}

@Composable
fun MoveMateAlertPermissionEffect() {
    val context =
        LocalContext.current

    val launcher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestPermission()
        ) {
            /*
             * The user controls notification permission.
             */
        }

    LaunchedEffect(
        Unit
    ) {
        MoveMateAlertManager
            .createChannels(
                context
            )

        MoveMateAlertStore
            .ensureLoaded(
                context
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }
}

@Composable
fun WorkoutAlertEffects(
    userId: Int,
    workoutId: String,
    activityName: String,
    activityEmoji: String,
    levelName: String,
    targetSeconds: Int,
    elapsedMilliseconds: Long,
    distanceKm: Double,
    calories: Double,
    isRunning: Boolean,
    isFinished: Boolean
) {
    val context =
        LocalContext.current

    val latestRunning by
    rememberUpdatedState(
        isRunning
    )

    val latestDistanceKm by
    rememberUpdatedState(
        distanceKm
    )

    val latestCalories by
    rememberUpdatedState(
        calories
    )


    val targetMilliseconds =
        targetSeconds
            .coerceAtLeast(
                1
            )
            .toLong() *
                1_000L

    val progress =
        (
                elapsedMilliseconds
                    .toDouble() /
                        targetMilliseconds
                            .toDouble()
                )
            .coerceIn(
                0.0,
                1.0
            )

    LaunchedEffect(
        workoutId,
        isRunning
    ) {
        if (
            isRunning
        ) {
            MoveMateAlertManager
                .postStoredAlert(
                    context =
                        context,
                    userId =
                        userId,
                    eventKey =
                        "$workoutId-started",
                    title =
                        "$activityName started",
                    message =
                        "$levelName workout is active. Target: " +
                                formatAlertDuration(
                                    targetMilliseconds
                                ),
                    emoji =
                        activityEmoji
                )
        }
    }

    LaunchedEffect(
        workoutId,
        progress >=
                0.5
    ) {
        if (
            progress >=
            0.5
        ) {
            MoveMateAlertManager
                .postStoredAlert(
                    context =
                        context,
                    userId =
                        userId,
                    eventKey =
                        "$workoutId-halfway",
                    title =
                        "Halfway completed",
                    message =
                        "$activityName is 50% complete. Keep moving!",
                    emoji =
                        "⚡"
                )
        }
    }

    LaunchedEffect(
        workoutId,
        progress >=
                1.0
    ) {
        if (
            progress >=
            1.0
        ) {
            MoveMateAlertManager
                .postStoredAlert(
                    context =
                        context,
                    userId =
                        userId,
                    eventKey =
                        "$workoutId-goal",
                    title =
                        "Workout target reached",
                    message =
                        "$activityName reached its $levelName target. " +
                                "${formatAlertDistance(distanceKm)} • " +
                                String.format(
                                    Locale.US,
                                    "%.2f kcal",
                                    calories
                                ),
                    emoji =
                        "🏆"
                )

            MoveMateAlertManager
                .cancelLiveWorkout(
                    context,
                    workoutId
                )
        }
    }

    val liveUpdateBucket =
        elapsedMilliseconds /
                5_000L

    LaunchedEffect(
        workoutId,
        isRunning,
        isFinished,
        liveUpdateBucket
    ) {
        if (
            !isFinished
        ) {
            MoveMateAlertManager
                .updateLiveWorkout(
                    context =
                        context,
                    workoutId =
                        workoutId,
                    activityName =
                        activityName,
                    levelName =
                        levelName,
                    elapsedMilliseconds =
                        elapsedMilliseconds,
                    distanceKm =
                        latestDistanceKm,
                    calories =
                        latestCalories,
                    running =
                        isRunning
                )
        }
    }

    LaunchedEffect(
        workoutId,
        isFinished
    ) {
        if (
            isFinished
        ) {
            MoveMateAlertManager
                .cancelLiveWorkout(
                    context,
                    workoutId
                )
        }
    }


    DisposableEffect(
        workoutId
    ) {
        onDispose {
            if (
                !latestRunning
            ) {
                MoveMateAlertManager
                    .cancelLiveWorkout(
                        context,
                        workoutId
                    )
            }
        }
    }
}

private fun formatAlertDuration(
    elapsedMilliseconds: Long
): String {
    val totalSeconds =
        elapsedMilliseconds
            .coerceAtLeast(
                0L
            ) /
                1_000L

    val minutes =
        totalSeconds /
                60L

    val seconds =
        totalSeconds %
                60L

    return String.format(
        Locale.US,
        "%02d:%02d",
        minutes,
        seconds
    )
}

private fun formatAlertDistance(
    distanceKm: Double
): String {
    val safeDistance =
        distanceKm.coerceAtLeast(
            0.0
        )

    return if (
        safeDistance <
        1.0
    ) {
        String.format(
            Locale.US,
            "%.1f m",
            safeDistance *
                    1_000.0
        )
    } else {
        String.format(
            Locale.US,
            "%.2f km",
            safeDistance
        )
    }
}
