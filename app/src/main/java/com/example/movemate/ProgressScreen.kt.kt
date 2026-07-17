package com.example.movemate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProgressScreen(
    userId: Int,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onSummary: () -> Unit,
    onGoal: () -> Unit,
    onProfile: () -> Unit
) {
    var selectedDays by remember { mutableIntStateOf(7) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var progressData by remember { mutableStateOf<ProgressPageData?>(null) }

    suspend fun loadProgress() {
        loading = true
        errorMessage = null

        if (userId <= 0) {
            loading = false
            errorMessage = "Invalid user ID."
            return
        }

        try {
            val response: JSONObject =
                withContext(Dispatchers.IO) {
                    ProgressApiService.getProgress(
                        userId = userId,
                        days = selectedDays
                    )
                }

            if (response.optBoolean("success", false)) {
                progressData = response.toProgressPageData()
                errorMessage = null
            } else {
                progressData = null
                errorMessage = response.optString(
                    "message",
                    "Could not load progress."
                )
            }
        } catch (e: Exception) {
            progressData = null
            errorMessage = e.message ?: "Unknown error."
        }

        loading = false
    }

    LaunchedEffect(userId, selectedDays) {
        loadProgress()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFF1F5F9),
                        Color(0xFFE0F2FE)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ProgressTopBar(
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 18.dp
                        )
                    ) {
                        Text(
                            text = "Progress",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Track streaks, calories, distance and daily workout performance smoothly.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DaySelectorRow(
                            selectedDays = selectedDays,
                            onSelectDays = { selectedDays = it }
                        )
                    }
                }

                item {
                    if (loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }

                item {
                    AnimatedVisibility(visible = errorMessage != null && !loading) {
                        ErrorCard(
                            message = errorMessage ?: "Unknown error",
                            onRetry = {
                                errorMessage = null
                                progressData = null
                                loading = true
                            }
                        )
                    }
                }

                progressData?.let { data ->
                    item {
                        HeroProgressCard(
                            data = data,
                            onGoal = onGoal
                        )
                    }

                    item {
                        MetricGrid(
                            data = data
                        )
                    }

                    item {
                        InsightCard(
                            data = data
                        )
                    }

                    item {
                        GoalProgressCard(
                            data = data,
                            onGoal = onGoal
                        )
                    }

                    item {
                        SectionTitle("Daily Activity")
                    }

                    if (data.daily.isEmpty()) {
                        item {
                            EmptyDailyCard()
                        }
                    } else {
                        items(data.daily.asReversed()) { day ->
                            DailyProgressCard(
                                item = day
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            ProgressFooterBar(
                onHome = onHome,
                onActivity = onActivity,
                onSummary = onSummary,
                onProfile = onProfile
            )
        }
    }
}

@Composable
private fun ProgressTopBar(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "←",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "MoveMate",
                fontSize = 12.sp,
                color = Color(0xFF2563EB),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Progress Overview",
                fontSize = 16.sp,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DaySelectorRow(
    selectedDays: Int,
    onSelectDays: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DayChip(
            text = "7 Days",
            selected = selectedDays == 7,
            onClick = { onSelectDays(7) }
        )
        DayChip(
            text = "30 Days",
            selected = selectedDays == 30,
            onClick = { onSelectDays(30) }
        )
    }
}

@Composable
private fun DayChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF2563EB),
                            Color(0xFF7C3AED)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color.White, Color.White)
                    )
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF334155),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun HeroProgressCard(
    data: ProgressPageData,
    onGoal: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1D4ED8),
                            Color(0xFF06B6D4)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = "YOUR FITNESS REPORT",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${data.fromDate}  →  ${data.toDate}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeroMiniMetric("Calories", String.format(Locale.US, "%.1f", data.summary.totalCalories))
                HeroMiniMetric("Active Days", data.summary.activeDays.toString())
                HeroMiniMetric("Streak", "${data.summary.currentStreak} d")
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable { onGoal() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Open Goal Center",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun HeroMiniMetric(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.76f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MetricGrid(
    data: ProgressPageData
) {
    Column(
        modifier = Modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Calories",
                value = String.format(Locale.US, "%.1f kcal", data.summary.totalCalories),
                accent = Color(0xFFF97316)
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Distance",
                value = formatDistance(data.summary.totalDistanceKm),
                accent = Color(0xFF06B6D4)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Workouts",
                value = data.summary.totalWorkouts.toString(),
                accent = Color(0xFF7C3AED)
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Duration",
                value = formatDuration(data.summary.totalDurationSeconds),
                accent = Color(0xFF2563EB)
            )
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color(0xFF0F172A),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InsightCard(
    data: ProgressPageData
) {
    val avgCalories =
        if (data.summary.activeDays > 0) {
            data.summary.totalCalories / data.summary.activeDays
        } else 0.0

    val bestDay =
        data.daily.maxByOrNull { it.calories }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8FAFC),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Progress Insight",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (data.summary.totalWorkouts == 0) {
                    "No completed workout yet. Start one activity to build your progress history."
                } else {
                    "You completed ${data.summary.totalWorkouts} workouts with ${data.summary.activeDays} active day(s). Keep going to improve your streak and average calories."
                },
                color = Color(0xFF334155),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Average: ${String.format(Locale.US, "%.1f", avgCalories)} kcal per active day",
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Best Day: ${bestDay?.date ?: "No data"} • ${String.format(Locale.US, "%.1f", bestDay?.calories ?: 0.0)} kcal",
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun GoalProgressCard(
    data: ProgressPageData,
    onGoal: () -> Unit
) {
    val calorieTarget = data.goal.calorieTarget
    val minutesTarget = data.goal.minutesTarget
    val totalMinutes = data.summary.totalDurationSeconds / 60f

    val caloriesProgress =
        if (calorieTarget > 0) {
            (data.summary.totalCalories / calorieTarget).toFloat().coerceIn(0f, 1f)
        } else 0f

    val minutesProgress =
        if (minutesTarget > 0) {
            (totalMinutes / minutesTarget).coerceIn(0f, 1f)
        } else 0f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFF0F172A),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Goal Progress",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (calorieTarget > 0) {
                    "${String.format(Locale.US, "%.1f", data.summary.totalCalories)} / $calorieTarget kcal"
                } else {
                    "No calorie goal set"
                },
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { caloriesProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFFF97316),
                trackColor = Color.White.copy(alpha = 0.18f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (minutesTarget > 0) {
                    "${totalMinutes.toInt()} / $minutesTarget min"
                } else {
                    "No workout-minute goal set"
                },
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { minutesProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFF06B6D4),
                trackColor = Color.White.copy(alpha = 0.18f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF2563EB),
                                Color(0xFF7C3AED)
                            )
                        )
                    )
                    .clickable { onGoal() }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (data.goal.exists) "Edit Goal" else "Set Goal",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 18.dp),
        color = Color(0xFF0F172A),
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp
    )
}

@Composable
private fun EmptyDailyCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFFFF7ED)
    ) {
        Text(
            text = "No progress records yet. Complete a workout first.",
            modifier = Modifier.padding(18.dp),
            color = Color(0xFFC2410C),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun DailyProgressCard(
    item: ProgressDayData
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDBEAFE)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (item.workoutCount > 0) "✓" else "–",
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatDate(item.date),
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${item.workoutCount} workout(s) • ${formatDuration(item.durationSeconds)} • ${formatDistance(item.distanceKm)}",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp
                )
            }

            Text(
                text = String.format(Locale.US, "%.1f kcal", item.calories),
                color = Color(0xFF7C3AED),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFFF1F2)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Failed to load progress",
                color = Color(0xFFBE123C),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = Color(0xFF9F1239),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { onRetry() }
                    .padding(horizontal = 18.dp, vertical = 11.dp)
            ) {
                Text(
                    text = "Retry",
                    color = Color(0xFFBE123C),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProgressFooterBar(
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onSummary: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        color = Color.White,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FooterItem("⌂", "Home", onHome)
            FooterItem("●", "Activity", onActivity)
            FooterItem("▣", "Summary", onSummary)
            FooterItem("▥", "Progress", null, selected = true)
            FooterItem("◉", "Profile", onProfile)
        }
    }
}

@Composable
private fun FooterItem(
    icon: String,
    label: String,
    onClick: (() -> Unit)?,
    selected: Boolean = false
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) Color(0xFFEFF6FF) else Color.Transparent
            )
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            color = if (selected) Color(0xFF2563EB) else Color(0xFF94A3B8),
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
        Text(
            text = label,
            color = if (selected) Color(0xFF2563EB) else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val totalMinutes = seconds.coerceAtLeast(0) / 60
    return if (totalMinutes >= 60) {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        "${hours}h ${minutes}m"
    } else {
        "${totalMinutes} min"
    }
}

private fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) {
        String.format(Locale.US, "%.0f m", distanceKm * 1000.0)
    } else {
        String.format(Locale.US, "%.2f km", distanceKm)
    }
}

private fun formatDate(value: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val output = SimpleDateFormat("EEE, dd MMM", Locale.US)
        output.format(input.parse(value)!!)
    } catch (_: Exception) {
        value
    }
}