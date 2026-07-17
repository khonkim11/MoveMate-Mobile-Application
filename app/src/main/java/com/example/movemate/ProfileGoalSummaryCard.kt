package com.example.movemate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import androidx.compose.foundation.layout.BoxScope

@Composable
fun ProfileGoalSummaryCard(
    session: UserSession,
    onOpenGoal: () -> Unit
) {
    var goal by remember(session.userId, session.email) {
        mutableStateOf<GoalCenterData?>(null)
    }

    var message by remember(session.userId, session.email) {
        mutableStateOf("Loading saved goal...")
    }

    var loading by remember(session.userId, session.email) {
        mutableStateOf(true)
    }

    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    LaunchedEffect(session.userId, session.email) {
        loading = true

        if (session.userId <= 0) {
            message = "Login again to load your saved goal."
            goal = null
            loading = false
            return@LaunchedEffect
        }

        try {
            val response = GoalCenterApiService.getUserGoal(session.userId)

            if (response.optBoolean("success", false) && response.optBoolean("exists", false)) {
                val goalJson = response.optJSONObject("goal") ?: JSONObject()
                goal = goalCenterDataFromJson(goalJson, session)
                message = ""
            } else {
                goal = null
                message = "No saved goal yet. Tap Set Goal to create one."
            }
        } catch (e: Exception) {
            goal = null
            message = "Could not load goal: ${e.message ?: "Unknown error"}"
        } finally {
            loading = false
        }
    }

    val savedGoal = goal
    val buttonScale by animateFloatAsState(
        targetValue = if (savedGoal == null) 1.0f else 1.02f,
        animationSpec = tween(durationMillis = 450),
        label = "goal_button_scale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(650)) +
                slideInVertically(
                    animationSpec = tween(650),
                    initialOffsetY = { it / 4 }
                )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    shadowElevation = 14f
                },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0F172A),
                                Color(0xFF1D4ED8),
                                Color(0xFF06B6D4)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                ProfileGoalAnimatedGlow()

                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProfileGoalHeader(
                        hasGoal = savedGoal != null,
                        loading = loading
                    )

                    if (loading) {
                        ProfileGoalLoadingBox()
                    } else if (savedGoal == null) {
                        ProfileGoalEmptyBox(message = message)
                    } else {
                        ProfileGoalSavedContent(savedGoal = savedGoal)
                    }

                    Button(
                        onClick = onOpenGoal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .graphicsLayer {
                                scaleX = buttonScale
                                scaleY = buttonScale
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1D4ED8)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 1.dp
                        )
                    ) {
                        Text(
                            text = if (savedGoal == null) "Set Goal" else "Update Goal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.ProfileGoalAnimatedGlow() {
    val transition = rememberInfiniteTransition(label = "profile_goal_glow")

    val moveOne by transition.animateFloat(
        initialValue = 0f,
        targetValue = 95f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "profile_goal_glow_one"
    )

    val moveTwo by transition.animateFloat(
        initialValue = 0f,
        targetValue = 130f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "profile_goal_glow_two"
    )

    Canvas(
        modifier = Modifier.matchParentSize()
    ) {
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = 150f,
            center = Offset(
                x = size.width - 40f + moveOne,
                y = 24f
            )
        )

        drawCircle(
            color = Color(0xFFFFC96B).copy(alpha = 0.18f),
            radius = 175f,
            center = Offset(
                x = -30f + moveTwo,
                y = size.height - 28f
            )
        )

        drawCircle(
            color = Color(0xFF22C55E).copy(alpha = 0.14f),
            radius = 120f,
            center = Offset(
                x = size.width * 0.78f - moveTwo / 2f,
                y = size.height * 0.72f
            )
        )
    }
}

@Composable
private fun ProfileGoalHeader(
    hasGoal: Boolean,
    loading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (hasGoal) "🎯" else "✨",
                fontSize = 26.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "My Saved Goal",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = when {
                    loading -> "Syncing your latest target..."
                    hasGoal -> "Stay focused. Your plan is ready."
                    else -> "Create your next fitness target."
                },
                color = Color(0xFFE0F2FE),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileGoalLoadingBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Loading saved goal...",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.20f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun ProfileGoalEmptyBox(
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚀",
                fontSize = 34.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Pick a goal and let MoveMate guide your daily progress.",
                color = Color(0xFFE0F2FE),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileGoalSavedContent(
    savedGoal: GoalCenterData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileGoalMainTargetCard(savedGoal = savedGoal)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileGoalMetricChip(
                label = "Calories",
                value = "${savedGoal.dailyCaloriesTarget}",
                suffix = "kcal",
                modifier = Modifier.weight(1f)
            )

            ProfileGoalMetricChip(
                label = "Steps",
                value = "${savedGoal.stepsTarget}",
                suffix = "steps",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileGoalMetricChip(
                label = "Water",
                value = "${savedGoal.waterTargetMl}",
                suffix = "ml",
                modifier = Modifier.weight(1f)
            )

            ProfileGoalMetricChip(
                label = "Workout",
                value = "${savedGoal.workoutMinutesTarget}",
                suffix = "min",
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.13f))
                .padding(14.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                ProfileGoalLine("Activity", savedGoal.activityFocus)
                ProfileGoalLine("Intensity", savedGoal.intensity)
                ProfileGoalLine("Schedule", savedGoal.schedule)
                ProfileGoalLine("Workout Burn", "${savedGoal.workoutCaloriesTarget} kcal")
            }
        }
    }
}

@Composable
private fun ProfileGoalMainTargetCard(
    savedGoal: GoalCenterData
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Current Focus",
                    color = Color(0xFFBFDBFE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = savedGoal.goalType,
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Keep showing up. Your goal is already in motion.",
                    color = Color(0xFFE0F2FE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔥",
                    fontSize = 27.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileGoalMetricChip(
    label: String,
    value: String,
    suffix: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = label,
                color = Color(0xFFBFDBFE),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = suffix,
                color = Color(0xFFE0F2FE),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileGoalLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFBFDBFE),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
