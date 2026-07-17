package com.example.movemate

import org.json.JSONObject

data class WorkoutHistorySummary(
    val totalWorkouts: Int,
    val totalDurationSeconds: Int,
    val totalDistanceKm: Double,
    val totalCalories: Double
)

data class WorkoutHistoryItem(
    val id: Int,
    val activityType: String,
    val workoutName: String,
    val levelName: String,
    val durationSeconds: Int,
    val targetSeconds: Int,
    val distanceKm: Double,
    val calories: Double,
    val awardTier: String,
    val intensity: String,
    val notes: String,
    val workoutStartedAt: String,
    val hasRoute: Boolean,
    val routePointCount: Int
)

data class WorkoutHistoryPage(
    val summary: WorkoutHistorySummary,
    val history: List<WorkoutHistoryItem>,
    val totalRows: Int,
    val returnedRows: Int
)

fun JSONObject.toWorkoutHistoryPage():
        WorkoutHistoryPage {
    val summaryObject =
        optJSONObject(
            "summary"
        ) ?: JSONObject()

    val historyArray =
        optJSONArray(
            "history"
        )

    val items =
        buildList {
            if (
                historyArray !=
                null
            ) {
                for (
                index in
                0 until
                        historyArray.length()
                ) {
                    val item =
                        historyArray
                            .optJSONObject(
                                index
                            )
                            ?: continue

                    add(
                        WorkoutHistoryItem(
                            id =
                                item.optInt(
                                    "id",
                                    0
                                ),
                            activityType =
                                item.optString(
                                    "activity_type",
                                    "Workout"
                                ),
                            workoutName =
                                item.optString(
                                    "workout_name",
                                    "Workout"
                                ),
                            levelName =
                                item.optString(
                                    "level_name",
                                    "Normal"
                                ),
                            durationSeconds =
                                item.optInt(
                                    "duration_seconds",
                                    0
                                ),
                            targetSeconds =
                                item.optInt(
                                    "target_seconds",
                                    0
                                ),
                            distanceKm =
                                item.optDouble(
                                    "distance_km",
                                    0.0
                                ),
                            calories =
                                item.optDouble(
                                    "calories",
                                    0.0
                                ),
                            awardTier =
                                item.optString(
                                    "award_tier",
                                    "STARTER"
                                ),
                            intensity =
                                item.optString(
                                    "intensity",
                                    item.optString(
                                        "level_name",
                                        "Normal"
                                    )
                                ),
                            notes =
                                item.optString(
                                    "notes",
                                    ""
                                ),
                            workoutStartedAt =
                                item.optString(
                                    "workout_started_at",
                                    item.optString(
                                        "created_at",
                                        ""
                                    )
                                ),
                            hasRoute =
                                item.optBoolean(
                                    "has_route",
                                    false
                                ),
                            routePointCount =
                                item.optInt(
                                    "route_point_count",
                                    0
                                )
                        )
                    )
                }
            }
        }

    return WorkoutHistoryPage(
        summary =
            WorkoutHistorySummary(
                totalWorkouts =
                    summaryObject.optInt(
                        "total_workouts",
                        items.size
                    ),
                totalDurationSeconds =
                    summaryObject.optInt(
                        "total_duration_seconds",
                        items.sumOf {
                            it.durationSeconds
                        }
                    ),
                totalDistanceKm =
                    summaryObject.optDouble(
                        "total_distance_km",
                        items.sumOf {
                            it.distanceKm
                        }
                    ),
                totalCalories =
                    summaryObject.optDouble(
                        "total_calories",
                        items.sumOf {
                            it.calories
                        }
                    )
            ),
        history =
            items,
        totalRows =
            optInt(
                "total_rows",
                items.size
            ),
        returnedRows =
            optInt(
                "returned_rows",
                items.size
            )
    )
}