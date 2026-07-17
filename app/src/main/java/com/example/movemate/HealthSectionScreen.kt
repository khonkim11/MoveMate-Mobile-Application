package com.example.movemate

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

private val HealthBackground = Color(0xFFF3F7FC)
private val HealthInk = Color(0xFF0F172A)
private val HealthMuted = Color(0xFF64748B)
private val HealthPrimary = Color(0xFF0F766E)
private val HealthCyan = Color(0xFF0891B2)
private val HealthBlue = Color(0xFF2563EB)
private val HealthGreen = Color(0xFF16A34A)
private val HealthCardShape = RoundedCornerShape(26.dp)

@Composable
fun HealthSectionScreen(
    session: UserSession,
    onBack: () -> Unit,
    onHome: () -> Unit = onBack,
    onActivity: () -> Unit = {},
    onSummary: () -> Unit = {},
    onProgress: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current

    var healthSession by remember(session.userId) {
        mutableStateOf(session)
    }

    var savedGoal by remember(session.userId, session.email) {
        mutableStateOf<GoalCenterData?>(null)
    }

    var loadingProfile by remember(session.userId) {
        mutableStateOf(true)
    }

    var loadingGoal by remember(session.userId) {
        mutableStateOf(true)
    }

    var message by remember(session.userId) {
        mutableStateOf("")
    }

    LaunchedEffect(session.userId) {
        if (session.userId <= 0) {
            loadingProfile = false
            loadingGoal = false
            message = "Please sign in again to load your health data."
            return@LaunchedEffect
        }

        loadingProfile = true
        message = ""

        try {
            val latestProfile =
                ProfileApiService.getProfile(session.userId)

            if (latestProfile != null) {
                healthSession = latestProfile
                saveSession(context, latestProfile)
            }
        } catch (exception: Exception) {
            message =
                "Could not load your latest profile: " +
                        (exception.message ?: "Unknown error")
        } finally {
            loadingProfile = false
        }
    }

    LaunchedEffect(
        session.userId,
        healthSession.weightKg,
        healthSession.heightCm
    ) {
        if (session.userId <= 0) {
            savedGoal = null
            loadingGoal = false
            return@LaunchedEffect
        }

        loadingGoal = true

        try {
            val response =
                GoalCenterApiService.getUserGoal(session.userId)

            savedGoal =
                if (
                    response.optBoolean("success", false) &&
                    response.optBoolean("exists", false)
                ) {
                    goalCenterDataFromJson(
                        json = response.optJSONObject("goal") ?: JSONObject(),
                        session = healthSession
                    )
                } else {
                    null
                }
        } catch (_: Exception) {
            savedGoal = null
        } finally {
            loadingGoal = false
        }
    }

    val bmi = calculateHealthBmi(
        weightKg = healthSession.weightKg,
        heightCm = healthSession.heightCm
    )

    val bmiStatus = healthBmiStatus(bmi)
    val score = healthScore(bmi)
    val healthyRange = healthyWeightRange(healthSession.heightCm)
    val bmiProgress = (bmi / 40.0).toFloat().coerceIn(0f, 1f)

    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(700),
        label = "health_score"
    )

    val animatedBmiProgress by animateFloatAsState(
        targetValue = bmiProgress,
        animationSpec = tween(800),
        label = "health_bmi_progress"
    )

    Scaffold(
        containerColor = HealthBackground,
        topBar = {
            HealthTopBar(
                onBack = onBack,
                onHome = onHome
            )
        },
        bottomBar = {
            HealthFooterMenu(
                onHome = onHome,
                onActivity = onActivity,
                onSummary = onSummary,
                onProgress = onProgress,
                onProfile = onProfile
            )
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFEFFCF8),
                            Color(0xFFF4F8FD),
                            Color.White
                        )
                    )
                ),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = scaffoldPadding.calculateTopPadding() + 14.dp,
                end = 18.dp,
                bottom = scaffoldPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item {
                HealthHeroCard(
                    session = healthSession,
                    score = animatedScore.roundToInt(),
                    bmi = bmi,
                    bmiStatus = bmiStatus,
                    loading = loadingProfile
                )
            }

            if (message.isNotBlank()) {
                item {
                    HealthMessageCard(message)
                }
            }

            item {
                HealthBodyMetrics(
                    session = healthSession,
                    bmi = bmi
                )
            }

            item {
                BmiOverviewCard(
                    bmi = bmi,
                    status = bmiStatus,
                    progress = animatedBmiProgress,
                    healthyRange = healthyRange
                )
            }

            item {
                HealthGoalSnapshot(
                    goal = savedGoal,
                    loading = loadingGoal
                )
            }

            item {
                HealthDailyGuide(
                    goal = savedGoal,
                    status = bmiStatus
                )
            }

            item {
                HealthAdviceCard(
                    bmi = bmi,
                    bmiStatus = bmiStatus,
                    weightKg = healthSession.weightKg,
                    healthyRange = healthyRange
                )
            }
        }
    }
}

@Composable
private fun HealthTopBar(
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HealthTopIcon("←", onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp)
            ) {
                Text(
                    text = "Health Overview",
                    color = HealthInk,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Body data, BMI and daily targets",
                    color = HealthMuted,
                    fontSize = 10.sp
                )
            }

            HealthTopIcon("⌂", onHome)
        }
    }
}

@Composable
private fun HealthTopIcon(
    symbol: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0xFFE6F7F3))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = HealthPrimary,
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun HealthHeroCard(
    session: UserSession,
    score: Int,
    bmi: Double,
    bmiStatus: String,
    loading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF052E2B),
                            Color(0xFF0F766E),
                            Color(0xFF0891B2)
                        )
                    )
                )
                .padding(21.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(34.dp)
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = score.toString(),
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "HEALTH",
                                color = Color.White.copy(alpha = 0.70f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = session.fullName.ifBlank { "MoveMate User" },
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = if (bmi > 0.0) {
                            "BMI ${String.format(Locale.US, "%.1f", bmi)} • $bmiStatus"
                        } else {
                            "Add weight and height to calculate BMI"
                        },
                        color = Color.White.copy(alpha = 0.86f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 5.dp)
                    )

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.padding(top = 13.dp)
                    ) {
                        Text(
                            text = healthStatusMessage(bmiStatus),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthBodyMetrics(
    session: UserSession,
    bmi: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Body measurements",
            color = HealthInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HealthMetricCard(
                modifier = Modifier.weight(1f),
                icon = "🎂",
                title = "Age",
                value = if (session.age > 0) "${session.age}" else "—",
                unit = "years",
                accent = HealthBlue
            )

            HealthMetricCard(
                modifier = Modifier.weight(1f),
                icon = "⚖️",
                title = "Weight",
                value = if (session.weightKg > 0.0) {
                    String.format(Locale.US, "%.1f", session.weightKg)
                } else {
                    "—"
                },
                unit = "kg",
                accent = HealthPrimary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HealthMetricCard(
                modifier = Modifier.weight(1f),
                icon = "📏",
                title = "Height",
                value = if (session.heightCm > 0.0) {
                    String.format(Locale.US, "%.0f", session.heightCm)
                } else {
                    "—"
                },
                unit = "cm",
                accent = HealthCyan
            )

            HealthMetricCard(
                modifier = Modifier.weight(1f),
                icon = "💚",
                title = "BMI",
                value = if (bmi > 0.0) {
                    String.format(Locale.US, "%.1f", bmi)
                } else {
                    "—"
                },
                unit = "index",
                accent = HealthGreen
            )
        }
    }
}

@Composable
private fun HealthMetricCard(
    modifier: Modifier,
    icon: String,
    title: String,
    value: String,
    unit: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = HealthCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE6ECF3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.11f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }

            Text(
                text = title,
                color = HealthMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = value,
                color = HealthInk,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = unit,
                color = Color(0xFF94A3B8),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun BmiOverviewCard(
    bmi: Double,
    status: String,
    progress: Float,
    healthyRange: Pair<Double, Double>?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HealthCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE6ECF3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BMI balance",
                        color = HealthInk,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = status,
                        color = bmiStatusColor(status),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                Text(
                    text = if (bmi > 0.0) {
                        String.format(Locale.US, "%.1f", bmi)
                    } else {
                        "—"
                    },
                    color = HealthInk,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = bmiStatusColor(status),
                trackColor = Color(0xFFE8EEF5)
            )

            Text(
                text = healthyRange?.let {
                    "Estimated healthy weight range: " +
                            String.format(Locale.US, "%.1f–%.1f kg", it.first, it.second)
                } ?: "Add a valid height to calculate a healthy weight range.",
                color = HealthMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun HealthGoalSnapshot(
    goal: GoalCenterData?,
    loading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HealthCardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF071F2A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily target snapshot",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = goal?.goalType ?: "No saved goal yet",
                        color = Color.White.copy(alpha = 0.62f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                if (loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("🎯", fontSize = 24.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DarkGoalMetric(
                    modifier = Modifier.weight(1f),
                    label = "Steps",
                    value = goal?.stepsTarget ?: "Not set"
                )
                DarkGoalMetric(
                    modifier = Modifier.weight(1f),
                    label = "Water",
                    value = goal?.waterTargetMl?.let { "$it ml" } ?: "Not set"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DarkGoalMetric(
                    modifier = Modifier.weight(1f),
                    label = "Workout",
                    value = goal?.workoutMinutesTarget?.let { "$it min" } ?: "Not set"
                )
                DarkGoalMetric(
                    modifier = Modifier.weight(1f),
                    label = "Calories",
                    value = goal?.workoutCaloriesTarget?.let { "$it kcal" } ?: "Not set"
                )
            }
        }
    }
}

@Composable
private fun DarkGoalMetric(
    modifier: Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(17.dp))
            .background(Color.White.copy(alpha = 0.09f))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.58f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun HealthDailyGuide(
    goal: GoalCenterData?,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HealthCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE6ECF3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Daily wellness guide",
                color = HealthInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            HealthGuideRow("💧", "Water", goal?.waterTargetMl?.let { "$it ml" } ?: "About 2000 ml")
            HealthGuideRow("👟", "Steps", goal?.stepsTarget ?: "7500")
            HealthGuideRow("🏃", "Movement", goal?.workoutMinutesTarget?.let { "$it minutes" } ?: "30 minutes")
            HealthGuideRow("😴", "Sleep", "7–9 hours")
            HealthGuideRow("🥗", "Focus", healthFoodFocus(status))
        }
    }
}

@Composable
private fun HealthGuideRow(
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF6F9FC))
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Text(
            text = label,
            color = HealthMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        )
        Text(
            text = value,
            color = HealthInk,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun HealthAdviceCard(
    bmi: Double,
    bmiStatus: String,
    weightKg: Double,
    healthyRange: Pair<Double, Double>?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HealthCardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 22.sp)
                Text(
                    text = "Health insight",
                    color = Color(0xFF92400E),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 9.dp)
                )
            }

            Text(
                text = buildHealthAdvice(
                    bmi = bmi,
                    bmiStatus = bmiStatus,
                    weightKg = weightKg,
                    healthyRange = healthyRange
                ),
                color = Color(0xFF78350F),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            HorizontalDivider(color = Color(0xFFFDE68A))

            Text(
                text = "This page provides general wellness information and is not a medical diagnosis.",
                color = Color(0xFF92400E),
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun HealthMessageCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HealthCardShape,
        color = Color(0xFFFFF1F2),
        border = BorderStroke(1.dp, Color(0xFFFECDD3))
    ) {
        Text(
            text = message,
            color = Color(0xFFBE123C),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(15.dp)
        )
    }
}

@Composable
private fun HealthFooterMenu(
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onSummary: () -> Unit,
    onProgress: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 5.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HealthFooterItem(Modifier.weight(1f), "⌂", "Home", false, onHome)
            HealthFooterItem(Modifier.weight(1f), "🏃", "Activity", false, onActivity)
            HealthFooterItem(Modifier.weight(1f), "📋", "Summary", false, onSummary)
            HealthFooterItem(Modifier.weight(1f), "📈", "Progress", false, onProgress)
            HealthFooterItem(Modifier.weight(1f), "❤️", "Health", true, {})
            HealthFooterItem(Modifier.weight(1f), "👤", "Profile", false, onProfile)
        }
    }
}

@Composable
private fun HealthFooterItem(
    modifier: Modifier,
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) Color(0xFFE6F7F3) else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = label,
            color = if (selected) HealthPrimary else Color(0xFF94A3B8),
            fontSize = 8.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
            maxLines = 1
        )
    }
}

private fun calculateHealthBmi(
    weightKg: Double,
    heightCm: Double
): Double {
    if (weightKg <= 0.0 || heightCm <= 0.0) return 0.0

    val heightM = heightCm / 100.0
    return weightKg / heightM.pow(2.0)
}

private fun healthBmiStatus(bmi: Double): String {
    return when {
        bmi <= 0.0 -> "Profile incomplete"
        bmi < 18.5 -> "Underweight"
        bmi < 25.0 -> "Normal"
        bmi < 30.0 -> "Overweight"
        else -> "High BMI"
    }
}

private fun healthScore(bmi: Double): Int {
    if (bmi <= 0.0) return 0

    val distanceFromBalanced = abs(bmi - 22.0)
    return (100.0 - distanceFromBalanced * 7.5)
        .roundToInt()
        .coerceIn(35, 100)
}

private fun healthyWeightRange(heightCm: Double): Pair<Double, Double>? {
    if (heightCm <= 0.0) return null

    val heightM = heightCm / 100.0
    return 18.5 * heightM.pow(2.0) to 24.9 * heightM.pow(2.0)
}

private fun healthStatusMessage(status: String): String {
    return when (status) {
        "Underweight" -> "Focus on steady strength and balanced meals"
        "Normal" -> "Your BMI is in the balanced range"
        "Overweight" -> "Consistency and daily movement can help"
        "High BMI" -> "Begin gently and build sustainable habits"
        else -> "Complete your profile measurements"
    }
}

private fun bmiStatusColor(status: String): Color {
    return when (status) {
        "Normal" -> HealthGreen
        "Underweight" -> HealthCyan
        "Overweight" -> Color(0xFFF59E0B)
        "High BMI" -> Color(0xFFEF4444)
        else -> Color(0xFF94A3B8)
    }
}

private fun healthFoodFocus(status: String): String {
    return when (status) {
        "Underweight" -> "Protein and balanced meals"
        "Normal" -> "Balanced portions"
        "Overweight", "High BMI" -> "Vegetables and portion control"
        else -> "Balanced nutrition"
    }
}

private fun buildHealthAdvice(
    bmi: Double,
    bmiStatus: String,
    weightKg: Double,
    healthyRange: Pair<Double, Double>?
): String {
    if (bmi <= 0.0 || healthyRange == null || weightKg <= 0.0) {
        return "Add your current weight and height in Profile Settings to receive a more useful health overview."
    }

    return when (bmiStatus) {
        "Underweight" -> {
            val difference = (healthyRange.first - weightKg).coerceAtLeast(0.0)
            "Your current weight is about ${String.format(Locale.US, "%.1f", difference)} kg below the estimated healthy range. Build strength gradually and discuss major weight changes with a qualified professional."
        }
        "Normal" ->
            "Your weight is inside the estimated healthy range. Maintain regular activity, sufficient sleep, hydration and balanced meals."
        "Overweight", "High BMI" -> {
            val difference = (weightKg - healthyRange.second).coerceAtLeast(0.0)
            "Your current weight is about ${String.format(Locale.US, "%.1f", difference)} kg above the estimated healthy range. Aim for gradual, sustainable habits rather than rapid changes."
        }
        else ->
            "Keep your profile measurements current to make this overview more useful."
    }
}
