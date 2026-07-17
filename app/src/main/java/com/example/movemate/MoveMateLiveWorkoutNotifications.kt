package com.example.movemate

import android.Manifest
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
import java.util.Locale

/**
 * Notifications used while a workout is actively running.
 *
 * This is separate from the daily workout-reminder scheduler:
 * - one ongoing notification while the workout is active;
 * - progress alerts at 25%, 50%, 75%, and 100%;
 * - event keys prevent the same alert from being posted repeatedly.
 */
object MoveMateLiveWorkoutNotifications {

    private const val LIVE_CHANNEL_ID =
        "movemate_live_workout"

    private const val MILESTONE_CHANNEL_ID =
        "movemate_workout_milestones"

    private const val EVENT_PREFS =
        "movemate_live_workout_notification_events"

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

        val liveChannel =
            NotificationChannel(
                LIVE_CHANNEL_ID,
                "Active workout",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description =
                    "Shows the workout currently running."

                setShowBadge(
                    false
                )
            }

        val milestoneChannel =
            NotificationChannel(
                MILESTONE_CHANNEL_ID,
                "Workout progress",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    "Shows progress and award alerts during workouts."

                enableVibration(
                    true
                )
            }

        manager.createNotificationChannel(
            liveChannel
        )

        manager.createNotificationChannel(
            milestoneChannel
        )
    }

    fun canPost(
        context: Context
    ): Boolean {
        val permissionGranted =
            Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) ==
                    PackageManager.PERMISSION_GRANTED

        return permissionGranted &&
                NotificationManagerCompat
                    .from(
                        context
                    )
                    .areNotificationsEnabled()
    }

    /**
     * Called as soon as Start/Resume begins the workout.
     *
     * The alert sound is shown only once for this workout. Later calls update
     * the same ongoing notification instead of creating duplicates.
     */
    fun showStartedOrUpdate(
        context: Context,
        userId: Int,
        workoutId: String,
        activityName: String,
        activityEmoji: String,
        levelName: String,
        elapsedSeconds: Int,
        targetSeconds: Int,
        isPaused: Boolean
    ) {
        if (
            userId <=
            0 ||
            workoutId.isBlank()
        ) {
            return
        }

        val settings =
            loadMoveMateNotificationSettings(
                context,
                userId
            )

        if (
            !settings.enabled
        ) {
            cancelOngoing(
                context,
                workoutId
            )

            return
        }

        createChannels(
            context
        )

        if (
            !canPost(
                context
            )
        ) {
            return
        }

        val safeTarget =
            targetSeconds.coerceAtLeast(
                1
            )

        val safeElapsed =
            elapsedSeconds.coerceIn(
                0,
                safeTarget
            )

        val percentage =
            (
                    safeElapsed
                        .toDouble() /
                            safeTarget
                                .toDouble() *
                            100.0
                    )
                .toInt()
                .coerceIn(
                    0,
                    100
                )

        val startedForFirstTime =
            markEventOnce(
                context =
                    context,
                userId =
                    userId,
                workoutId =
                    workoutId,
                eventName =
                    "started"
            )

        val contentText =
            if (
                isPaused
            ) {
                "$levelName • Paused at ${
                    formatLiveWorkoutTime(
                        elapsedSeconds
                    )
                }"
            } else {
                "$levelName • ${
                    formatLiveWorkoutTime(
                        elapsedSeconds
                    )
                } / ${
                    formatLiveWorkoutTime(
                        targetSeconds
                    )
                }"
            }

        val builder =
            NotificationCompat
                .Builder(
                    context,
                    LIVE_CHANNEL_ID
                )
                .setSmallIcon(
                    android.R.drawable.ic_media_play
                )
                .setContentTitle(
                    if (
                        isPaused
                    ) {
                        "$activityEmoji $activityName paused"
                    } else {
                        "$activityEmoji $activityName in progress"
                    }
                )
                .setContentText(
                    contentText
                )
                .setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(
                            "$contentText • $percentage% completed"
                        )
                )
                .setContentIntent(
                    appPendingIntent(
                        context,
                        workoutId
                    )
                )
                .setProgress(
                    safeTarget,
                    safeElapsed,
                    false
                )
                .setOngoing(
                    !isPaused
                )
                .setOnlyAlertOnce(
                    true
                )
                .setSilent(
                    !startedForFirstTime
                )
                .setAutoCancel(
                    false
                )
                .setCategory(
                    NotificationCompat.CATEGORY_WORKOUT
                )
                .setVisibility(
                    NotificationCompat.VISIBILITY_PUBLIC
                )

        NotificationManagerCompat
            .from(
                context
            )
            .notify(
                ongoingNotificationId(
                    workoutId
                ),
                builder.build()
            )

        if (
            startedForFirstTime
        ) {
            addMoveMateNotificationLog(
                context =
                    context,
                userId =
                    userId,
                title =
                    "$activityName started",
                message =
                    "$levelName workout started. " +
                            "Target: ${
                                formatLiveWorkoutTime(
                                    targetSeconds
                                )
                            }."
            )
        }
    }

    /**
     * Posts a separate heads-up alert for a milestone.
     *
     * Supported percentages: 25, 50, 75, 100.
     */
    fun showMilestoneOnce(
        context: Context,
        userId: Int,
        workoutId: String,
        activityName: String,
        activityEmoji: String,
        levelName: String,
        percentage: Int,
        awardTitle: String,
        awardEmoji: String
    ) {
        if (
            percentage !in
            setOf(
                25,
                50,
                75,
                100
            )
        ) {
            return
        }

        val settings =
            loadMoveMateNotificationSettings(
                context,
                userId
            )

        if (
            !settings.enabled
        ) {
            return
        }

        if (
            percentage ==
            100 &&
            !settings.achievementAlertsEnabled
        ) {
            return
        }

        val isNewEvent =
            markEventOnce(
                context =
                    context,
                userId =
                    userId,
                workoutId =
                    workoutId,
                eventName =
                    "progress_$percentage"
            )

        if (
            !isNewEvent
        ) {
            return
        }

        createChannels(
            context
        )

        val title =
            when (
                percentage
            ) {
                25 ->
                    "$activityEmoji Great start — 25%"

                50 ->
                    "$activityEmoji Halfway there — 50%"

                75 ->
                    "$activityEmoji Almost finished — 75%"

                else ->
                    "$awardEmoji $awardTitle award unlocked"
            }

        val message =
            when (
                percentage
            ) {
                25 ->
                    "$activityName $levelName is underway. Keep your pace steady."

                50 ->
                    "You completed half of your $activityName target."

                75 ->
                    "Only 25% remains in your $activityName workout."

                else ->
                    "You completed the full $levelName target and earned the $awardTitle award."
            }

        if (
            canPost(
                context
            )
        ) {
            val notification =
                NotificationCompat
                    .Builder(
                        context,
                        MILESTONE_CHANNEL_ID
                    )
                    .setSmallIcon(
                        android.R.drawable.star_big_on
                    )
                    .setContentTitle(
                        title
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
                            workoutId
                        )
                    )
                    .setPriority(
                        NotificationCompat.PRIORITY_HIGH
                    )
                    .setCategory(
                        NotificationCompat.CATEGORY_STATUS
                    )
                    .setVisibility(
                        NotificationCompat.VISIBILITY_PUBLIC
                    )
                    .setAutoCancel(
                        true
                    )
                    .build()

            NotificationManagerCompat
                .from(
                    context
                )
                .notify(
                    milestoneNotificationId(
                        workoutId,
                        percentage
                    ),
                    notification
                )
        }

        /*
         * Keep the alert visible inside the app's Recent Notifications list
         * even when Android notification permission is disabled.
         */
        addMoveMateNotificationLog(
            context =
                context,
            userId =
                userId,
            title =
                title,
            message =
                message
        )
    }

    fun showFinished(
        context: Context,
        userId: Int,
        workoutId: String,
        activityName: String,
        awardTitle: String,
        awardEmoji: String
    ) {
        cancelOngoing(
            context,
            workoutId
        )

        val isNewEvent =
            markEventOnce(
                context =
                    context,
                userId =
                    userId,
                workoutId =
                    workoutId,
                eventName =
                    "finished"
            )

        if (
            !isNewEvent
        ) {
            return
        }

        val settings =
            loadMoveMateNotificationSettings(
                context,
                userId
            )

        if (
            !settings.enabled
        ) {
            return
        }

        createChannels(
            context
        )

        val title =
            "$awardEmoji Workout complete"

        val message =
            "$activityName saved successfully. $awardTitle award earned."

        if (
            canPost(
                context
            )
        ) {
            val notification =
                NotificationCompat
                    .Builder(
                        context,
                        MILESTONE_CHANNEL_ID
                    )
                    .setSmallIcon(
                        android.R.drawable.checkbox_on_background
                    )
                    .setContentTitle(
                        title
                    )
                    .setContentText(
                        message
                    )
                    .setContentIntent(
                        appPendingIntent(
                            context,
                            workoutId
                        )
                    )
                    .setPriority(
                        NotificationCompat.PRIORITY_HIGH
                    )
                    .setAutoCancel(
                        true
                    )
                    .build()

            NotificationManagerCompat
                .from(
                    context
                )
                .notify(
                    milestoneNotificationId(
                        workoutId,
                        101
                    ),
                    notification
                )
        }

        addMoveMateNotificationLog(
            context =
                context,
            userId =
                userId,
            title =
                title,
            message =
                message
        )
    }

    fun cancelOngoing(
        context: Context,
        workoutId: String
    ) {
        NotificationManagerCompat
            .from(
                context
            )
            .cancel(
                ongoingNotificationId(
                    workoutId
                )
            )
    }

    /**
     * Call this when Reset removes/restarts the same workout ID.
     */
    fun clearWorkoutEvents(
        context: Context,
        userId: Int,
        workoutId: String
    ) {
        val prefix =
            eventPrefix(
                userId,
                workoutId
            )

        val preferences =
            context.getSharedPreferences(
                EVENT_PREFS,
                Context.MODE_PRIVATE
            )

        val editor =
            preferences.edit()

        preferences
            .all
            .keys
            .filter {
                it.startsWith(
                    prefix
                )
            }
            .forEach {
                editor.remove(
                    it
                )
            }

        editor.apply()

        cancelOngoing(
            context,
            workoutId
        )
    }

    private fun markEventOnce(
        context: Context,
        userId: Int,
        workoutId: String,
        eventName: String
    ): Boolean {
        val preferences =
            context.getSharedPreferences(
                EVENT_PREFS,
                Context.MODE_PRIVATE
            )

        val key =
            eventPrefix(
                userId,
                workoutId
            ) +
                    eventName

        if (
            preferences.getBoolean(
                key,
                false
            )
        ) {
            return false
        }

        preferences
            .edit()
            .putBoolean(
                key,
                true
            )
            .apply()

        return true
    }

    private fun eventPrefix(
        userId: Int,
        workoutId: String
    ): String {
        return "user_${userId}_workout_${workoutId}_"
    }

    private fun ongoingNotificationId(
        workoutId: String
    ): Int {
        return 20_000 +
                positiveHash(
                    workoutId
                ) %
                9_000
    }

    private fun milestoneNotificationId(
        workoutId: String,
        percentage: Int
    ): Int {
        return 30_000 +
                positiveHash(
                    "$workoutId-$percentage"
                ) %
                20_000
    }

    private fun positiveHash(
        value: String
    ): Int {
        return value
            .hashCode() and
                0x7FFFFFFF
    }

    private fun appPendingIntent(
        context: Context,
        workoutId: String
    ): PendingIntent {
        val intent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP

                putExtra(
                    "open_active_workout_id",
                    workoutId
                )
            }

        return PendingIntent.getActivity(
            context,
            ongoingNotificationId(
                workoutId
            ),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatLiveWorkoutTime(
        totalSeconds: Int
    ): String {
        val safeSeconds =
            totalSeconds.coerceAtLeast(
                0
            )

        return String.format(
            Locale.US,
            "%02d:%02d",
            safeSeconds /
                    60,
            safeSeconds %
                    60
        )
    }
}