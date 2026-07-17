package com.example.movemate

import java.util.Locale
import kotlin.math.abs

data class ActiveWorkoutSelection(
    val activity: WorkoutActivityOption,
    val level: WorkoutLevel
)

fun resolveActiveWorkoutSelection(
    activeWorkout: ActiveWorkoutState
): ActiveWorkoutSelection {
    val knownActivity =
        workoutActivityOptions.firstOrNull {
            normalizeWorkoutName(it.name) ==
                    normalizeWorkoutName(activeWorkout.activityName) ||
                    normalizeWorkoutName(it.shortName) ==
                    normalizeWorkoutName(activeWorkout.activityName)
        }

    val knownLevel =
        knownActivity
            ?.levels
            ?.firstOrNull {
                normalizeWorkoutName(it.name) ==
                        normalizeWorkoutName(activeWorkout.levelName)
            }
            ?: knownActivity
                ?.levels
                ?.firstOrNull {
                    levelGroup(it.name) ==
                            levelGroup(activeWorkout.levelName)
                }
            ?: knownActivity
                ?.levels
                ?.minByOrNull {
                    abs(
                        it.targetMinutes -
                                (activeWorkout.targetSeconds / 60)
                    )
                }

    val resolvedLevel =
        knownLevel
            ?: WorkoutLevel(
                name =
                    activeWorkout.levelName.ifBlank {
                        "Medium"
                    },
                description =
                    "Continuing your active workout.",
                targetMinutes =
                    (activeWorkout.targetSeconds / 60)
                        .coerceAtLeast(1),
                calorieMultiplier = 1.0,
                speedMultiplier = 1.0
            )

    val resolvedActivity =
        knownActivity
            ?: WorkoutActivityOption(
                name = activeWorkout.activityName,
                shortName =
                    activeWorkout.activityName.take(12),
                emoji = activeWorkout.activityEmoji,
                calorieRatePerMinute = 0.0,
                accentColor = activeWorkout.accentColor,
                description =
                    "Continuing your active workout.",
                beginnerTip =
                    "Use Pause or Finish when ready.",
                tracksRoute = activeWorkout.tracksRoute,
                levels = listOf(resolvedLevel)
            )

    return ActiveWorkoutSelection(
        activity = resolvedActivity,
        level = resolvedLevel
    )
}

private fun normalizeWorkoutName(
    value: String
): String =
    value
        .trim()
        .lowercase(Locale.US)
        .replace(" ", "")
        .replace("-", "")
        .replace("_", "")

private fun levelGroup(
    value: String
): Int =
    when (normalizeWorkoutName(value)) {
        "easy", "basic", "beginner" -> 1
        "medium", "normal", "intermediate" -> 2
        "hard", "advanced" -> 3
        else -> 0
    }
