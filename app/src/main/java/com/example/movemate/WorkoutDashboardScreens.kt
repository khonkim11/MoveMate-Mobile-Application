package com.example.movemate
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import java.text.SimpleDateFormat
import java.util.Date

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

@Composable
fun SummaryScreen(
    session: UserSession,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val measurementSystem =
        LocalMeasurementSystem.current

    var loading by remember(session.userId) {
        mutableStateOf(true)
    }

    var refreshToken by remember {
        mutableStateOf(0)
    }

    var response by remember(session.userId) {
        mutableStateOf(JSONObject())
    }

    var goalResponse by remember(session.userId) {
        mutableStateOf(JSONObject())
    }

    LaunchedEffect(
        session.userId,
        refreshToken
    ) {
        loading = true

        response =
            WorkoutApiService.getTodaySummary(
                session.userId
            )

        goalResponse =
            GoalCenterApiService.getUserGoal(
                session.userId
            )

        loading = false
    }

    val totalWorkouts =
        response.optInt(
            "total_workouts",
            0
        )

    val totalCalories =
        response.optDouble(
            "total_calories",
            0.0
        )

    val totalDurationSeconds =
        response.optInt(
            "total_duration_seconds",
            0
        )

    val totalDistanceKm =
        response.optDouble(
            "total_distance_km",
            0.0
        )

    val goalTarget =
        if (
            goalResponse.optBoolean(
                "success",
                false
            ) &&
            goalResponse.optBoolean(
                "exists",
                false
            )
        ) {
            goalResponse
                .optJSONObject("goal")
                ?.optDouble(
                    "workout_calories_target",
                    0.0
                )
                ?: 0.0
        } else {
            0.0
        }

    val goalProgress =
        if (goalTarget > 0.0) {
            (
                    totalCalories /
                            goalTarget
                    ).toFloat()
                .coerceIn(0f, 1f)
        } else {
            0f
        }

    val goalCompleted =
        goalTarget > 0.0 &&
                totalCalories >= goalTarget

    val animatedWorkouts by animateIntAsState(
        targetValue = totalWorkouts,
        animationSpec = tween(
            durationMillis = 750
        ),
        label = "summary_workouts"
    )

    val animatedCalories by animateFloatAsState(
        targetValue = totalCalories.toFloat(),
        animationSpec = tween(
            durationMillis = 950
        ),
        label = "summary_calories"
    )

    val animatedDuration by animateIntAsState(
        targetValue =
            totalDurationSeconds,
        animationSpec = tween(
            durationMillis = 850
        ),
        label = "summary_duration"
    )

    val animatedDistance by animateFloatAsState(
        targetValue =
            totalDistanceKm.toFloat(),
        animationSpec = tween(
            durationMillis = 950
        ),
        label = "summary_distance"
    )

    val animatedGoalProgress by animateFloatAsState(
        targetValue = goalProgress,
        animationSpec = tween(
            durationMillis = 1100
        ),
        label = "summary_goal_progress"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        TodaySummaryAnimatedBackground(
            goalCompleted = goalCompleted
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 18.dp,
                    vertical = 16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack
                ) {
                    Text(
                        text = "‹ Back",
                        color = Color.White,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                Text(
                    text = "Today Summary",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        refreshToken += 1
                    },
                    enabled = !loading
                ) {
                    Text(
                        text = "Refresh",
                        color =
                            if (loading) {
                                Color.White.copy(
                                    alpha = 0.45f
                                )
                            } else {
                                Color.White
                            }
                    )
                }
            }

            Text(
                text = todaySummaryDateText(),
                color = Color.White.copy(
                    alpha = 0.82f
                ),
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color.White
                        )

                        Text(
                            text =
                                "Loading today's fitness results...",
                            color = Color.White
                        )
                    }
                }
            } else if (
                !response.optBoolean(
                    "success",
                    false
                )
            ) {
                TodaySummaryErrorCard(
                    message =
                        response.optString(
                            "message",
                            "Could not load today's summary."
                        ),
                    onRetry = {
                        refreshToken += 1
                    }
                )
            } else {
                TodaySummaryHeroCard(
                    name = session.fullName,
                    calories =
                        animatedCalories.toDouble(),
                    goalTarget = goalTarget,
                    progress =
                        animatedGoalProgress,
                    completed = goalCompleted
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    TodaySummaryMetricCard(
                        emoji = "🏋️",
                        title = "Workouts",
                        value =
                            animatedWorkouts.toString(),
                        note = "completed today",
                        accent =
                            Color(0xFF7C3AED),
                        modifier =
                            Modifier.weight(1f)
                    )

                    TodaySummaryMetricCard(
                        emoji = "🔥",
                        title = "Calories",
                        value =
                            animatedCalories
                                .toDouble()
                                .oneDecimal(),
                        note = "kcal burned",
                        accent =
                            Color(0xFFEA580C),
                        modifier =
                            Modifier.weight(1f)
                    )
                }

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    TodaySummaryMetricCard(
                        emoji = "⏱️",
                        title = "Duration",
                        value =
                            formatWorkoutTime(
                                animatedDuration
                            ),
                        note = "active time",
                        accent =
                            Color(0xFF0891B2),
                        modifier =
                            Modifier.weight(1f)
                    )

                    TodaySummaryMetricCard(
                        emoji = "📍",
                        title = "Distance",
                        value =
                            formatDistanceValue(
                                animatedDistance
                                    .toDouble(),
                                measurementSystem
                            ),
                        note =
                            "${distanceUnitLabel(measurementSystem)} completed",
                        accent =
                            Color(0xFF16A34A),
                        modifier =
                            Modifier.weight(1f)
                    )
                }

                val awards =
                    response.optJSONObject(
                        "award_counts"
                    )

                if (awards != null) {
                    Text(
                        text = "Today's Achievements",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                9.dp
                            )
                    ) {
                        TodaySummaryAwardCard(
                            emoji = "🥈",
                            title = "Silver",
                            count =
                                awards.optInt(
                                    "SILVER",
                                    0
                                ),
                            accent =
                                Color(0xFF94A3B8),
                            modifier =
                                Modifier.weight(1f)
                        )

                        TodaySummaryAwardCard(
                            emoji = "🥇",
                            title = "Gold",
                            count =
                                awards.optInt(
                                    "GOLD",
                                    0
                                ),
                            accent =
                                Color(0xFFF59E0B),
                            modifier =
                                Modifier.weight(1f)
                        )

                        TodaySummaryAwardCard(
                            emoji = "💎",
                            title = "Diamond",
                            count =
                                awards.optInt(
                                    "DIAMOND",
                                    0
                                ),
                            accent =
                                Color(0xFF06B6D4),
                            modifier =
                                Modifier.weight(1f)
                        )
                    }
                }

                val lastWorkout =
                    response.optJSONObject(
                        "last_workout"
                    )

                if (lastWorkout != null) {
                    TodaySummaryLastWorkoutCard(
                        workout = lastWorkout
                    )
                }

                val activityStats =
                    response.optJSONArray(
                        "activity_stats"
                    ) ?: JSONArray()

                if (
                    totalWorkouts == 0 ||
                    activityStats.length() == 0
                ) {
                    TodaySummaryEmptyState()
                } else {
                    Text(
                        text = "Activity Breakdown",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    val maximumActivityCalories =
                        todaySummaryMaximumCalories(
                            activityStats
                        )

                    for (
                    index in 0 until
                            activityStats.length()
                    ) {
                        val activity =
                            activityStats
                                .optJSONObject(index)
                                ?: JSONObject()

                        val activityCalories =
                            activity.optDouble(
                                "calories",
                                0.0
                            )

                        TodaySummaryActivityCard(
                            activityName =
                                activity.optString(
                                    "activity_type",
                                    "Workout"
                                ),
                            workoutCount =
                                activity.optInt(
                                    "workout_count",
                                    0
                                ),
                            durationSeconds =
                                activity.optInt(
                                    "duration_seconds",
                                    0
                                ),
                            calories =
                                activityCalories,
                            distanceKm =
                                activity.optDouble(
                                    "distance_km",
                                    0.0
                                ),
                            progress =
                                (
                                        activityCalories /
                                                maximumActivityCalories
                                        ).toFloat()
                                    .coerceIn(
                                        0f,
                                        1f
                                    )
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )
        }
    }
}

@Composable
private fun TodaySummaryAnimatedBackground(
    goalCompleted: Boolean
) {
    val transition =
        rememberInfiniteTransition(
            label =
                "today_summary_background"
        )

    val moveOne by transition.animateFloat(
        initialValue = -80f,
        targetValue = 170f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 6200,
                    easing = LinearEasing
                ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "summary_blob_one"
    )

    val moveTwo by transition.animateFloat(
        initialValue = 150f,
        targetValue = -120f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 7600,
                    easing = LinearEasing
                ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "summary_blob_two"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.16f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 3000
                ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "summary_blob_pulse"
    )

    val backgroundColors =
        if (goalCompleted) {
            listOf(
                Color(0xFF052E16),
                Color(0xFF15803D),
                Color(0xFF22C55E)
            )
        } else {
            listOf(
                Color(0xFF020617),
                Color(0xFF172554),
                Color(0xFF2563EB),
                Color(0xFF0EA5E9)
            )
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    backgroundColors
                )
            )
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCircle(
                color = Color.White.copy(
                    alpha = 0.09f
                ),
                radius = 170f * pulse,
                center = Offset(
                    x = size.width -
                            70f +
                            moveOne,
                    y = 180f
                )
            )

            drawCircle(
                color = Color(0xFFFFD166)
                    .copy(
                        alpha = 0.14f
                    ),
                radius = 240f,
                center = Offset(
                    x = -80f + moveTwo,
                    y = size.height * 0.42f
                )
            )

            drawCircle(
                color = Color.White.copy(
                    alpha = 0.07f
                ),
                radius = 290f,
                center = Offset(
                    x = size.width * 0.80f -
                            moveTwo,
                    y = size.height - 120f
                )
            )
        }
    }
}

@Composable
private fun TodaySummaryHeroCard(
    name: String,
    calories: Double,
    goalTarget: Double,
    progress: Float,
    completed: Boolean
) {
    val transition =
        rememberInfiniteTransition(
            label = "summary_hero"
        )

    val pulseScale by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 1800
                ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "summary_hero_pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(
                if (completed) {
                    pulseScale
                } else {
                    1f
                }
            ),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                Color.White.copy(
                    alpha = 0.96f
                )
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            TodaySummaryGoalRing(
                progress = progress,
                completed = completed
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text =
                        if (completed) {
                            "Goal Complete! 🎉"
                        } else {
                            "Keep Moving Today"
                        },
                    color =
                        if (completed) {
                            Color(0xFF15803D)
                        } else {
                            Color(0xFF1D4ED8)
                        },
                    fontSize = 20.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        name
                            .ifBlank {
                                "MoveMate User"
                            },
                    color = Color(0xFF0F172A),
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        if (goalTarget > 0.0) {
                            "${calories.oneDecimal()} / " +
                                    "${goalTarget.oneDecimal()} kcal"
                        } else {
                            "${calories.oneDecimal()} kcal burned today"
                        },
                    color = Color(0xFF475569),
                    fontSize = 13.sp
                )

                Text(
                    text =
                        if (goalTarget > 0.0) {
                            if (completed) {
                                "You reached today's workout-calorie target."
                            } else {
                                "${(
                                        goalTarget -
                                                calories
                                        ).coerceAtLeast(
                                        0.0
                                    ).oneDecimal()} kcal remaining"
                            }
                        } else {
                            "Set a workout-calorie target in Goal Center."
                        },
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun TodaySummaryGoalRing(
    progress: Float,
    completed: Boolean
) {
    Box(
        modifier = Modifier.size(105.dp),
        contentAlignment =
            Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCircle(
                color = Color(0xFFE2E8F0),
                style = Stroke(
                    width = 13f
                )
            )

            drawArc(
                color =
                    if (completed) {
                        Color(0xFF22C55E)
                    } else {
                        Color(0xFF2563EB)
                    },
                startAngle = -90f,
                sweepAngle =
                    360f *
                            progress.coerceIn(
                                0f,
                                1f
                            ),
                useCenter = false,
                style = Stroke(
                    width = 13f,
                    cap = StrokeCap.Round
                )
            )
        }

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    if (completed) {
                        "✓"
                    } else {
                        "${(progress * 100).toInt()}%"
                    },
                color =
                    if (completed) {
                        Color(0xFF15803D)
                    } else {
                        Color(0xFF1D4ED8)
                    },
                fontSize =
                    if (completed) {
                        34.sp
                    } else {
                        21.sp
                    },
                fontWeight =
                    FontWeight.ExtraBold
            )

            if (!completed) {
                Text(
                    text = "goal",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun TodaySummaryMetricCard(
    emoji: String,
    title: String,
    value: String,
    note: String,
    accent: Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                Color.White.copy(
                    alpha = 0.96f
                )
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 5.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(15.dp),
            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        accent.copy(
                            alpha = 0.13f
                        )
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 20.sp
                )
            }

            Text(
                text = title,
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                text = value,
                color = Color(0xFF0F172A),
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text = note,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TodaySummaryAwardCard(
    emoji: String,
    title: String,
    count: Int,
    accent: Color,
    modifier: Modifier
) {
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(
            durationMillis = 700
        ),
        label = "summary_award_$title"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                Color.White.copy(
                    alpha = 0.95f
                )
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
    ) {
        Column(
            modifier = Modifier.padding(
                vertical = 14.dp,
                horizontal = 8.dp
            ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 29.sp
            )

            Text(
                text =
                    animatedCount.toString(),
                color = accent,
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text = title,
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TodaySummaryLastWorkoutCard(
    workout: JSONObject
) {
    val activityName =
        workout.optString(
            "activity_type",
            "Workout"
        )

    val award =
        workout.optString(
            "award_tier",
            "STARTER"
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                summaryActivityColor(
                    activityName
                ).copy(
                    alpha = 0.92f
                )
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 7.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    summaryActivityEmoji(
                        activityName
                    ),
                fontSize = 43.sp
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp),
                verticalArrangement =
                    Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Last Workout",
                    color = Color.White.copy(
                        alpha = 0.78f
                    ),
                    fontSize = 11.sp
                )

                Text(
                    text =
                        "$activityName • " +
                                workout.optString(
                                    "level_name",
                                    "Normal"
                                ),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        "${formatWorkoutTime(
                            workout.optInt(
                                "duration_seconds",
                                0
                            )
                        )} • ${
                            workout
                                .optDouble(
                                    "calories",
                                    0.0
                                )
                                .oneDecimal()
                        } kcal",
                    color = Color.White.copy(
                        alpha = 0.86f
                    ),
                    fontSize = 12.sp
                )
            }

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    text = awardEmoji(award),
                    fontSize = 31.sp
                )

                Text(
                    text = award,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TodaySummaryActivityCard(
    activityName: String,
    workoutCount: Int,
    durationSeconds: Int,
    calories: Double,
    distanceKm: Double,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue =
            progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 1000
        ),
        label =
            "summary_activity_$activityName"
    )

    val accent =
        summaryActivityColor(
            activityName
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                Color.White.copy(
                    alpha = 0.96f
                )
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(
                            RoundedCornerShape(
                                16.dp
                            )
                        )
                        .background(
                            accent.copy(
                                alpha = 0.13f
                            )
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            summaryActivityEmoji(
                                activityName
                            ),
                        fontSize = 25.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 11.dp)
                ) {
                    Text(
                        text = activityName,
                        color = Color(0xFF0F172A),
                        fontSize = 17.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            "$workoutCount completed • " +
                                    formatWorkoutTime(
                                        durationSeconds
                                    ),
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }

                Text(
                    text =
                        "${calories.oneDecimal()} kcal",
                    color = accent,
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }

            LinearProgressIndicator(
                progress =
                    animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(
                        RoundedCornerShape(
                            8.dp
                        )
                    ),
                color = accent,
                trackColor =
                    accent.copy(
                        alpha = 0.14f
                    )
            )

            Text(
                text =
                    if (distanceKm > 0.0) {
                        "${
                            formatDistance(
                                distanceKm,
                                LocalMeasurementSystem.current
                            )
                        } completed"
                    } else {
                        "Stationary workout"
                    },
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TodaySummaryEmptyState() {
    val transition =
        rememberInfiniteTransition(
            label = "summary_empty"
        )

    val scale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 1600
                ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "summary_empty_pulse"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                Color.White.copy(
                    alpha = 0.95f
                )
        )
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "🏃",
                fontSize = 54.sp,
                modifier =
                    Modifier.scale(scale)
            )

            Text(
                text =
                    "Your day is ready",
                color = Color(0xFF0F172A),
                fontSize = 21.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    "Complete a workout and your animated summary will appear here.",
                color = Color(0xFF64748B),
                textAlign =
                    TextAlign.Center
            )
        }
    }
}

@Composable
private fun TodaySummaryErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text =
                    "Could not load summary",
                color = Color(0xFFB91C1C),
                fontSize = 18.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text = message,
                color = Color(0xFF991B1B),
                fontSize = 12.sp
            )

            TextButton(
                onClick = onRetry
            ) {
                Text(
                    text = "Try Again",
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}

private fun todaySummaryMaximumCalories(
    activities: JSONArray
): Double {
    var maximum = 1.0

    for (
    index in 0 until
            activities.length()
    ) {
        maximum = maxOf(
            maximum,
            activities
                .optJSONObject(index)
                ?.optDouble(
                    "calories",
                    0.0
                )
                ?: 0.0
        )
    }

    return maximum
}

private fun summaryActivityEmoji(
    activityName: String
): String {
    return when (
        activityName.lowercase(
            Locale.US
        )
    ) {
        "running" -> "🏃"
        "cycling" -> "🚴"
        "walking" -> "🚶"
        "weightlifting" -> "🏋️"
        "yoga" -> "🧘"
        "hiit" -> "🔥"
        "swimming" -> "🏊"
        "rowing" -> "🚣"
        else -> "💪"
    }
}

private fun summaryActivityColor(
    activityName: String
): Color {
    return when (
        activityName.lowercase(
            Locale.US
        )
    ) {
        "running" ->
            Color(0xFF2563EB)

        "cycling" ->
            Color(0xFF0891B2)

        "walking" ->
            Color(0xFF16A34A)

        "weightlifting" ->
            Color(0xFF7C3AED)

        "yoga" ->
            Color(0xFFDB2777)

        "hiit" ->
            Color(0xFFEA580C)

        "swimming" ->
            Color(0xFF0284C7)

        "rowing" ->
            Color(0xFF0F766E)

        else ->
            Color(0xFF475569)
    }
}

private fun todaySummaryDateText(): String {
    return SimpleDateFormat(
        "EEEE, dd MMMM",
        Locale.US
    ).format(Date())
}

@Composable
fun ProgressScreen(
    session: UserSession,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedDays by remember {
        mutableStateOf(7)
    }

    var loading by remember(
        session.userId,
        selectedDays
    ) {
        mutableStateOf(true)
    }

    var response by remember(
        session.userId,
        selectedDays
    ) {
        mutableStateOf(JSONObject())
    }

    LaunchedEffect(
        session.userId,
        selectedDays
    ) {
        loading = true

        response =
            WorkoutApiService.getProgress(
                userId = session.userId,
                days = selectedDays
            )

        loading = false
    }

    WorkoutDataPage(
        title = "Progress",
        subtitle =
            "Review consistency, streaks and daily performance",
        onBack = onBack
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState()
                ),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            listOf(7, 30).forEach { days ->
                FilterChip(
                    selected =
                        selectedDays == days,
                    onClick = {
                        selectedDays = days
                    },
                    label = {
                        Text("$days Days")
                    }
                )
            }
        }

        when {
            loading -> {
                CircularProgressIndicator()
            }

            !response.optBoolean("success", false) -> {
                WorkoutDataMessage(
                    response.optString(
                        "message",
                        "Could not load progress."
                    )
                )
            }

            else -> {
                val summary =
                    response.optJSONObject("summary")
                        ?: JSONObject()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    WorkoutDataMetric(
                        title = "Calories",
                        value = "${
                            summary
                                .optDouble(
                                    "total_calories",
                                    0.0
                                )
                                .oneDecimal()
                        } kcal",
                        modifier = Modifier.weight(1f)
                    )

                    WorkoutDataMetric(
                        title = "Active Days",
                        value = summary
                            .optInt(
                                "active_days",
                                0
                            )
                            .toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    WorkoutDataMetric(
                        title = "Current Streak",
                        value = "${
                            summary.optInt(
                                "current_streak",
                                0
                            )
                        } days",
                        modifier = Modifier.weight(1f)
                    )

                    WorkoutDataMetric(
                        title = "Best Day",
                        value = "${
                            summary
                                .optDouble(
                                    "best_day_calories",
                                    0.0
                                )
                                .oneDecimal()
                        } kcal",
                        modifier = Modifier.weight(1f)
                    )
                }

                WorkoutDataCard(
                    title = "Progress Insight",
                    value = progressInsight(
                        activeDays =
                            summary.optInt(
                                "active_days",
                                0
                            ),
                        selectedDays =
                            selectedDays,
                        currentStreak =
                            summary.optInt(
                                "current_streak",
                                0
                            )
                    ),
                    note =
                        "Average: ${
                            summary
                                .optDouble(
                                    "average_daily_calories",
                                    0.0
                                )
                                .oneDecimal()
                        } kcal per day"
                )

                Text(
                    text = "Daily Activity",
                    color = Color(0xFF172554),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                val rows =
                    response.optJSONArray("progress")
                        ?: JSONArray()

                val maximumCalories =
                    maxProgressCalories(rows)

                if (rows.length() == 0) {
                    WorkoutDataMessage(
                        "No progress records yet. Complete a workout first."
                    )
                } else {
                    for (
                    index in 0 until rows.length()
                    ) {
                        val item =
                            rows.optJSONObject(index)
                                ?: JSONObject()

                        val calories =
                            item.optDouble(
                                "calories",
                                0.0
                            )

                        WorkoutProgressItem(
                            date = item.optString(
                                "workout_date",
                                "Unknown date"
                            ),
                            details = "${
                                item.optInt(
                                    "workout_count",
                                    0
                                )
                            } workouts • ${
                                calories.oneDecimal()
                            } kcal",
                            note = "${
                                formatWorkoutTime(
                                    item.optInt(
                                        "duration_seconds",
                                        0
                                    )
                                )
                            } • ${
                                formatDistance(
                                    item.optDouble(
                                        "distance_km",
                                        0.0
                                    ),
                                    LocalMeasurementSystem.current
                                )
                            }",
                            progress =
                                (
                                        calories /
                                                maximumCalories
                                        ).toFloat()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutDataPage(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFEFF6FF)
                    )
                )
            )
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) {
            Text(
                text = "‹ Back",
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = title,
            color = Color(0xFF0F172A),
            fontSize = 29.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = subtitle,
            color = Color(0xFF64748B)
        )

        content()
    }
}

@Composable
private fun WorkoutDataMetric(
    title: String,
    value: String,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )

            Text(
                text = value,
                color = Color(0xFF0F172A),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun WorkoutDataCard(
    title: String,
    value: String,
    note: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = value,
                color = Color(0xFF0F172A),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = note,
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun WorkoutProgressItem(
    date: String,
    details: String,
    note: String,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = date,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = details,
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }

            LinearProgressIndicator(
                progress = progress.coerceIn(
                    0f,
                    1f
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Text(
                text = note,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun WorkoutDataMessage(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF7ED)
        )
    ) {
        Text(
            text = message,
            color = Color(0xFF9A3412),
            modifier = Modifier.padding(14.dp)
        )
    }
}

private fun maxProgressCalories(
    rows: JSONArray
): Double {
    var maximum = 1.0

    for (index in 0 until rows.length()) {
        maximum = maxOf(
            maximum,
            rows
                .optJSONObject(index)
                ?.optDouble(
                    "calories",
                    0.0
                )
                ?: 0.0
        )
    }

    return maximum
}

private fun progressInsight(
    activeDays: Int,
    selectedDays: Int,
    currentStreak: Int
): String {
    return when {
        currentStreak >= 7 ->
            "Excellent consistency. Your streak is building a strong fitness habit."

        activeDays >= selectedDays * 0.70 ->
            "Strong activity frequency. Keep your current routine."

        activeDays >= selectedDays * 0.40 ->
            "Good progress. Add one more active day to improve consistency."

        activeDays > 0 ->
            "You have started. Short, regular sessions can build a stronger streak."

        else ->
            "Complete your first workout to begin progress tracking."
    }
}



private fun awardEmoji(
    award: String
): String {
    return when (
        award.uppercase(Locale.US)
    ) {
        "SILVER" -> "🥈"
        "GOLD" -> "🥇"
        "DIAMOND" -> "💎"
        else -> "🌱"
    }
}

private fun Double.oneDecimal(): String {
    return String.format(
        Locale.US,
        "%.1f",
        this
    )
}
