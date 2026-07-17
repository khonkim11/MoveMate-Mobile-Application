package com.example.movemate

import androidx.compose.ui.graphics.Color
import java.util.Locale

/*
 * Keep the existing constructor fields for compatibility with older screens.
 * New MET and per-level speed fields are appended with defaults.
 */
data class WorkoutLevel(
    val name: String,
    val description: String,
    val targetMinutes: Int,
    val calorieMultiplier: Double,
    val speedMultiplier: Double = 1.0
)

fun defaultWorkoutLevels(): List<WorkoutLevel> {
    return listOf(
        WorkoutLevel(
            name = "Easy",
            description = "Comfortable pace with a short target.",
            targetMinutes = 10,
            calorieMultiplier = 1.0,
            speedMultiplier = 1.0
        ),
        WorkoutLevel(
            name = "Medium",
            description = "Steady pace with a moderate target.",
            targetMinutes = 20,
            calorieMultiplier = 1.0,
            speedMultiplier = 1.0
        ),
        WorkoutLevel(
            name = "Hard",
            description = "Challenging pace with a longer target.",
            targetMinutes = 30,
            calorieMultiplier = 1.0,
            speedMultiplier = 1.0
        )
    )
}

data class WorkoutActivityOption(
    val name: String,
    val shortName: String,
    val emoji: String,
    val calorieRatePerMinute: Double,
    val accentColor: Color,
    val description: String,
    val beginnerTip: String,
    val tracksRoute: Boolean,
    val levels: List<WorkoutLevel> = defaultWorkoutLevels(),
    val averageSpeedKmh: Double = 0.0,

    /*
     * MET values used by the new weight-based calorie calculation.
     */
    val easyMet: Double = 3.0,
    val mediumMet: Double = 4.0,
    val hardMet: Double = 5.0,

    /*
     * Route speed targets. They are used only to calculate target distance
     * and to drive the Android Emulator demonstration.
     *
     * A physical phone still counts real changing GPS positions.
     */
    val easySpeedKmh: Double = 0.0,
    val mediumSpeedKmh: Double = 0.0,
    val hardSpeedKmh: Double = 0.0,

    /*
     * Label displayed for non-route movement sensing.
     */
    val movementMetricTitle: String = "Movement"
)

data class WorkoutSessionResult(
    val activityName: String,
    val levelName: String,
    val durationSeconds: Int,
    val distanceKm: Double,
    val calories: Double,
    val targetSeconds: Int = 0,
    val awardTier: String = "STARTER"
)

enum class WorkoutAwardTier(
    val serverValue: String,
    val title: String,
    val emoji: String,
    val message: String
) {
    STARTER(
        "STARTER",
        "Starter",
        "🌱",
        "Keep going to unlock your first medal."
    ),
    SILVER(
        "SILVER",
        "Silver",
        "🥈",
        "You completed at least half of your target."
    ),
    GOLD(
        "GOLD",
        "Gold",
        "🥇",
        "You reached your full workout target."
    ),
    DIAMOND(
        "DIAMOND",
        "Diamond",
        "💎",
        "You exceeded your target by at least 30%."
    );

    companion object {
        fun fromServer(
            value: String
        ): WorkoutAwardTier {
            return values()
                .firstOrNull {
                    it.serverValue.equals(
                        value,
                        ignoreCase = true
                    )
                }
                ?: STARTER
        }
    }
}

fun calculateWorkoutAward(
    durationSeconds: Int,
    targetSeconds: Int
): WorkoutAwardTier {
    if (
        durationSeconds <= 0 ||
        targetSeconds <= 0
    ) {
        return WorkoutAwardTier.STARTER
    }

    val ratio =
        durationSeconds.toDouble() /
                targetSeconds.toDouble()

    return when {
        ratio >= 1.30 ->
            WorkoutAwardTier.DIAMOND

        ratio >= 1.00 ->
            WorkoutAwardTier.GOLD

        ratio >= 0.50 ->
            WorkoutAwardTier.SILVER

        else ->
            WorkoutAwardTier.STARTER
    }
}

data class WorkoutGeoPoint(
    val latitude: Double,
    val longitude: Double,
    val timestampMillis: Long =
        System.currentTimeMillis()
)

data class WorkoutCompletionData(
    val activityName: String,
    val levelName: String,
    val durationSeconds: Int,
    val distanceKm: Double,
    val calories: Double,
    val awardTier: WorkoutAwardTier,
    val todayWorkoutCount: Int,
    val todayCalories: Double,
    val todayDurationSeconds: Int,
    val todayDistanceKm: Double
)

private fun normalizedWorkoutLevelName(
    level: WorkoutLevel
): String {
    return level.name
        .trim()
        .lowercase(
            Locale.US
        )
}

fun WorkoutActivityOption.metForLevel(
    level: WorkoutLevel
): Double {
    return when (
        normalizedWorkoutLevelName(
            level
        )
    ) {
        "easy",
        "basic",
        "beginner" ->
            easyMet

        "hard",
        "advanced" ->
            hardMet

        else ->
            mediumMet
    }
}

fun WorkoutActivityOption.speedForLevelKmh(
    level: WorkoutLevel
): Double {
    if (!tracksRoute) {
        return 0.0
    }

    return when (
        normalizedWorkoutLevelName(
            level
        )
    ) {
        "easy",
        "basic",
        "beginner" ->
            easySpeedKmh

        "hard",
        "advanced" ->
            hardSpeedKmh

        else ->
            mediumSpeedKmh
    }
}

/**
 * Standard MET estimate:
 *
 * kcal/min = MET × 3.5 × body weight in kg ÷ 200
 *
 * This is an estimate, not a direct medical measurement. A valid saved user
 * weight is used. When weight is missing, the UI clearly reports the 70 kg
 * fallback.
 */
fun calculateWorkoutCalories(
    activity: WorkoutActivityOption,
    level: WorkoutLevel,
    weightKg: Double,
    elapsedMilliseconds: Long
): Double {
    val safeWeightKg =
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

    /*
     * Display ACTIVE workout calories rather than gross calories.
     *
     * One MET represents resting demand. Subtracting 1 MET avoids counting
     * the calories the user would have burned while resting during the same
     * period.
     */
    val activeMet =
        (
                activity
                    .metForLevel(
                        level
                    ) -
                        1.0
                )
            .coerceAtLeast(
                0.0
            )

    val activeCaloriesPerMinute =
        activeMet *
                3.5 *
                safeWeightKg /
                200.0

    return activeCaloriesPerMinute *
            minutes
}

/**
 * Target distance is meaningful only for outdoor route activities.
 *
 * Yoga, HIIT, swimming, rowing-machine, and weight training save 0 km rather
 * than inventing distance from elapsed time.
 */
fun realisticTargetDistanceKm(
    activity: WorkoutActivityOption,
    level: WorkoutLevel
): Double {
    if (!activity.tracksRoute) {
        return 0.0
    }

    val targetHours =
        level.targetMinutes
            .coerceAtLeast(
                0
            ) /
                60.0

    return activity
        .speedForLevelKmh(
            level
        ) *
            targetHours
}

/*
 * Compatibility helper used by older screens.
 * It never estimates distance for non-route activities.
 */
fun estimateWorkoutDistanceKm(
    activity: WorkoutActivityOption,
    level: WorkoutLevel,
    elapsedSeconds: Int
): Double {
    if (!activity.tracksRoute) {
        return 0.0
    }

    val elapsedHours =
        elapsedSeconds
            .coerceAtLeast(
                0
            ) /
                3600.0

    return activity
        .speedForLevelKmh(
            level
        ) *
            elapsedHours
}

fun formatWorkoutTime(
    totalSeconds: Int
): String {
    val safe =
        totalSeconds
            .coerceAtLeast(
                0
            )

    val hours =
        safe /
                3600

    val minutes =
        (
                safe %
                        3600
                ) /
                60

    val seconds =
        safe %
                60

    return if (hours > 0) {
        String.format(
            Locale.US,
            "%02d:%02d:%02d",
            hours,
            minutes,
            seconds
        )
    } else {
        String.format(
            Locale.US,
            "%02d:%02d",
            minutes,
            seconds
        )
    }
}

fun Double.workoutFormatOneDecimal():
        String {
    return String.format(
        Locale.US,
        "%.1f",
        this
    )
}

val workoutActivityOptions =
    listOf(
        /*
         * Route speeds are intentionally conservative.
         *
         * Selected target examples:
         * Running Medium: 6.0 km/h × 20 min = 2.0 km
         * Walking Medium: 4.5 km/h × 20 min = 1.5 km
         * Cycling Medium: 12 km/h × 20 min = 4.0 km
         */
        WorkoutActivityOption(
            name = "Running",
            shortName = "Run",
            emoji = "🏃",
            calorieRatePerMinute = 6.0,
            accentColor = Color(0xFF2563EB),
            description =
                "One-way road route, filtered GPS distance and active calories.",
            beginnerTip =
                "Easy is a jog/walk pace. Increase pace only when comfortable.",
            tracksRoute = true,
            averageSpeedKmh = 6.0,
            easyMet = 4.0,
            mediumMet = 5.5,
            hardMet = 7.0,
            easySpeedKmh = 4.5,
            mediumSpeedKmh = 5.4,
            hardSpeedKmh = 6.6,
            movementMetricTitle = "Distance"
        ),
        WorkoutActivityOption(
            name = "Walking",
            shortName = "Walk",
            emoji = "🚶",
            calorieRatePerMinute = 3.0,
            accentColor = Color(0xFF16A34A),
            description =
                "One-way walking route, filtered GPS distance and active calories.",
            beginnerTip =
                "Use a comfortable pace and let GPS settle before moving.",
            tracksRoute = true,
            averageSpeedKmh = 4.5,
            easyMet = 3.0,
            mediumMet = 3.5,
            hardMet = 3.8,
            easySpeedKmh = 3.0,
            mediumSpeedKmh = 4.0,
            hardSpeedKmh = 5.0,
            movementMetricTitle = "Distance"
        ),
        WorkoutActivityOption(
            name = "Cycling",
            shortName = "Cycle",
            emoji = "🚴",
            calorieRatePerMinute = 5.0,
            accentColor = Color(0xFF0891B2),
            description =
                "One-way bicycle route, filtered GPS distance and active calories.",
            beginnerTip =
                "Use a safe road and maintain a controlled cadence.",
            tracksRoute = true,
            averageSpeedKmh = 12.0,
            easyMet = 3.5,
            mediumMet = 4.3,
            hardMet = 6.8,
            easySpeedKmh = 7.0,
            mediumSpeedKmh = 10.0,
            hardSpeedKmh = 14.0,
            movementMetricTitle = "Distance"
        ),
        WorkoutActivityOption(
            name = "Weightlifting",
            shortName = "Lift",
            emoji = "🏋️",
            calorieRatePerMinute = 3.0,
            accentColor = Color(0xFF7C3AED),
            description =
                "Time and active-calorie estimate; no fake distance or repetition count.",
            beginnerTip =
                "Use controlled form and stop before technique breaks down.",
            tracksRoute = false,
            easyMet = 2.5,
            mediumMet = 3.5,
            hardMet = 5.0,
            movementMetricTitle = "Target"
        ),
        WorkoutActivityOption(
            name = "Yoga",
            shortName = "Yoga",
            emoji = "🧘",
            calorieRatePerMinute = 2.0,
            accentColor = Color(0xFFDB2777),
            description =
                "Time and active-calorie estimate; no map or fake distance.",
            beginnerTip =
                "Move slowly and never force a painful pose.",
            tracksRoute = false,
            easyMet = 2.0,
            mediumMet = 2.5,
            hardMet = 3.0,
            movementMetricTitle = "Target"
        ),
        WorkoutActivityOption(
            name = "HIIT",
            shortName = "HIIT",
            emoji = "🔥",
            calorieRatePerMinute = 5.0,
            accentColor = Color(0xFFEA580C),
            description =
                "Interval timer and active-calorie estimate; no fake distance.",
            beginnerTip =
                "Keep work intervals short and recover fully.",
            tracksRoute = false,
            easyMet = 4.5,
            mediumMet = 6.0,
            hardMet = 8.0,
            movementMetricTitle = "Target"
        ),
        WorkoutActivityOption(
            name = "Swimming",
            shortName = "Swim",
            emoji = "🏊",
            calorieRatePerMinute = 5.0,
            accentColor = Color(0xFF0284C7),
            description =
                "Time and active-calorie estimate; pool distance needs lap data.",
            beginnerTip =
                "Use manual laps or a waterproof wearable for pool distance.",
            tracksRoute = false,
            easyMet = 4.0,
            mediumMet = 6.0,
            hardMet = 8.0,
            movementMetricTitle = "Target"
        ),
        WorkoutActivityOption(
            name = "Rowing",
            shortName = "Row",
            emoji = "🚣",
            calorieRatePerMinute = 4.0,
            accentColor = Color(0xFF0F766E),
            description =
                "Time and active-calorie estimate; no invented route distance.",
            beginnerTip =
                "Drive with the legs, then lean and pull.",
            tracksRoute = false,
            easyMet = 3.0,
            mediumMet = 5.0,
            hardMet = 7.0,
            movementMetricTitle = "Target"
        )
    )
