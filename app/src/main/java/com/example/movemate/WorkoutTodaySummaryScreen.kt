package com.example.movemate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

private data class TodayActivityStat(
    val activityType: String,
    val workoutCount: Int,
    val durationSeconds: Int,
    val distanceKm: Double,
    val calories: Double
)

@Composable
fun WorkoutTodaySummaryScreen(
    session: UserSession,
    onBack: () -> Unit,
    onStartAnotherWorkout: () -> Unit,
    onTopHome: () -> Unit =
        onBack,
    onHome: () -> Unit =
        onBack,
    onProgress: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    BackHandler(
        onBack =
            onBack
    )

    var loading by remember(
        session.userId
    ) {
        mutableStateOf(
            true
        )
    }

    var errorMessage by remember(
        session.userId
    ) {
        mutableStateOf(
            ""
        )
    }

    var summary by remember(
        session.userId
    ) {
        mutableStateOf<JSONObject?>(
            null
        )
    }

    var refreshToken by remember {
        mutableIntStateOf(
            0
        )
    }

    LaunchedEffect(
        session.userId,
        refreshToken
    ) {
        loading = true
        errorMessage = ""

        val response =
            WorkoutApiService
                .getTodaySummary(
                    session.userId
                )

        if (
            response.optBoolean(
                "success",
                false
            )
        ) {
            summary =
                response
        } else {
            errorMessage =
                response.optString(
                    "message",
                    "Could not load today's summary."
                )
        }

        loading = false
    }

    val response =
        summary

    val totalWorkouts =
        response
            ?.optInt(
                "total_workouts",
                0
            )
            ?: 0

    val totalDurationSeconds =
        response
            ?.optInt(
                "total_duration_seconds",
                0
            )
            ?: 0

    val totalDistanceKm =
        response
            ?.optDouble(
                "total_distance_km",
                0.0
            )
            ?: 0.0

    val serverTotalCalories =
        response
            ?.optDouble(
                "total_calories",
                0.0
            )
            ?: 0.0

    val activities =
        remember(response) {
            parseTodayActivities(
                response
                    ?.optJSONArray(
                        "activity_stats"
                    )
            )
        }

    val lastWorkout =
        response
            ?.optJSONObject(
                "last_workout"
            )

    val awardCounts =
        response
            ?.optJSONObject(
                "award_counts"
            )

    /*
     * Recover saved calories from grounded workout data when an older server
     * response incorrectly returns top-level total_calories as zero.
     */
    val activityCaloriesTotal =
        activities.sumOf {
            it.calories
        }

    val lastWorkoutCalories =
        lastWorkout
            ?.optDouble(
                "calories",
                0.0
            )
            ?: 0.0

    val displayTotalCalories =
        maxOf(
            serverTotalCalories,
            activityCaloriesTotal,
            lastWorkoutCalories
        )

    Scaffold(
        containerColor =
            Color.Transparent,
        topBar = {
            TodaySummaryTopBar(
                loading =
                    loading,
                onTopHome =
                    onTopHome,
                onRefresh = {
                    if (
                        !loading
                    ) {
                        refreshToken +=
                            1
                    }
                }
            )
        },
        bottomBar = {
            TodaySummaryFooter(
                onHome =
                    onHome,
                onActivity =
                    onStartAnotherWorkout,
                onProgress =
                    onProgress,
                onProfile =
                    onProfile
            )
        }
    ) {
            scaffoldPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFEFF6FF),
                            Color.White,
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(
                    scaffoldPadding
                )
        ) {
            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start =
                            18.dp,
                        top =
                            14.dp,
                        end =
                            18.dp,
                        bottom =
                            28.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        14.dp
                    )
            ) {
                if (loading) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical =
                                        48.dp
                                ),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    12.dp
                                )
                        ) {
                            CircularProgressIndicator()

                            Text(
                                "Loading saved workouts...",
                                color =
                                    Color(0xFF64748B)
                            )
                        }
                    }
                } else if (
                    errorMessage.isNotBlank()
                ) {
                    item {
                        Card(
                            modifier =
                                Modifier.fillMaxWidth(),
                            shape =
                                RoundedCornerShape(
                                    22.dp
                                ),
                            colors =
                                CardDefaults
                                    .cardColors(
                                        containerColor =
                                            Color(0xFFFFEBEE)
                                    )
                        ) {
                            Column(
                                modifier =
                                    Modifier.padding(
                                        18.dp
                                    ),
                                verticalArrangement =
                                    Arrangement.spacedBy(
                                        10.dp
                                    )
                            ) {
                                Text(
                                    "Could not load summary",
                                    color =
                                        Color(0xFFB91C1C),
                                    fontWeight =
                                        FontWeight.ExtraBold
                                )

                                Text(
                                    errorMessage,
                                    color =
                                        Color(0xFF7F1D1D)
                                )

                                OutlinedButton(
                                    onClick = {
                                        refreshToken +=
                                            1
                                    }
                                ) {
                                    Text(
                                        "Retry"
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Card(
                            modifier =
                                Modifier.fillMaxWidth(),
                            shape =
                                RoundedCornerShape(
                                    28.dp
                                ),
                            colors =
                                CardDefaults
                                    .cardColors(
                                        containerColor =
                                            Color(0xFF0F172A)
                                    ),
                            elevation =
                                CardDefaults
                                    .cardElevation(
                                        7.dp
                                    )
                        ) {
                            Column(
                                modifier =
                                    Modifier.padding(
                                        20.dp
                                    ),
                                verticalArrangement =
                                    Arrangement.spacedBy(
                                        8.dp
                                    )
                            ) {
                                Text(
                                    "Today",
                                    color =
                                        Color(0xFF93C5FD),
                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Text(
                                    "$totalWorkouts completed workouts",
                                    color =
                                        Color.White,
                                    fontSize =
                                        25.sp,
                                    fontWeight =
                                        FontWeight.ExtraBold
                                )

                                Text(
                                    "All values below are loaded from MySQL.",
                                    color =
                                        Color(0xFFCBD5E1),
                                    fontSize =
                                        12.sp
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    10.dp
                                )
                        ) {
                            TodaySummaryMetric(
                                title =
                                    "Calories",
                                value =
                                    String.format(
                                        Locale.US,
                                        "%.1f kcal",
                                        displayTotalCalories
                                    ),
                                emoji =
                                    "🔥",
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )

                            TodaySummaryMetric(
                                title =
                                    "Distance",
                                value =
                                    formatTodayDistance(
                                        totalDistanceKm
                                    ),
                                emoji =
                                    "📍",
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )
                        }
                    }

                    item {
                        Card(
                            modifier =
                                Modifier.fillMaxWidth(),
                            shape =
                                RoundedCornerShape(
                                    22.dp
                                ),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color(0xFFFFF7ED)
                                )
                        ) {
                            Column(
                                modifier =
                                    Modifier.padding(
                                        17.dp
                                    ),
                                verticalArrangement =
                                    Arrangement.spacedBy(
                                        8.dp
                                    )
                            ) {
                                Text(
                                    "Calories completed today",
                                    color =
                                        Color(0xFF9A3412),
                                    fontWeight =
                                        FontWeight.ExtraBold
                                )

                                /*
                                 * Version-independent progress bar.
                                 *
                                 * Avoids LinearProgressIndicator API differences
                                 * between Material3 versions.
                                 */
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(
                                            10.dp
                                        )
                                        .background(
                                            color =
                                                Color(0xFFFED7AA),
                                            shape =
                                                RoundedCornerShape(
                                                    50.dp
                                                )
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(
                                                fraction =
                                                    if (
                                                        displayTotalCalories >
                                                        0.0
                                                    ) {
                                                        1f
                                                    } else {
                                                        0f
                                                    }
                                            )
                                            .height(
                                                10.dp
                                            )
                                            .background(
                                                color =
                                                    Color(0xFFEA580C),
                                                shape =
                                                    RoundedCornerShape(
                                                        50.dp
                                                    )
                                            )
                                    )
                                }

                                Text(
                                    text =
                                        if (
                                            displayTotalCalories >
                                            0.0
                                        ) {
                                            String.format(
                                                Locale.US,
                                                "%.1f kcal saved and counted",
                                                displayTotalCalories
                                            )
                                        } else {
                                            "No completed workout calories saved today."
                                        },
                                    color =
                                        Color(0xFF7C2D12)
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    10.dp
                                )
                        ) {
                            TodaySummaryMetric(
                                title =
                                    "Active time",
                                value =
                                    formatTodayDuration(
                                        totalDurationSeconds
                                    ),
                                emoji =
                                    "⏱️",
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )

                            TodaySummaryMetric(
                                title =
                                    "Workouts",
                                value =
                                    totalWorkouts.toString(),
                                emoji =
                                    "✅",
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )
                        }
                    }

                    if (lastWorkout != null) {
                        item {
                            LastWorkoutCard(
                                workout =
                                    lastWorkout
                            )
                        }
                    }

                    item {
                        Text(
                            "Activity breakdown",
                            color =
                                Color(0xFF0F172A),
                            fontSize =
                                19.sp,
                            fontWeight =
                                FontWeight.ExtraBold
                        )
                    }

                    if (activities.isEmpty()) {
                        item {
                            Card(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape =
                                    RoundedCornerShape(
                                        20.dp
                                    ),
                                colors =
                                    CardDefaults
                                        .cardColors(
                                            containerColor =
                                                Color.White
                                        )
                            ) {
                                Text(
                                    "No completed workout has been saved today.",
                                    color =
                                        Color(0xFF64748B),
                                    textAlign =
                                        TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            24.dp
                                        )
                                )
                            }
                        }
                    } else {
                        items(
                            items =
                                activities,
                            key = {
                                it.activityType
                            }
                        ) { activity ->
                            ActivitySummaryCard(
                                activity =
                                    activity
                            )
                        }
                    }

                    if (awardCounts != null) {
                        item {
                            AwardCountCard(
                                awards =
                                    awardCounts
                            )
                        }
                    }

                    item {
                        Button(
                            onClick =
                                onStartAnotherWorkout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    56.dp
                                ),
                            shape =
                                RoundedCornerShape(
                                    18.dp
                                ),
                            colors =
                                ButtonDefaults
                                    .buttonColors(
                                        containerColor =
                                            Color(0xFF2563EB)
                                    )
                        ) {
                            Text(
                                "Start Another Workout",
                                fontWeight =
                                    FontWeight.ExtraBold
                            )
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                refreshToken +=
                                    1
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    52.dp
                                ),
                            shape =
                                RoundedCornerShape(
                                    18.dp
                                )
                        ) {
                            Text(
                                "Refresh Summary",
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodaySummaryTopBar(
    loading: Boolean,
    onTopHome: () -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        color =
            Color.White.copy(
                alpha =
                    0.98f
            ),
        shadowElevation =
            5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal =
                        16.dp,
                    vertical =
                        10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            TodaySummaryNavigationIcon(
                icon =
                    "⌂",
                enabled =
                    true,
                onClick =
                    onTopHome
            )

            Column(
                modifier = Modifier
                    .weight(
                        1f
                    )
                    .padding(
                        start =
                            13.dp
                    )
            ) {
                Text(
                    text =
                        "Today's Summary",
                    color =
                        Color(0xFF0F172A),
                    fontSize =
                        21.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        "Saved workouts and daily totals",
                    color =
                        Color(0xFF64748B),
                    fontSize =
                        10.sp
                )
            }

            TodaySummaryNavigationIcon(
                icon =
                    "↻",
                enabled =
                    !loading,
                loading =
                    loading,
                onClick =
                    onRefresh
            )
        }
    }
}

@Composable
private fun TodaySummaryNavigationIcon(
    icon: String,
    enabled: Boolean,
    loading: Boolean =
        false,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource.collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (
                pressed &&
                enabled
            ) {
                0.90f
            } else {
                1f
            },
        animationSpec =
            spring()
    )

    Box(
        modifier = Modifier
            .size(
                46.dp
            )
            .scale(
                scale
            )
            .clip(
                CircleShape
            )
            .background(
                if (
                    enabled
                ) {
                    Color(0xFFEAF2FF)
                } else {
                    Color(0xFFF1F5F9)
                }
            )
            .clickable(
                enabled =
                    enabled,
                interactionSource =
                    interactionSource,
                indication =
                    null,
                onClick =
                    onClick
            ),
        contentAlignment =
            Alignment.Center
    ) {
        if (
            loading
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(
                        21.dp
                    ),
                color =
                    Color(0xFF2563EB),
                strokeWidth =
                    2.4.dp
            )
        } else {
            Text(
                text =
                    icon,
                color =
                    if (
                        enabled
                    ) {
                        Color(0xFF1E3A8A)
                    } else {
                        Color(0xFF94A3B8)
                    },
                fontSize =
                    21.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun TodaySummaryFooter(
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onProgress: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        color =
            Color.White,
        shape =
            RoundedCornerShape(
                topStart =
                    25.dp,
                topEnd =
                    25.dp
            ),
        shadowElevation =
            14.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal =
                        8.dp,
                    vertical =
                        8.dp
                ),
            horizontalArrangement =
                Arrangement.SpaceAround,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            TodaySummaryFooterItem(
                icon =
                    "⌂",
                label =
                    "Home",
                onClick =
                    onHome
            )

            TodaySummaryFooterItem(
                icon =
                    "●",
                label =
                    "Activity",
                onClick =
                    onActivity
            )

            TodaySummaryFooterItem(
                icon =
                    "▣",
                label =
                    "Summary",
                selected =
                    true,
                onClick = {}
            )

            TodaySummaryFooterItem(
                icon =
                    "▥",
                label =
                    "Progress",
                onClick =
                    onProgress
            )

            TodaySummaryFooterItem(
                icon =
                    "◉",
                label =
                    "Profile",
                onClick =
                    onProfile
            )
        }
    }
}

@Composable
private fun TodaySummaryFooterItem(
    icon: String,
    label: String,
    selected: Boolean =
        false,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource.collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (
                pressed
            ) {
                0.91f
            } else {
                1f
            },
        animationSpec =
            spring()
    )

    val itemColor =
        if (
            selected
        ) {
            Color(0xFF2563EB)
        } else {
            Color(0xFF94A3B8)
        }

    Column(
        modifier = Modifier
            .width(
                66.dp
            )
            .scale(
                scale
            )
            .clip(
                RoundedCornerShape(
                    16.dp
                )
            )
            .background(
                if (
                    selected
                ) {
                    Color(0xFFEFF6FF)
                } else {
                    Color.Transparent
                }
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication =
                    null,
                onClick =
                    onClick
            )
            .padding(
                vertical =
                    7.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                icon,
            color =
                itemColor,
            fontSize =
                18.sp,
            fontWeight =
                FontWeight.ExtraBold
        )

        Text(
            text =
                label,
            color =
                itemColor,
            fontSize =
                9.sp,
            fontWeight =
                FontWeight.ExtraBold,
            modifier =
                Modifier.padding(
                    top =
                        2.dp
                )
        )
    }
}

@Composable
private fun TodaySummaryMetric(
    title: String,
    value: String,
    emoji: String,
    modifier: Modifier
) {
    Card(
        modifier =
            modifier,
        shape =
            RoundedCornerShape(
                21.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                4.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    5.dp
                )
        ) {
            Text(
                emoji,
                fontSize =
                    24.sp
            )

            Text(
                title,
                color =
                    Color(0xFF64748B),
                fontSize =
                    12.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                value,
                color =
                    Color(0xFF0F172A),
                fontSize =
                    18.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun LastWorkoutCard(
    workout: JSONObject
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                24.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFFEFF6FF)
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    18.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    7.dp
                )
        ) {
            Text(
                "Most recent saved workout",
                color =
                    Color(0xFF1E3A8A),
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                workout.optString(
                    "workout_name",
                    "Workout"
                ),
                color =
                    Color(0xFF0F172A),
                fontSize =
                    20.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                "${workout.optString("level_name", "Normal")} • " +
                        workout.optString(
                            "award_tier",
                            "STARTER"
                        ),
                color =
                    Color(0xFF2563EB),
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                "${formatTodayDuration(workout.optInt("duration_seconds", 0))} • " +
                        "${formatTodayDistance(workout.optDouble("distance_km", 0.0))} • " +
                        String.format(
                            Locale.US,
                            "%.1f kcal",
                            workout.optDouble(
                                "calories",
                                0.0
                            )
                        ),
                color =
                    Color(0xFF475569)
            )
        }
    }
}

@Composable
private fun ActivitySummaryCard(
    activity: TodayActivityStat
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                3.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    17.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    7.dp
                )
        ) {
            Text(
                activity.activityType,
                color =
                    Color(0xFF0F172A),
                fontSize =
                    18.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                "${activity.workoutCount} sessions • " +
                        formatTodayDuration(
                            activity.durationSeconds
                        ),
                color =
                    Color(0xFF64748B),
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                "${formatTodayDistance(activity.distanceKm)} • " +
                        String.format(
                            Locale.US,
                            "%.1f kcal",
                            activity.calories
                        ),
                color =
                    Color(0xFF2563EB)
            )
        }
    }
}

@Composable
private fun AwardCountCard(
    awards: JSONObject
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFFFFFBEB)
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    18.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            Text(
                "Today's awards",
                color =
                    Color(0xFF78350F),
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                "🌱 ${awards.optInt("STARTER", 0)}  " +
                        "🥈 ${awards.optInt("SILVER", 0)}  " +
                        "🥇 ${awards.optInt("GOLD", 0)}  " +
                        "💎 ${awards.optInt("DIAMOND", 0)}",
                color =
                    Color(0xFF92400E),
                fontSize =
                    17.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

private fun parseTodayActivities(
    array: JSONArray?
): List<TodayActivityStat> {
    if (array == null) {
        return emptyList()
    }

    val result =
        mutableListOf<TodayActivityStat>()

    for (
    index in
    0 until
            array.length()
    ) {
        val item =
            array.optJSONObject(
                index
            )
                ?: continue

        result.add(
            TodayActivityStat(
                activityType =
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

    return result
}

private fun formatTodayDuration(
    seconds: Int
): String {
    val safeSeconds =
        seconds.coerceAtLeast(
            0
        )

    val hours =
        safeSeconds /
                3600

    val minutes =
        (
                safeSeconds %
                        3600
                ) /
                60

    val remainingSeconds =
        safeSeconds %
                60

    return if (hours > 0) {
        String.format(
            Locale.US,
            "%d:%02d:%02d",
            hours,
            minutes,
            remainingSeconds
        )
    } else {
        String.format(
            Locale.US,
            "%02d:%02d",
            minutes,
            remainingSeconds
        )
    }
}

private fun formatTodayDistance(
    distanceKm: Double
): String {
    val safeDistance =
        distanceKm.coerceAtLeast(
            0.0
        )

    return if (
        safeDistance <
        1.0
    ) {
        String.format(
            Locale.US,
            "%.1f m",
            safeDistance *
                    1000.0
        )
    } else {
        String.format(
            Locale.US,
            "%.2f km",
            safeDistance
        )
    }
}