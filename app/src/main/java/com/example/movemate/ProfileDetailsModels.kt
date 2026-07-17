package com.example.movemate

import org.json.JSONArray
import org.json.JSONObject

data class ProfileWorkoutSummary(
    val totalWorkouts: Int = 0,
    val totalDurationSeconds: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val totalCalories: Double = 0.0,
    val activeDays: Int = 0,
    val firstWorkoutAt: String = "",
    val latestWorkoutAt: String = "",
    val longestWorkoutSeconds: Int = 0,
    val longestDistanceKm: Double = 0.0,
    val highestCalories: Double = 0.0,
    val unlockedAchievements: Int = 0,
    val totalAchievements: Int = 0
)

data class ProfileAwardCounts(
    val starter: Int = 0,
    val silver: Int = 0,
    val gold: Int = 0,
    val diamond: Int = 0
)

data class ProfileActivityStat(
    val activityName: String,
    val workoutCount: Int,
    val durationSeconds: Int,
    val distanceKm: Double,
    val calories: Double
)

data class ProfileAchievementItem(
    val key: String,
    val title: String,
    val description: String,
    val emoji: String,
    val tier: String,
    val progressValue: Double,
    val targetValue: Double,
    val unlocked: Boolean,
    val unlockedAt: String
) {
    val progressFraction: Float
        get() =
            if (
                targetValue >
                0.0
            ) {
                (
                        progressValue /
                                targetValue
                        )
                    .toFloat()
                    .coerceIn(
                        0f,
                        1f
                    )
            } else {
                if (
                    unlocked
                ) {
                    1f
                } else {
                    0f
                }
            }
}

data class ProfileFinishedWorkoutItem(
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
    val workoutAt: String
)

data class ProfileDetailsData(
    val fullName: String,
    val email: String,
    val gender: String,
    val age: Int,
    val weightKg: Double,
    val heightCm: Double,
    val summary: ProfileWorkoutSummary,
    val awardCounts: ProfileAwardCounts,
    val activityStats: List<ProfileActivityStat>,
    val achievements: List<ProfileAchievementItem>,
    val workouts: List<ProfileFinishedWorkoutItem>
)

data class ProfileDetailsResult(
    val success: Boolean,
    val message: String,
    val data: ProfileDetailsData?
)

fun parseProfileDetailsResult(
    response: JSONObject,
    fallbackSession: UserSession
): ProfileDetailsResult {
    if (
        !response.optBoolean(
            "success",
            false
        )
    ) {
        return ProfileDetailsResult(
            success =
                false,
            message =
                response.optString(
                    "message",
                    "Could not load profile details."
                ),
            data =
                null
        )
    }

    val profile =
        response.optJSONObject(
            "profile"
        )
            ?: JSONObject()

    val summary =
        response.optJSONObject(
            "summary"
        )
            ?: JSONObject()

    val awardCounts =
        response.optJSONObject(
            "award_counts"
        )
            ?: JSONObject()

    return ProfileDetailsResult(
        success =
            true,
        message =
            response.optString(
                "message",
                "Profile details loaded."
            ),
        data =
            ProfileDetailsData(
                fullName =
                    profile.optString(
                        "full_name",
                        fallbackSession.fullName
                    ),
                email =
                    profile.optString(
                        "email",
                        fallbackSession.email
                    ),
                gender =
                    profile.optString(
                        "gender",
                        fallbackSession.gender
                    ),
                age =
                    profile.optInt(
                        "age",
                        fallbackSession.age
                    ),
                weightKg =
                    profile.optDouble(
                        "weight_kg",
                        fallbackSession.weightKg
                    ),
                heightCm =
                    profile.optDouble(
                        "height_cm",
                        fallbackSession.heightCm
                    ),
                summary =
                    ProfileWorkoutSummary(
                        totalWorkouts =
                            summary.optInt(
                                "total_workouts",
                                0
                            ),
                        totalDurationSeconds =
                            summary.optInt(
                                "total_duration_seconds",
                                0
                            ),
                        totalDistanceKm =
                            summary.optDouble(
                                "total_distance_km",
                                0.0
                            ),
                        totalCalories =
                            summary.optDouble(
                                "total_calories",
                                0.0
                            ),
                        activeDays =
                            summary.optInt(
                                "active_days",
                                0
                            ),
                        firstWorkoutAt =
                            summary.optString(
                                "first_workout_at",
                                ""
                            ),
                        latestWorkoutAt =
                            summary.optString(
                                "latest_workout_at",
                                ""
                            ),
                        longestWorkoutSeconds =
                            summary.optInt(
                                "longest_workout_seconds",
                                0
                            ),
                        longestDistanceKm =
                            summary.optDouble(
                                "longest_distance_km",
                                0.0
                            ),
                        highestCalories =
                            summary.optDouble(
                                "highest_calories",
                                0.0
                            ),
                        unlockedAchievements =
                            summary.optInt(
                                "unlocked_achievements",
                                0
                            ),
                        totalAchievements =
                            summary.optInt(
                                "total_achievements",
                                0
                            )
                    ),
                awardCounts =
                    ProfileAwardCounts(
                        starter =
                            awardCounts.optInt(
                                "starter",
                                0
                            ),
                        silver =
                            awardCounts.optInt(
                                "silver",
                                0
                            ),
                        gold =
                            awardCounts.optInt(
                                "gold",
                                0
                            ),
                        diamond =
                            awardCounts.optInt(
                                "diamond",
                                0
                            )
                    ),
                activityStats =
                    response
                        .optJSONArray(
                            "activity_stats"
                        )
                        .toProfileActivityStats(),
                achievements =
                    response
                        .optJSONArray(
                            "achievements"
                        )
                        .toProfileAchievements(),
                workouts =
                    response
                        .optJSONArray(
                            "workouts"
                        )
                        .toProfileFinishedWorkouts()
            )
    )
}

private fun JSONArray?.toProfileActivityStats():
        List<ProfileActivityStat> {
    if (
        this ==
        null
    ) {
        return emptyList()
    }

    return buildList {
        for (
        index in
        0 until length()
        ) {
            val item =
                optJSONObject(
                    index
                )
                    ?: continue

            add(
                ProfileActivityStat(
                    activityName =
                        item.optString(
                            "activity_type",
                            "Workout"
                        ),
                    workoutCount =
                        item.optInt(
                            "workout_count",
                            0
                        ),
                    durationSeconds =
                        item.optInt(
                            "duration_seconds",
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
                        )
                )
            )
        }
    }
}

private fun JSONArray?.toProfileAchievements():
        List<ProfileAchievementItem> {
    if (
        this ==
        null
    ) {
        return emptyList()
    }

    return buildList {
        for (
        index in
        0 until length()
        ) {
            val item =
                optJSONObject(
                    index
                )
                    ?: continue

            add(
                ProfileAchievementItem(
                    key =
                        item.optString(
                            "achievement_key",
                            "achievement_$index"
                        ),
                    title =
                        item.optString(
                            "title",
                            "Achievement"
                        ),
                    description =
                        item.optString(
                            "description",
                            ""
                        ),
                    emoji =
                        item.optString(
                            "emoji",
                            "🏆"
                        ),
                    tier =
                        item.optString(
                            "tier",
                            "BRONZE"
                        ),
                    progressValue =
                        item.optDouble(
                            "progress_value",
                            0.0
                        ),
                    targetValue =
                        item.optDouble(
                            "target_value",
                            1.0
                        ),
                    unlocked =
                        item.optBoolean(
                            "is_unlocked",
                            false
                        ),
                    unlockedAt =
                        item.optString(
                            "unlocked_at",
                            ""
                        )
                )
            )
        }
    }
}

private fun JSONArray?.toProfileFinishedWorkouts():
        List<ProfileFinishedWorkoutItem> {
    if (
        this ==
        null
    ) {
        return emptyList()
    }

    return buildList {
        for (
        index in
        0 until length()
        ) {
            val item =
                optJSONObject(
                    index
                )
                    ?: continue

            add(
                ProfileFinishedWorkoutItem(
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
                            ""
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
                            ""
                        ),
                    notes =
                        item.optString(
                            "notes",
                            ""
                        ),
                    workoutAt =
                        item.optString(
                            "workout_at",
                            ""
                        )
                )
            )
        }
    }
}
