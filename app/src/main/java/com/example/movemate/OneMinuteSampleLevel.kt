package com.example.movemate

/**
 * Creates a safe one-minute sample from an activity's Easy level.
 *
 * This helper is used only by the Home-page sample cards.
 * Normal workouts still use Easy, Medium and Hard from WorkoutLevelScreen.
 */
fun oneMinuteSampleLevel(
    activity: WorkoutActivityOption
): WorkoutLevel {
    val baseLevel =
        activity.levels
            .firstOrNull {
                    level ->

                level.name.equals(
                    "Easy",
                    ignoreCase =
                        true
                ) ||
                        level.name.equals(
                            "Basic",
                            ignoreCase =
                                true
                        ) ||
                        level.name.equals(
                            "Beginner",
                            ignoreCase =
                                true
                        )
            }
            ?: activity.levels
                .firstOrNull()
            ?: error(
                "${activity.name} has no workout levels."
            )

    return baseLevel.copy(
        name =
            "Sample 1 Min",
        description =
            "A one-minute sample of ${activity.name}.",
        targetMinutes =
            1,
        calorieMultiplier =
            minOf(
                baseLevel.calorieMultiplier,
                1.0
            ),
        speedMultiplier =
            minOf(
                baseLevel.speedMultiplier,
                1.0
            )
    )
}