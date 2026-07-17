package com.example.movemate

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

data class ProgressSummaryData(
    val totalWorkouts: Int,
    val totalDurationSeconds: Int,
    val totalDistanceKm: Double,
    val totalCalories: Double,
    val activeDays: Int,
    val currentStreak: Int
)

data class ProgressGoalData(
    val exists: Boolean,
    val calorieTarget: Int,
    val minutesTarget: Int,
    val calorieProgressPercent: Float,
    val minutesProgressPercent: Float
)

data class ProgressDayData(
    val date: String,
    val workoutCount: Int,
    val durationSeconds: Int,
    val distanceKm: Double,
    val calories: Double
)

data class ProgressPageData(
    val days: Int,
    val fromDate: String,
    val toDate: String,
    val summary: ProgressSummaryData,
    val goal: ProgressGoalData,
    val daily: List<ProgressDayData>,
    val averageCaloriesPerActiveDay: Double,
    val averageWorkoutMinutes: Double,
    val bestDay: ProgressDayData?,
    val trendPercent: Double
)

fun JSONObject.toProgressPageData():
        ProgressPageData {
    val summaryJson =
        optJSONObject(
            "summary"
        )
            ?: this

    val goalJson =
        optJSONObject(
            "goal"
        )
            ?: JSONObject()

    val dailyJson =
        optJSONArray(
            "daily"
        )
            ?: optJSONArray(
                "progress"
            )
            ?: JSONArray()

    val daily =
        mutableListOf<ProgressDayData>()

    for (
    index in
    0 until dailyJson.length()
    ) {
        val item =
            dailyJson.optJSONObject(
                index
            )
                ?: continue

        daily +=
            ProgressDayData(
                date =
                    item.optString(
                        "date"
                    )
                        .ifBlank {
                            item.optString(
                                "workout_date"
                            )
                        },
                workoutCount =
                    item.optInt(
                        "workout_count",
                        item.optInt(
                            "total_workouts",
                            0
                        )
                    ),
                durationSeconds =
                    item.optInt(
                        "duration_seconds",
                        item.optInt(
                            "total_duration_seconds",
                            0
                        )
                    ),
                distanceKm =
                    item.optDouble(
                        "distance_km",
                        item.optDouble(
                            "total_distance_km",
                            0.0
                        )
                    ),
                calories =
                    item.optDouble(
                        "calories",
                        item.optDouble(
                            "total_calories",
                            0.0
                        )
                    )
            )
    }

    val totalWorkouts =
        summaryJson.optInt(
            "total_workouts",
            daily.sumOf {
                it.workoutCount
            }
        )

    val totalDurationSeconds =
        summaryJson.optInt(
            "total_duration_seconds",
            daily.sumOf {
                it.durationSeconds
            }
        )

    val totalDistanceKm =
        summaryJson.optDouble(
            "total_distance_km",
            daily.sumOf {
                it.distanceKm
            }
        )

    val totalCalories =
        summaryJson.optDouble(
            "total_calories",
            daily.sumOf {
                it.calories
            }
        )

    val computedActiveDays =
        daily.count {
            it.workoutCount >
                    0
        }

    val activeDays =
        summaryJson.optInt(
            "active_days",
            computedActiveDays
        )

    val currentStreak =
        summaryJson.optInt(
            "current_streak",
            calculateProgressStreak(
                daily
            )
        )

    /*
     * No invented defaults.
     * If the database has no saved goal, both targets remain 0.
     */
    val calorieTarget =
        goalJson.optInt(
            "calorie_target",
            goalJson.optInt(
                "workout_calories_target",
                0
            )
        )

    val minutesTarget =
        goalJson.optInt(
            "minutes_target",
            goalJson.optInt(
                "workout_minutes_target",
                0
            )
        )

    val goalExists =
        goalJson.optBoolean(
            "exists",
            calorieTarget >
                    0 ||
                    minutesTarget >
                    0
        )

    val calorieProgress =
        goalJson.optDouble(
            "calorie_progress_percent",
            if (
                calorieTarget >
                0
            ) {
                totalCalories /
                        calorieTarget *
                        100.0
            } else {
                0.0
            }
        )
            .toFloat()
            .coerceIn(
                0f,
                100f
            )

    val minutesProgress =
        goalJson.optDouble(
            "minutes_progress_percent",
            if (
                minutesTarget >
                0
            ) {
                (
                        totalDurationSeconds /
                                60.0
                        ) /
                        minutesTarget *
                        100.0
            } else {
                0.0
            }
        )
            .toFloat()
            .coerceIn(
                0f,
                100f
            )

    return ProgressPageData(
        days =
            optInt(
                "days",
                daily.size
                    .takeIf {
                        it >
                                0
                    }
                    ?: 7
            ),
        fromDate =
            optString(
                "from_date",
                daily.firstOrNull()
                    ?.date
                    .orEmpty()
            ),
        toDate =
            optString(
                "to_date",
                daily.lastOrNull()
                    ?.date
                    .orEmpty()
            ),
        summary =
            ProgressSummaryData(
                totalWorkouts =
                    totalWorkouts,
                totalDurationSeconds =
                    totalDurationSeconds,
                totalDistanceKm =
                    totalDistanceKm,
                totalCalories =
                    totalCalories,
                activeDays =
                    activeDays,
                currentStreak =
                    currentStreak
            ),
        goal =
            ProgressGoalData(
                exists =
                    goalExists,
                calorieTarget =
                    calorieTarget,
                minutesTarget =
                    minutesTarget,
                calorieProgressPercent =
                    calorieProgress,
                minutesProgressPercent =
                    minutesProgress
            ),
        daily =
            daily,
        averageCaloriesPerActiveDay =
            if (
                activeDays >
                0
            ) {
                totalCalories /
                        activeDays
            } else {
                0.0
            },
        averageWorkoutMinutes =
            if (
                totalWorkouts >
                0
            ) {
                (
                        totalDurationSeconds /
                                60.0
                        ) /
                        totalWorkouts
            } else {
                0.0
            },
        bestDay =
            daily.maxByOrNull {
                it.calories
            }
                ?.takeIf {
                    it.workoutCount >
                            0
                },
        trendPercent =
            calculateProgressTrend(
                daily
            )
    )
}

private fun calculateProgressTrend(
    daily: List<ProgressDayData>
): Double {
    val active =
        daily.filter {
            it.workoutCount >
                    0
        }

    if (
        active.size <
        2
    ) {
        return 0.0
    }

    val half =
        active.size /
                2

    if (
        half <=
        0
    ) {
        return 0.0
    }

    val olderAverage =
        active.take(
            half
        )
            .map {
                it.calories
            }
            .average()

    val newerAverage =
        active.drop(
            half
        )
            .map {
                it.calories
            }
            .average()

    if (
        olderAverage ==
        0.0
    ) {
        return if (
            newerAverage >
            0.0
        ) {
            100.0
        } else {
            0.0
        }
    }

    return (
            newerAverage -
                    olderAverage
            ) /
            olderAverage *
            100.0
}

private fun calculateProgressStreak(
    daily: List<ProgressDayData>
): Int {
    val parser =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )
            .apply {
                isLenient =
                    false
            }

    val activeDates =
        daily.filter {
            it.workoutCount >
                    0
        }
            .mapNotNull {
                runCatching {
                    parser.parse(
                        it.date
                    )
                }
                    .getOrNull()
            }
            .sortedDescending()

    if (
        activeDates.isEmpty()
    ) {
        return 0
    }

    var streak =
        1

    for (
    index in
    0 until activeDates.lastIndex
    ) {
        val newer =
            Calendar.getInstance()
                .apply {
                    time =
                        activeDates[
                            index
                        ]
                }

        val older =
            Calendar.getInstance()
                .apply {
                    time =
                        activeDates[
                            index +
                                    1
                        ]
                }

        val difference =
            abs(
                (
                        newer.timeInMillis -
                                older.timeInMillis
                        ) /
                        86_400_000L
            )

        if (
            difference ==
            1L
        ) {
            streak +=
                1
        } else {
            break
        }
    }

    return streak
}
