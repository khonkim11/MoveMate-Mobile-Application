package com.example.movemate

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Connects the workout timer to Android system notifications.
 *
 * Add this once inside WorkoutSessionScreen.
 */
@Composable
fun LiveWorkoutNotificationEffects(
    context: Context,
    userId: Int,
    workoutId: String,
    activityName: String,
    activityEmoji: String,
    levelName: String,
    elapsedSeconds: Int,
    targetSeconds: Int,
    isRunning: Boolean,
    isFinished: Boolean,
    award: WorkoutAwardTier
) {
    val safeTargetSeconds =
        targetSeconds.coerceAtLeast(
            1
        )

    val progressPercentage =
        (
                elapsedSeconds
                    .coerceAtLeast(
                        0
                    )
                    .toDouble() /
                        safeTargetSeconds
                            .toDouble() *
                        100.0
                )
            .toInt()
            .coerceIn(
                0,
                100
            )

    /*
     * Start notification immediately and update the same ongoing card once
     * per minute. Updating the same ID avoids filling the notification tray.
     */
    LaunchedEffect(
        workoutId,
        isRunning,
        isFinished,
        elapsedSeconds /
                60,
        targetSeconds
    ) {
        if (
            isFinished
        ) {
            MoveMateLiveWorkoutNotifications
                .cancelOngoing(
                    context,
                    workoutId
                )

            return@LaunchedEffect
        }

        if (
            isRunning ||
            elapsedSeconds >
            0
        ) {
            MoveMateLiveWorkoutNotifications
                .showStartedOrUpdate(
                    context =
                        context,
                    userId =
                        userId,
                    workoutId =
                        workoutId,
                    activityName =
                        activityName,
                    activityEmoji =
                        activityEmoji,
                    levelName =
                        levelName,
                    elapsedSeconds =
                        elapsedSeconds,
                    targetSeconds =
                        targetSeconds,
                    isPaused =
                        !isRunning
                )
        }
    }

    /*
     * Separate heads-up alerts during the workout.
     *
     * For a 10-minute workout:
     * 25% = 02:30
     * 50% = 05:00
     * 75% = 07:30
     * 100% = 10:00
     */
    LaunchedEffect(
        workoutId,
        progressPercentage,
        award
    ) {
        val reachedMilestone =
            when {
                progressPercentage >=
                        100 ->
                    100

                progressPercentage >=
                        75 ->
                    75

                progressPercentage >=
                        50 ->
                    50

                progressPercentage >=
                        25 ->
                    25

                else ->
                    0
            }

        if (
            reachedMilestone >
            0
        ) {
            /*
             * If the timer jumps over more than one threshold, post all
             * missing milestones. Event persistence prevents duplicates.
             */
            listOf(
                25,
                50,
                75,
                100
            )
                .filter {
                    it <=
                            reachedMilestone
                }
                .forEach {
                        milestone ->

                    MoveMateLiveWorkoutNotifications
                        .showMilestoneOnce(
                            context =
                                context,
                            userId =
                                userId,
                            workoutId =
                                workoutId,
                            activityName =
                                activityName,
                            activityEmoji =
                                activityEmoji,
                            levelName =
                                levelName,
                            percentage =
                                milestone,
                            awardTitle =
                                award.title,
                            awardEmoji =
                                award.emoji
                        )
                }
        }
    }
}