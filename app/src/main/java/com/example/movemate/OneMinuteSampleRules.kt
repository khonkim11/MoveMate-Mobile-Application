package com.example.movemate

import java.util.Locale

/**
 * Returns true only for the Home-page one-minute sample level.
 *
 * Normal Easy, Medium and Hard workouts are not auto-finished by this rule.
 */
fun isOneMinuteSampleWorkout(
    levelName: String,
    targetSeconds: Int
): Boolean {
    if (
        targetSeconds !=
        60
    ) {
        return false
    }

    val normalizedName =
        levelName
            .trim()
            .lowercase(
                Locale.US
            )
            .replace(
                " ",
                ""
            )
            .replace(
                "-",
                ""
            )
            .replace(
                "_",
                ""
            )

    return normalizedName in
            setOf(
                "sample1min",
                "1minutesample",
                "quick1min",
                "quick1minute"
            )
}