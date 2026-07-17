package com.example.movemate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
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
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WorkoutHistoryScreen(
    session: UserSession,
    onBack: () -> Unit,
    onHome: () -> Unit =
        onBack,
    onActivity: () -> Unit = {},
    onSummary: () -> Unit = {},
    onProgress: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    BackHandler(
        onBack =
            onBack
    )

    var refreshToken by remember {
        mutableIntStateOf(
            0
        )
    }

    var loading by remember {
        mutableStateOf(
            true
        )
    }

    var errorMessage by remember {
        mutableStateOf<String?>(
            null
        )
    }

    var page by remember {
        mutableStateOf<WorkoutHistoryPage?>(
            null
        )
    }

    var selectedFilter by remember {
        mutableStateOf(
            "All"
        )
    }

    LaunchedEffect(
        session.userId,
        refreshToken
    ) {
        loading =
            true

        errorMessage =
            null

        try {
            val response =
                withContext(
                    Dispatchers.IO
                ) {
                    WorkoutHistoryApiService
                        .loadHistory(
                            userId =
                                session.userId,
                            limit =
                                1_000
                        )
                }

            if (
                response.optBoolean(
                    "success",
                    false
                )
            ) {
                page =
                    response
                        .toWorkoutHistoryPage()
            } else {
                page =
                    null

                errorMessage =
                    response.optString(
                        "message",
                        "Could not load workout history."
                    )
            }
        } catch (
            exception: Exception
        ) {
            page =
                null

            errorMessage =
                exception.message
                    ?: "Unknown history error."
        } finally {
            loading =
                false
        }
    }

    val history =
        page
            ?.history
            .orEmpty()

    val filters =
        remember(
            history
        ) {
            buildList {
                add(
                    "All"
                )

                history
                    .map {
                        it.activityType
                            .ifBlank {
                                "Workout"
                            }
                    }
                    .distinct()
                    .sorted()
                    .forEach {
                        add(
                            it
                        )
                    }
            }
        }

    val filteredHistory =
        remember(
            history,
            selectedFilter
        ) {
            if (
                selectedFilter ==
                "All"
            ) {
                history
            } else {
                history.filter {
                    it.activityType.equals(
                        selectedFilter,
                        ignoreCase =
                            true
                    )
                }
            }
        }

    Scaffold(
        containerColor =
            Color(0xFFF4F7FC),
        topBar = {
            HistoryTopBar(
                loading =
                    loading,
                onBack =
                    onBack,
                onHome =
                    onHome,
                onRefresh = {
                    refreshToken +=
                        1
                }
            )
        },
        bottomBar = {
            HistoryFooter(
                onHome =
                    onHome,
                onActivity =
                    onActivity,
                onSummary =
                    onSummary,
                onProgress =
                    onProgress,
                onProfile =
                    onProfile
            )
        }
    ) {
            innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFEFF6FF),
                            Color(0xFFF8FAFC),
                            Color.White
                        )
                    )
                ),
            contentPadding =
                PaddingValues(
                    start =
                        18.dp,
                    top =
                        innerPadding
                            .calculateTopPadding() +
                                14.dp,
                    end =
                        18.dp,
                    bottom =
                        innerPadding
                            .calculateBottomPadding() +
                                24.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    14.dp
                )
        ) {
            item {
                HistoryHero(
                    summary =
                        page
                            ?.summary
                            ?: WorkoutHistorySummary(
                                totalWorkouts =
                                    0,
                                totalDurationSeconds =
                                    0,
                                totalDistanceKm =
                                    0.0,
                                totalCalories =
                                    0.0
                            ),
                    returnedRows =
                        page
                            ?.returnedRows
                            ?: 0,
                    totalRows =
                        page
                            ?.totalRows
                            ?: 0
                )
            }

            when {
                loading -> {
                    item {
                        HistoryLoadingCard()
                    }
                }

                errorMessage !=
                        null -> {
                    item {
                        HistoryErrorCard(
                            message =
                                errorMessage
                                    ?: "Unknown error.",
                            onRetry = {
                                refreshToken +=
                                    1
                            }
                        )
                    }
                }

                history.isEmpty() -> {
                    item {
                        HistoryEmptyCard(
                            onStartWorkout =
                                onActivity
                        )
                    }
                }

                else -> {
                    item {
                        Column {
                            Text(
                                text =
                                    "Filter workouts",
                                color =
                                    Color(0xFF0F172A),
                                fontSize =
                                    18.sp,
                                fontWeight =
                                    FontWeight.ExtraBold
                            )

                            Text(
                                text =
                                    "Every saved database workout is ordered newest first.",
                                color =
                                    Color(0xFF64748B),
                                fontSize =
                                    11.sp,
                                modifier =
                                    Modifier.padding(
                                        top =
                                            3.dp,
                                        bottom =
                                            9.dp
                                    )
                            )

                            LazyRow(
                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        8.dp
                                    ),
                                contentPadding =
                                    PaddingValues(
                                        end =
                                            8.dp
                                    )
                            ) {
                                items(
                                    filters
                                ) {
                                        filter ->

                                    HistoryFilterChip(
                                        text =
                                            filter,
                                        selected =
                                            selectedFilter ==
                                                    filter,
                                        onClick = {
                                            selectedFilter =
                                                filter
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                text =
                                    if (
                                        selectedFilter ==
                                        "All"
                                    ) {
                                        "All Workouts"
                                    } else {
                                        selectedFilter
                                    },
                                color =
                                    Color(0xFF0F172A),
                                fontSize =
                                    21.sp,
                                fontWeight =
                                    FontWeight.ExtraBold
                            )

                            Text(
                                text =
                                    "${filteredHistory.size} records",
                                color =
                                    Color(0xFF2563EB),
                                fontSize =
                                    11.sp,
                                fontWeight =
                                    FontWeight.ExtraBold,
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            999.dp
                                        )
                                    )
                                    .background(
                                        Color(0xFFEFF6FF)
                                    )
                                    .padding(
                                        horizontal =
                                            11.dp,
                                        vertical =
                                            7.dp
                                    )
                            )
                        }
                    }

                    items(
                        items =
                            filteredHistory,
                        key = {
                            it.id
                        }
                    ) {
                            workout ->

                        WorkoutHistoryCard(
                            workout =
                                workout
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTopBar(
    loading: Boolean,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        color =
            Color.White,
        shadowElevation =
            5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal =
                        15.dp,
                    vertical =
                        9.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            HistoryRoundButton(
                text =
                    "←",
                enabled =
                    !loading,
                onClick =
                    onBack
            )

            Column(
                modifier = Modifier
                    .weight(
                        1f
                    )
                    .padding(
                        start =
                            12.dp
                    )
            ) {
                Text(
                    text =
                        "Workout History",
                    color =
                        Color(0xFF0F172A),
                    fontSize =
                        20.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        "All completed database workouts",
                    color =
                        Color(0xFF64748B),
                    fontSize =
                        10.sp
                )
            }

            HistoryRoundButton(
                text =
                    "⌂",
                enabled =
                    !loading,
                onClick =
                    onHome
            )

            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )

            HistoryRoundButton(
                text =
                    if (
                        loading
                    ) {
                        "…"
                    } else {
                        "↻"
                    },
                enabled =
                    !loading,
                onClick =
                    onRefresh
            )
        }
    }
}

@Composable
private fun HistoryRoundButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(
                44.dp
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
                onClick =
                    onClick
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                text,
            color =
                if (
                    enabled
                ) {
                    Color(0xFF1D4ED8)
                } else {
                    Color(0xFF94A3B8)
                },
            fontSize =
                19.sp,
            fontWeight =
                FontWeight.ExtraBold
        )
    }
}

@Composable
private fun HistoryHero(
    summary: WorkoutHistorySummary,
    returnedRows: Int,
    totalRows: Int
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                29.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.Transparent
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    7.dp
            )
    ) {
        Column(
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
                .padding(
                    20.dp
                )
        ) {
            Text(
                text =
                    "YOUR COMPLETE ACTIVITY LOG",
                color =
                    Color.White.copy(
                        alpha =
                            0.68f
                    ),
                fontSize =
                    10.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    "${summary.totalWorkouts} completed workouts",
                color =
                    Color.White,
                fontSize =
                    25.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                modifier =
                    Modifier.padding(
                        top =
                            5.dp
                    )
            )

            Text(
                text =
                    if (
                        returnedRows <
                        totalRows
                    ) {
                        "Showing $returnedRows of $totalRows database records"
                    } else {
                        "All $totalRows database records loaded"
                    },
                color =
                    Color.White.copy(
                        alpha =
                            0.82f
                    ),
                fontSize =
                    11.sp,
                modifier =
                    Modifier.padding(
                        top =
                            3.dp,
                        bottom =
                            16.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                HistoryHeroMetric(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    label =
                        "TIME",
                    value =
                        formatHistoryDuration(
                            summary
                                .totalDurationSeconds
                        )
                )

                HistoryHeroMetric(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    label =
                        "DISTANCE",
                    value =
                        formatHistoryDistance(
                            summary
                                .totalDistanceKm
                        )
                )

                HistoryHeroMetric(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    label =
                        "CALORIES",
                    value =
                        String.format(
                            Locale.US,
                            "%.0f",
                            summary
                                .totalCalories
                        )
                )
            }
        }
    }
}

@Composable
private fun HistoryHeroMetric(
    modifier: Modifier,
    label: String,
    value: String
) {
    Column(
        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        17.dp
                    )
                )
                .background(
                    Color.White.copy(
                        alpha =
                            0.14f
                    )
                )
                .padding(
                    horizontal =
                        6.dp,
                    vertical =
                        12.dp
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                label,
            color =
                Color.White.copy(
                    alpha =
                        0.64f
                ),
            fontSize =
                8.sp,
            fontWeight =
                FontWeight.ExtraBold
        )

        Text(
            text =
                value,
            color =
                Color.White,
            fontSize =
                14.sp,
            fontWeight =
                FontWeight.ExtraBold,
            textAlign =
                TextAlign.Center,
            modifier =
                Modifier.padding(
                    top =
                        3.dp
                )
        )
    }
}

@Composable
private fun HistoryFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text =
            text,
        color =
            if (
                selected
            ) {
                Color.White
            } else {
                Color(0xFF475569)
            },
        fontSize =
            12.sp,
        fontWeight =
            FontWeight.Bold,
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    999.dp
                )
            )
            .background(
                if (
                    selected
                ) {
                    Color(0xFF2563EB)
                } else {
                    Color.White
                }
            )
            .clickable(
                onClick =
                    onClick
            )
            .padding(
                horizontal =
                    15.dp,
                vertical =
                    10.dp
            )
    )
}

@Composable
private fun WorkoutHistoryCard(
    workout: WorkoutHistoryItem
) {
    val accent =
        historyActivityColor(
            workout.activityType
        )

    val progress =
        if (
            workout.targetSeconds >
            0
        ) {
            (
                    workout.durationSeconds
                        .toFloat() /
                            workout.targetSeconds
                                .toFloat()
                    )
                .coerceIn(
                    0f,
                    1f
                )
        } else {
            0f
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                25.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    4.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    17.dp
                )
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(
                            50.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                17.dp
                            )
                        )
                        .background(
                            accent.copy(
                                alpha =
                                    0.12f
                            )
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            historyActivityEmoji(
                                workout.activityType
                            ),
                        fontSize =
                            24.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(
                            1f
                        )
                        .padding(
                            start =
                                12.dp
                        )
                ) {
                    Text(
                        text =
                            workout.workoutName
                                .ifBlank {
                                    workout.activityType
                                },
                        color =
                            Color(0xFF0F172A),
                        fontSize =
                            17.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            "${workout.levelName} • " +
                                    formatHistoryDate(
                                        workout.workoutStartedAt
                                    ),
                        color =
                            Color(0xFF64748B),
                        fontSize =
                            11.sp,
                        modifier =
                            Modifier.padding(
                                top =
                                    3.dp
                            )
                    )
                }

                AwardBadge(
                    tier =
                        workout.awardTier
                )
            }

            Row(
                modifier =
                    Modifier.padding(
                        top =
                            15.dp
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                HistoryCardMetric(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    label =
                        "TIME",
                    value =
                        formatHistoryDuration(
                            workout
                                .durationSeconds
                        )
                )

                HistoryCardMetric(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    label =
                        "DISTANCE",
                    value =
                        formatHistoryDistance(
                            workout.distanceKm
                        )
                )

                HistoryCardMetric(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    label =
                        "CALORIES",
                    value =
                        String.format(
                            Locale.US,
                            "%.1f kcal",
                            workout.calories
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        8.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            999.dp
                        )
                    )
                    .background(
                        Color(0xFFE2E8F0)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            progress
                        )
                        .height(
                            8.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                999.dp
                            )
                        )
                        .background(
                            accent
                        )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            8.dp
                    ),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text =
                        workout.intensity
                            .ifBlank {
                                workout.levelName
                            },
                    color =
                        accent,
                    fontSize =
                        10.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        if (
                            workout.targetSeconds >
                            0
                        ) {
                            "${(progress * 100f).toInt()}% of target"
                        } else {
                            "Saved workout"
                        },
                    color =
                        Color(0xFF64748B),
                    fontSize =
                        10.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            if (
                workout.hasRoute ||
                workout.notes.isNotBlank()
            ) {
                HorizontalDivider(
                    modifier =
                        Modifier.padding(
                            vertical =
                                12.dp
                        ),
                    color =
                        Color(0xFFE2E8F0)
                )

                if (
                    workout.hasRoute
                ) {
                    Text(
                        text =
                            "📍 Saved route • " +
                                    "${workout.routePointCount} points",
                        color =
                            Color(0xFF2563EB),
                        fontSize =
                            11.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                if (
                    workout.notes.isNotBlank()
                ) {
                    Text(
                        text =
                            workout.notes,
                        color =
                            Color(0xFF64748B),
                        fontSize =
                            11.sp,
                        lineHeight =
                            16.sp,
                        modifier =
                            Modifier.padding(
                                top =
                                    if (
                                        workout.hasRoute
                                    ) {
                                        7.dp
                                    } else {
                                        0.dp
                                    }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCardMetric(
    modifier: Modifier,
    label: String,
    value: String
) {
    Column(
        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        15.dp
                    )
                )
                .background(
                    Color(0xFFF8FAFC)
                )
                .padding(
                    horizontal =
                        5.dp,
                    vertical =
                        10.dp
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                label,
            color =
                Color(0xFF94A3B8),
            fontSize =
                8.sp,
            fontWeight =
                FontWeight.ExtraBold
        )

        Text(
            text =
                value,
            color =
                Color(0xFF0F172A),
            fontSize =
                11.sp,
            fontWeight =
                FontWeight.ExtraBold,
            textAlign =
                TextAlign.Center,
            modifier =
                Modifier.padding(
                    top =
                        3.dp
                )
        )
    }
}

@Composable
private fun AwardBadge(
    tier: String
) {
    val normalized =
        tier
            .trim()
            .uppercase(
                Locale.US
            )

    val emoji =
        when (
            normalized
        ) {
            "DIAMOND" ->
                "💎"

            "GOLD" ->
                "🥇"

            "SILVER" ->
                "🥈"

            else ->
                "🏅"
        }

    Text(
        text =
            "$emoji ${normalized.ifBlank { "STARTER" }}",
        color =
            historyAwardColor(
                normalized
            ),
        fontSize =
            9.sp,
        fontWeight =
            FontWeight.ExtraBold,
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    999.dp
                )
            )
            .background(
                historyAwardColor(
                    normalized
                )
                    .copy(
                        alpha =
                            0.11f
                    )
            )
            .padding(
                horizontal =
                    8.dp,
                vertical =
                    6.dp
            )
    )
}

@Composable
private fun HistoryLoadingCard() {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                23.dp
            ),
        color =
            Color.White,
        shadowElevation =
            3.dp
    ) {
        Row(
            modifier =
                Modifier.padding(
                    19.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(
                        26.dp
                    ),
                color =
                    Color(0xFF2563EB),
                strokeWidth =
                    3.dp
            )

            Text(
                text =
                    "Loading every workout from MySQL...",
                color =
                    Color(0xFF475569),
                fontSize =
                    13.sp,
                fontWeight =
                    FontWeight.Bold,
                modifier =
                    Modifier.padding(
                        start =
                            13.dp
                    )
            )
        }
    }
}

@Composable
private fun HistoryErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                23.dp
            ),
        color =
            Color(0xFFFFF1F2)
    ) {
        Column(
            modifier =
                Modifier.padding(
                    18.dp
                )
        ) {
            Text(
                text =
                    "History could not be loaded",
                color =
                    Color(0xFFBE123C),
                fontSize =
                    17.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    message,
                color =
                    Color(0xFF9F1239),
                fontSize =
                    12.sp,
                lineHeight =
                    17.sp,
                modifier =
                    Modifier.padding(
                        top =
                            7.dp
                    )
            )

            Button(
                onClick =
                    onRetry,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFFBE123C)
                    ),
                shape =
                    RoundedCornerShape(
                        15.dp
                    ),
                modifier =
                    Modifier.padding(
                        top =
                            13.dp
                    )
            ) {
                Text(
                    text =
                        "Try Again",
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HistoryEmptyCard(
    onStartWorkout: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                25.dp
            ),
        color =
            Color.White,
        shadowElevation =
            3.dp
    ) {
        Column(
            modifier =
                Modifier.padding(
                    22.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    "🏃",
                fontSize =
                    42.sp
            )

            Text(
                text =
                    "No workout history yet",
                color =
                    Color(0xFF0F172A),
                fontSize =
                    19.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                modifier =
                    Modifier.padding(
                        top =
                            10.dp
                    )
            )

            Text(
                text =
                    "Finish and save a workout. It will appear here from the database.",
                color =
                    Color(0xFF64748B),
                fontSize =
                    12.sp,
                lineHeight =
                    18.sp,
                textAlign =
                    TextAlign.Center,
                modifier =
                    Modifier.padding(
                        top =
                            6.dp
                    )
            )

            Button(
                onClick =
                    onStartWorkout,
                shape =
                    RoundedCornerShape(
                        17.dp
                    ),
                modifier =
                    Modifier.padding(
                        top =
                            15.dp
                    )
            ) {
                Text(
                    text =
                        "Start Activity",
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun HistoryFooter(
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onSummary: () -> Unit,
    onProgress: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            Color.White,
        shadowElevation =
            16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal =
                        5.dp,
                    vertical =
                        8.dp
                ),
            horizontalArrangement =
                Arrangement.SpaceEvenly,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            HistoryFooterItem(
                icon =
                    "⌂",
                label =
                    "Home",
                selected =
                    false,
                onClick =
                    onHome
            )

            HistoryFooterItem(
                icon =
                    "🏃",
                label =
                    "Activity",
                selected =
                    false,
                onClick =
                    onActivity
            )

            HistoryFooterItem(
                icon =
                    "📋",
                label =
                    "Summary",
                selected =
                    false,
                onClick =
                    onSummary
            )

            HistoryFooterItem(
                icon =
                    "📈",
                label =
                    "Progress",
                selected =
                    false,
                onClick =
                    onProgress
            )

            HistoryFooterItem(
                icon =
                    "🕘",
                label =
                    "History",
                selected =
                    true,
                onClick = {}
            )

            HistoryFooterItem(
                icon =
                    "👤",
                label =
                    "Profile",
                selected =
                    false,
                onClick =
                    onProfile
            )
        }
    }
}

@Composable
private fun HistoryFooterItem(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(
                56.dp
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
            fontSize =
                16.sp
        )

        Text(
            text =
                label,
            color =
                if (
                    selected
                ) {
                    Color(0xFF2563EB)
                } else {
                    Color(0xFF64748B)
                },
            fontSize =
                8.sp,
            fontWeight =
                if (
                    selected
                ) {
                    FontWeight.ExtraBold
                } else {
                    FontWeight.Bold
                },
            modifier =
                Modifier.padding(
                    top =
                        2.dp
                )
        )
    }
}

private fun historyActivityEmoji(
    activityType: String
): String {
    return when (
        activityType
            .trim()
            .lowercase(
                Locale.US
            )
    ) {
        "running" ->
            "🏃"

        "walking" ->
            "🚶"

        "cycling" ->
            "🚴"

        "weightlifting" ->
            "🏋️"

        "yoga" ->
            "🧘"

        "hiit" ->
            "🔥"

        "swimming" ->
            "🏊"

        "rowing" ->
            "🚣"

        else ->
            "⚡"
    }
}

private fun historyActivityColor(
    activityType: String
): Color {
    return when (
        activityType
            .trim()
            .lowercase(
                Locale.US
            )
    ) {
        "running" ->
            Color(0xFF2563EB)

        "walking" ->
            Color(0xFF16A34A)

        "cycling" ->
            Color(0xFF0891B2)

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

private fun historyAwardColor(
    tier: String
): Color {
    return when (
        tier
            .trim()
            .uppercase(
                Locale.US
            )
    ) {
        "DIAMOND" ->
            Color(0xFF0284C7)

        "GOLD" ->
            Color(0xFFCA8A04)

        "SILVER" ->
            Color(0xFF64748B)

        else ->
            Color(0xFF7C3AED)
    }
}

private fun formatHistoryDuration(
    totalSeconds: Int
): String {
    val seconds =
        totalSeconds.coerceAtLeast(
            0
        )

    val hours =
        seconds /
                3_600

    val minutes =
        (
                seconds %
                        3_600
                ) /
                60

    return when {
        hours >
                0 ->
            "${hours}h ${minutes}m"

        minutes >
                0 ->
            "${minutes} min"

        else ->
            "${seconds} sec"
    }
}

private fun formatHistoryDistance(
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
            "%.0f m",
            safeDistance *
                    1_000.0
        )
    } else {
        String.format(
            Locale.US,
            "%.2f km",
            safeDistance
        )
    }
}

private fun formatHistoryDate(
    value: String
): String {
    if (
        value.isBlank()
    ) {
        return "Unknown date"
    }

    val inputPatterns =
        listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
        )

    inputPatterns.forEach {
            pattern ->

        try {
            val input =
                SimpleDateFormat(
                    pattern,
                    Locale.US
                )

            val parsed =
                input.parse(
                    value
                )
                    ?: return@forEach

            val output =
                SimpleDateFormat(
                    "dd MMM yyyy • h:mm a",
                    Locale.US
                )

            return output.format(
                parsed
            )
        } catch (
            _: Exception
        ) {
            /*
             * Try the next supported server format.
             */
        }
    }

    return value
}
