package com.example.movemate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.math.roundToInt

data class HomeGoalDashboardState(
    val loading: Boolean,
    val effectiveUserId: Int,
    val savedGoal: GoalCenterData?,
    val totalWorkouts: Int,
    val totalSeconds: Int,
    val totalDistanceKm: Double,
    val totalCalories: Double,
    val targetCalories: Double,
    val remainingCalories: Double,
    val caloriesAboveGoal: Double,
    val progress: Float,
    val completed: Boolean,
    val topActivity: String,
    val activeWorkoutCount: Int,
    val errorMessage: String,
    val refresh: () -> Unit
) {
    val progressPercent: Int
        get() =
            (progress * 100f)
                .roundToInt()
                .coerceIn(0, 100)
}

@Composable
fun rememberHomeGoalDashboard(
    session: UserSession,
    refreshToken: Int = 0
): HomeGoalDashboardState {
    val context = LocalContext.current

    var effectiveUserId by remember(
        session.userId,
        session.email
    ) {
        mutableIntStateOf(session.userId)
    }

    var loading by remember(session.email) {
        mutableStateOf(true)
    }

    var summaryResponse by remember(session.email) {
        mutableStateOf(JSONObject())
    }

    var savedGoal by remember(session.email) {
        mutableStateOf<GoalCenterData?>(null)
    }

    var errorMessage by remember(session.email) {
        mutableStateOf("")
    }

    var localRefresh by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(
        session.userId,
        session.email,
        refreshToken,
        localRefresh
    ) {
        loading = true
        errorMessage = ""

        var userId = session.userId

        if (
            userId <= 0 &&
            session.email.isNotBlank()
        ) {
            val identity =
                UserIdentityApiService.resolveUserId(
                    session.email
                )

            if (identity.optBoolean("success", false)) {
                userId = identity.optInt("user_id", 0)

                if (userId > 0) {
                    effectiveUserId = userId

                    saveSession(
                        context,
                        session.copy(userId = userId)
                    )
                }
            }
        } else {
            effectiveUserId = userId
        }

        if (userId <= 0) {
            loading = false
            errorMessage = ""
            return@LaunchedEffect
        }

        try {
            val summary =
                WorkoutApiService.getTodaySummary(
                    userId
                )

            if (summary.optBoolean("success", false)) {
                summaryResponse = summary
            } else if (!summary.isInvalidUserResponse()) {
                errorMessage =
                    summary.homeMessage(
                        "Could not load today's workout summary."
                    )
            }

            val goal =
                GoalCenterApiService.getUserGoal(
                    userId
                )

            when {
                goal.optBoolean("success", false) &&
                        goal.optBoolean("exists", false) -> {
                    savedGoal =
                        goalCenterDataFromJson(
                            goal.optJSONObject("goal")
                                ?: JSONObject(),
                            session.copy(userId = userId)
                        )
                }

                goal.optBoolean("success", false) -> {
                    savedGoal = null
                }

                !goal.isInvalidUserResponse() &&
                        errorMessage.isBlank() -> {
                    errorMessage =
                        goal.homeMessage(
                            "Could not load your saved goal."
                        )
                }
            }
        } catch (exception: Exception) {
            errorMessage =
                "Cannot load home data: " +
                        (exception.message ?: "Unknown error")
        } finally {
            loading = false
        }
    }

    val summarySuccess =
        summaryResponse.optBoolean(
            "success",
            false
        )

    val savedWorkouts =
        if (summarySuccess) {
            summaryResponse.optInt(
                "total_workouts",
                0
            )
        } else {
            0
        }

    val savedSeconds =
        if (summarySuccess) {
            summaryResponse.optInt(
                "total_duration_seconds",
                0
            )
        } else {
            0
        }

    val savedDistance =
        if (summarySuccess) {
            summaryResponse.optDouble(
                "total_distance_km",
                0.0
            )
        } else {
            0.0
        }

    val savedCalories =
        if (summarySuccess) {
            summaryResponse.optDouble(
                "total_calories",
                0.0
            )
        } else {
            0.0
        }

    val activeWorkouts =
        ActiveWorkoutManager.activeWorkouts
            .filter { workout ->
                !workout.isFinished &&
                        (
                                workout.userId == effectiveUserId ||
                                        workout.userId == session.userId ||
                                        (
                                                session.userId <= 0 &&
                                                        workout.userId <= 0
                                                )
                                )
            }

    val liveSeconds =
        activeWorkouts.sumOf {
            it.elapsedSeconds
        }

    val liveDistance =
        activeWorkouts.sumOf {
            it.distanceKm
        }

    val liveCalories =
        activeWorkouts.sumOf {
            it.calories
        }

    val totalSeconds =
        savedSeconds + liveSeconds

    val totalDistance =
        savedDistance + liveDistance

    val totalCalories =
        savedCalories + liveCalories

    val targetCalories =
        savedGoal
            ?.workoutCaloriesTarget
            ?.toDoubleOrNull()
            ?.takeIf { it > 0.0 }
            ?: 500.0

    val progress =
        (totalCalories / targetCalories)
            .toFloat()
            .coerceIn(0f, 1f)

    val completed =
        totalCalories >= targetCalories

    val remaining =
        (targetCalories - totalCalories)
            .coerceAtLeast(0.0)

    val above =
        (totalCalories - targetCalories)
            .coerceAtLeast(0.0)

    val activityStats: JSONArray =
        if (summarySuccess) {
            summaryResponse.optJSONArray(
                "activity_stats"
            ) ?: JSONArray()
        } else {
            JSONArray()
        }

    val topActivity =
        activityStats
            .optJSONObject(0)
            ?.optString("activity_type")
            ?.takeIf { it.isNotBlank() }
            ?: activeWorkouts
                .firstOrNull()
                ?.activityName
            ?: "No activity yet"

    return HomeGoalDashboardState(
        loading = loading,
        effectiveUserId = effectiveUserId,
        savedGoal = savedGoal,
        totalWorkouts = savedWorkouts,
        totalSeconds = totalSeconds,
        totalDistanceKm = totalDistance,
        totalCalories = totalCalories,
        targetCalories = targetCalories,
        remainingCalories = remaining,
        caloriesAboveGoal = above,
        progress = progress,
        completed = completed,
        topActivity = topActivity,
        activeWorkoutCount = activeWorkouts.size,
        errorMessage = errorMessage,
        refresh = {
            localRefresh += 1
        }
    )
}

@Composable
fun GoalAwareTodayFitnessPlanCard(
    state: HomeGoalDashboardState,
    onStartWorkout: () -> Unit,
    onOpenGoal: () -> Unit
) {
    val animatedProgress by
    animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(900),
        label = "home_goal_progress"
    )

    val animatedCalories by
    animateFloatAsState(
        targetValue =
            state.totalCalories.toFloat(),
        animationSpec = tween(750),
        label = "home_goal_calories"
    )

    val colors =
        if (state.completed) {
            listOf(
                Color(0xFF064E3B),
                Color(0xFF059669),
                Color(0xFF22C55E)
            )
        } else {
            listOf(
                Color(0xFF07152F),
                Color(0xFF1D4ED8),
                Color(0xFF38BDF8)
            )
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(colors)
                )
                .padding(22.dp)
        ) {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(9.dp)
            ) {
                Text(
                    text =
                        if (state.completed) {
                            "Today's Goal Complete! 🎉"
                        } else {
                            "Today's Fitness Plan"
                        },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text =
                        when {
                            state.loading ->
                                "Loading saved goal and workout totals..."

                            state.savedGoal != null ->
                                "${state.totalWorkouts} workouts completed • " +
                                        "${state.savedGoal.goalType} • " +
                                        state.savedGoal.activityFocus

                            else ->
                                "${state.totalWorkouts} workouts completed • " +
                                        "Using 500 kcal until you set a goal"
                        },
                    color = Color(0xFFE0F2FE),
                    fontSize = 13.sp
                )

                if (state.activeWorkoutCount > 0) {
                    Text(
                        text =
                            "${state.activeWorkoutCount} active workout" +
                                    if (state.activeWorkoutCount == 1) {
                                        " • live calories included"
                                    } else {
                                        "s • live calories included"
                                    },
                        color = Color(0xFFFFE29A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    Modifier.height(5.dp)
                )

                if (state.loading) {
                    CircularProgressIndicator(
                        color = Color.White
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(11.dp)
                            .background(
                                Color.White.copy(
                                    alpha = 0.24f
                                ),
                                RoundedCornerShape(50.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(
                                    animatedProgress
                                        .coerceIn(0f, 1f)
                                )
                                .height(11.dp)
                                .background(
                                    Color(0xFFFFC857),
                                    RoundedCornerShape(50.dp)
                                )
                        )
                    }

                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text =
                                String.format(
                                    Locale.US,
                                    "%.1f / %.0f kcal",
                                    animatedCalories,
                                    state.targetCalories
                                ),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(
                            Modifier.weight(1f)
                        )

                        Text(
                            text =
                                "${state.progressPercent}%",
                            color = Color(0xFFFFE29A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Text(
                        text =
                            if (state.completed) {
                                String.format(
                                    Locale.US,
                                    "Goal reached • %.1f kcal above target",
                                    state.caloriesAboveGoal
                                )
                            } else {
                                String.format(
                                    Locale.US,
                                    "%.1f kcal remaining today",
                                    state.remainingCalories
                                )
                            },
                        color =
                            Color.White.copy(
                                alpha = 0.90f
                            ),
                        fontSize = 12.sp
                    )

                    Text(
                        text =
                            if (state.savedGoal != null) {
                                "Target loaded from Goal Center: Workout Calories Burn"
                            } else {
                                "Set Workout Calories Burn to replace the 500 kcal fallback"
                            },
                        color =
                            Color.White.copy(
                                alpha = 0.72f
                            ),
                        fontSize = 10.sp
                    )
                }

                Button(
                    onClick = onStartWorkout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text =
                            if (state.completed) {
                                "Keep Moving"
                            } else {
                                "Start New Workout"
                            },
                        color = Color(0xFF172554),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text =
                        if (state.savedGoal == null) {
                            "Set your personal goal"
                        } else {
                            "View or change goal"
                        },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = onOpenGoal)
                        .padding(vertical = 3.dp)
                )
            }
        }
    }
}

private fun JSONObject.isInvalidUserResponse():
        Boolean =
    optInt("http_code", 0) == 422 &&
            optString("message")
                .contains(
                    "user",
                    ignoreCase = true
                )

private fun JSONObject.homeMessage(
    fallback: String
): String {
    val message =
        optString(
            "message",
            fallback
        ).ifBlank {
            fallback
        }

    val code =
        optInt(
            "http_code",
            0
        )

    return if (code > 0) {
        "$message\nHTTP: $code"
    } else {
        message
    }
}