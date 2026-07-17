package com.example.movemate

import android.os.SystemClock
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class ActiveWorkoutState(
    val id: String,
    val userId: Int,
    val activityName: String,
    val activityEmoji: String,
    val levelName: String,
    val targetSeconds: Int,
    val tracksRoute: Boolean,
    val accentColor: Color,
    val weightKg: Double,
    val metValue: Double,
    val elapsedMilliseconds: Long = 0L,
    val distanceKm: Double = 0.0,
    val routePoints: List<LatLng> = emptyList(),
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val createdAtMillis: Long =
        System.currentTimeMillis()
) {
    val elapsedSeconds: Int
        get() =
            (
                    elapsedMilliseconds /
                            1_000L
                    )
                .toInt()

    /**
     * Active calories only.
     *
     * Because elapsedMilliseconds is clamped at 60,000 for a one-minute
     * sample, calories also stop increasing at exactly one minute.
     */
    val calories: Double
        get() {
            val safeWeight =
                weightKg
                    .takeIf {
                        it in
                                20.0..350.0
                    }
                    ?: 70.0

            val minutes =
                elapsedMilliseconds
                    .coerceAtLeast(
                        0L
                    )
                    .toDouble() /
                        60_000.0

            val activeMet =
                (
                        metValue -
                                1.0
                        )
                    .coerceAtLeast(
                        0.0
                    )

            return activeMet *
                    3.5 *
                    safeWeight /
                    200.0 *
                    minutes
        }

    val progress: Float
        get() =
            if (
                targetSeconds >
                0
            ) {
                (
                        elapsedMilliseconds
                            .toFloat() /
                                (
                                        targetSeconds
                                            .toFloat() *
                                                1_000f
                                        )
                        )
                    .coerceIn(
                        0f,
                        1f
                    )
            } else {
                0f
            }
}

object ActiveWorkoutManager {

    val activeWorkouts =
        mutableStateListOf<ActiveWorkoutState>()

    private val scope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.Main.immediate
        )

    init {
        scope.launch {
            var previousTick =
                SystemClock
                    .elapsedRealtime()

            while (
                true
            ) {
                delay(
                    1_000L
                )

                val currentTick =
                    SystemClock
                        .elapsedRealtime()

                val delta =
                    (
                            currentTick -
                                    previousTick
                            )
                        .coerceIn(
                            0L,
                            1_500L
                        )

                previousTick =
                    currentTick

                for (
                index in
                activeWorkouts.indices
                ) {
                    val workout =
                        activeWorkouts[index]

                    if (
                        !workout.isRunning ||
                        workout.isFinished
                    ) {
                        continue
                    }

                    val nextElapsedMilliseconds =
                        workout
                            .elapsedMilliseconds +
                                delta

                    val oneMinuteSample =
                        isOneMinuteSampleWorkout(
                            levelName =
                                workout.levelName,
                            targetSeconds =
                                workout.targetSeconds
                        )

                    if (
                        oneMinuteSample
                    ) {
                        val exactTargetMilliseconds =
                            workout
                                .targetSeconds
                                .coerceAtLeast(
                                    1
                                )
                                .toLong() *
                                    1_000L

                        if (
                            nextElapsedMilliseconds >=
                            exactTargetMilliseconds
                        ) {
                            /*
                             * Hard stop:
                             * - time becomes exactly 01:00;
                             * - calories stop because they derive from time;
                             * - map movement stops because isRunning is false;
                             * - WorkoutSessionScreen sees isFinished and
                             *   performs automatic saving.
                             */
                            activeWorkouts[index] =
                                workout.copy(
                                    elapsedMilliseconds =
                                        exactTargetMilliseconds,
                                    isRunning =
                                        false,
                                    isFinished =
                                        true
                                )

                            continue
                        }
                    }

                    activeWorkouts[index] =
                        workout.copy(
                            elapsedMilliseconds =
                                nextElapsedMilliseconds
                        )
                }
            }
        }
    }

    fun getOrCreate(
        userId: Int,
        activity: WorkoutActivityOption,
        level: WorkoutLevel,
        weightKg: Double
    ): String {
        val existing =
            activeWorkouts
                .firstOrNull {
                    it.userId ==
                            userId &&
                            it.activityName
                                .equals(
                                    activity.name,
                                    ignoreCase =
                                        true
                                ) &&
                            it.levelName
                                .equals(
                                    level.name,
                                    ignoreCase =
                                        true
                                ) &&
                            !it.isFinished
                }

        if (
            existing !=
            null
        ) {
            return existing.id
        }

        val newWorkout =
            ActiveWorkoutState(
                id =
                    UUID.randomUUID()
                        .toString(),
                userId =
                    userId,
                activityName =
                    activity.name,
                activityEmoji =
                    activity.emoji,
                levelName =
                    level.name,
                targetSeconds =
                    level.targetMinutes
                        .coerceAtLeast(
                            1
                        ) *
                            60,
                tracksRoute =
                    activity.tracksRoute,
                accentColor =
                    activity.accentColor,
                weightKg =
                    weightKg,
                metValue =
                    activity.metForLevel(
                        level
                    )
            )

        activeWorkouts.add(
            newWorkout
        )

        return newWorkout.id
    }

    fun find(
        workoutId: String
    ): ActiveWorkoutState? {
        return activeWorkouts
            .firstOrNull {
                it.id ==
                        workoutId
            }
    }

    fun activeForUser(
        userId: Int
    ): List<ActiveWorkoutState> {
        return activeWorkouts
            .filter {
                it.userId ==
                        userId &&
                        !it.isFinished
            }
    }

    fun runningForUser(
        userId: Int
    ): List<ActiveWorkoutState> {
        return activeForUser(
            userId
        )
            .filter {
                it.isRunning
            }
    }

    fun activeCaloriesForUser(
        userId: Int
    ): Double {
        return activeForUser(
            userId
        )
            .sumOf {
                it.calories
            }
    }

    fun start(
        workoutId: String
    ) {
        update(
            workoutId
        ) {
            if (
                it.isFinished
            ) {
                it
            } else {
                it.copy(
                    isRunning =
                        true
                )
            }
        }
    }

    fun pause(
        workoutId: String
    ) {
        update(
            workoutId
        ) {
            it.copy(
                isRunning =
                    false
            )
        }
    }

    fun reset(
        workoutId: String
    ) {
        update(
            workoutId
        ) {
            it.copy(
                elapsedMilliseconds =
                    0L,
                distanceKm =
                    0.0,
                routePoints =
                    emptyList(),
                isRunning =
                    false,
                isFinished =
                    false
            )
        }
    }

    fun updateDistance(
        workoutId: String,
        distanceKm: Double
    ) {
        update(
            workoutId
        ) {
            it.copy(
                distanceKm =
                    distanceKm
                        .coerceAtLeast(
                            0.0
                        )
            )
        }
    }

    fun updateRoute(
        workoutId: String,
        routePoints: List<LatLng>
    ) {
        update(
            workoutId
        ) {
            it.copy(
                routePoints =
                    routePoints
                        .toList()
            )
        }
    }

    fun remove(
        workoutId: String
    ) {
        activeWorkouts
            .removeAll {
                it.id ==
                        workoutId
            }
    }

    fun clearUser(
        userId: Int
    ) {
        activeWorkouts
            .removeAll {
                it.userId ==
                        userId
            }
    }

    private inline fun update(
        workoutId: String,
        transform:
            (
            ActiveWorkoutState
        ) -> ActiveWorkoutState
    ) {
        val index =
            activeWorkouts
                .indexOfFirst {
                    it.id ==
                            workoutId
                }

        if (
            index <
            0
        ) {
            return
        }

        activeWorkouts[index] =
            transform(
                activeWorkouts[index]
            )
    }
}