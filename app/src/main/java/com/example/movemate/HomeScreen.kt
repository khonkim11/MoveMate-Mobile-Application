package com.example.movemate

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlin.math.roundToInt

private val HomeBackgroundTop =
    Color(0xFFEFF6FF)

private val HomeBackgroundMiddle =
    Color(0xFFF8FAFC)

private val HomeBackgroundBottom =
    Color(0xFFF1F5F9)

private val HomeInk =
    Color(0xFF0F172A)

private val HomeMuted =
    Color(0xFF64748B)

private val HomePrimary =
    Color(0xFF2563EB)

private val HomePrimaryDark =
    Color(0xFF1D4ED8)

private val HomeCyan =
    Color(0xFF06B6D4)

private val HomeGreen =
    Color(0xFF16A34A)

@Composable
fun HomeScreen(
    session: UserSession,
    onBack: () -> Unit,
    onActivity: () -> Unit,

    onStartSampleWorkout:
        (
        WorkoutActivityOption,
        WorkoutLevel
    ) -> Unit,

    onOpenActiveWorkout: (ActiveWorkoutState) -> Unit,
    onSummary: () -> Unit,
    onProgress: () -> Unit,
    onGoal: () -> Unit,
    onHistory: () -> Unit,
    onHealth: () -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit,
    refreshToken: Int
) {
    BackHandler(
        onBack =
            onBack
    )

    val resolvedState =
        rememberResolvedMoveMateSession(
            session
        )

    val activeSession =
        resolvedState.session

    val goalRefreshVersion =
        GoalRefreshBus.version

    var loading by remember(
        activeSession.userId
    ) {
        mutableStateOf(
            true
        )
    }

    var dashboardResponse by remember(
        activeSession.userId
    ) {
        mutableStateOf(
            JSONObject()
        )
    }

    var savedGoal by remember(
        activeSession.userId,
        activeSession.email
    ) {
        mutableStateOf<GoalCenterData?>(
            null
        )
    }

    var errorMessage by remember(
        activeSession.userId
    ) {
        mutableStateOf(
            ""
        )
    }

    var localRefresh by remember {
        mutableIntStateOf(
            0
        )
    }

    LaunchedEffect(
        activeSession.userId,
        refreshToken,
        localRefresh,
        goalRefreshVersion,
        resolvedState.loading
    ) {
        if (
            resolvedState.loading
        ) {
            return@LaunchedEffect
        }

        if (
            activeSession.userId <=
            0
        ) {
            loading =
                false

            errorMessage =
                resolvedState
                    .errorMessage

            return@LaunchedEffect
        }

        loading =
            true

        errorMessage =
            ""

        try {
            val summaryResult =
                WorkoutApiService
                    .getTodaySummary(
                        activeSession.userId
                    )

            if (
                summaryResult.optBoolean(
                    "success",
                    false
                )
            ) {
                dashboardResponse =
                    summaryResult
            } else {
                errorMessage =
                    friendlyApiMessage(
                        response =
                            summaryResult,
                        fallback =
                            "Could not load today's workout totals."
                    )
            }

            val goalResult =
                GoalCenterApiService
                    .getUserGoal(
                        activeSession.userId
                    )

            when {
                goalResult.optBoolean(
                    "success",
                    false
                ) &&
                        goalResult.optBoolean(
                            "exists",
                            false
                        ) -> {
                    val goalJson =
                        goalResult
                            .optJSONObject(
                                "goal"
                            )
                            ?: JSONObject()

                    savedGoal =
                        goalCenterDataFromJson(
                            json =
                                goalJson,
                            session =
                                activeSession
                        )
                }

                goalResult.optBoolean(
                    "success",
                    false
                ) -> {
                    savedGoal =
                        null
                }

                errorMessage.isBlank() -> {
                    errorMessage =
                        friendlyApiMessage(
                            response =
                                goalResult,
                            fallback =
                                "Could not load your saved goal."
                        )
                }
            }
        } catch (
            exception: Exception
        ) {
            errorMessage =
                "Cannot load Home data: " +
                        (
                                exception.message
                                    ?: exception
                                        .javaClass
                                        .simpleName
                                )
        } finally {
            loading =
                false
        }
    }

    val summarySuccess =
        dashboardResponse
            .optBoolean(
                "success",
                false
            )

    val savedWorkouts =
        if (
            summarySuccess
        ) {
            dashboardResponse
                .optInt(
                    "total_workouts",
                    0
                )
        } else {
            0
        }

    val savedSeconds =
        if (
            summarySuccess
        ) {
            dashboardResponse
                .optInt(
                    "total_duration_seconds",
                    0
                )
        } else {
            0
        }

    val savedDistanceKm =
        if (
            summarySuccess
        ) {
            dashboardResponse
                .optDouble(
                    "total_distance_km",
                    0.0
                )
        } else {
            0.0
        }

    val savedCalories =
        if (
            summarySuccess
        ) {
            dashboardResponse
                .optDouble(
                    "total_calories",
                    0.0
                )
        } else {
            0.0
        }

    val activeWorkouts =
        ActiveWorkoutManager
            .activeWorkouts
            .filter {
                !it.isFinished &&
                        (
                                it.userId ==
                                        activeSession.userId ||
                                        (
                                                it.userId <=
                                                        0 &&
                                                        session.userId <=
                                                        0
                                                )
                                )
            }

    val liveCalories =
        activeWorkouts
            .sumOf {
                it.calories
            }

    val liveSeconds =
        activeWorkouts
            .sumOf {
                it.elapsedSeconds
            }

    val liveDistanceKm =
        activeWorkouts
            .sumOf {
                it.distanceKm
            }

    val totalWorkouts =
        savedWorkouts

    val totalSeconds =
        savedSeconds +
                liveSeconds

    val totalDistanceKm =
        savedDistanceKm +
                liveDistanceKm

    val totalCalories =
        savedCalories +
                liveCalories

    val workoutCaloriesGoal =
        savedGoal
            ?.workoutCaloriesTarget
            ?.toDoubleOrNull()
            ?.takeIf {
                it >
                        0.0
            }

    val calorieProgress =
        workoutCaloriesGoal
            ?.let {
                    target ->

                (
                        totalCalories /
                                target
                        )
                    .toFloat()
                    .coerceIn(
                        0f,
                        1f
                    )
            }
            ?: 0f

    val remainingCalories =
        workoutCaloriesGoal
            ?.let {
                    target ->

                (
                        target -
                                totalCalories
                        )
                    .coerceAtLeast(
                        0.0
                    )
            }
            ?: 0.0

    val goalCompleted =
        workoutCaloriesGoal
            ?.let {
                    target ->

                totalCalories >=
                        target
            }
            ?: false

    val activityStats:
            JSONArray =
        if (
            summarySuccess
        ) {
            dashboardResponse
                .optJSONArray(
                    "activity_stats"
                )
                ?: JSONArray()
        } else {
            JSONArray()
        }

    val topActivity =
        activityStats
            .optJSONObject(
                0
            )
            ?.optString(
                "activity_type"
            )
            ?.takeIf {
                it.isNotBlank()
            }
            ?: activeWorkouts
                .firstOrNull()
                ?.activityName
            ?: "No activity yet"

    val animatedWorkoutCount by
    animateIntAsState(
        targetValue =
            totalWorkouts,
        animationSpec =
            tween(
                durationMillis =
                    650
            )
    )

    val animatedCalories by
    animateFloatAsState(
        targetValue =
            totalCalories
                .toFloat(),
        animationSpec =
            tween(
                durationMillis =
                    800
            )
    )

    val animatedDistance by
    animateFloatAsState(
        targetValue =
            totalDistanceKm
                .toFloat(),
        animationSpec =
            tween(
                durationMillis =
                    800
            )
    )

    val animatedProgress by
    animateFloatAsState(
        targetValue =
            calorieProgress,
        animationSpec =
            tween(
                durationMillis =
                    850
            )
    )

    Scaffold(
        containerColor =
            Color.Transparent,
        bottomBar = {
            HomeFooterMenu(
                onHome = {
                    localRefresh +=
                        1
                },
                onActivity =
                    onActivity,
                onProgress =
                    onProgress,
                onGoal =
                    onGoal,
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
                        colors =
                            listOf(
                                HomeBackgroundTop,
                                HomeBackgroundMiddle,
                                HomeBackgroundBottom
                            )
                    )
                )
                .padding(
                    scaffoldPadding
                )
        ) {
            HomeBackgroundDecoration()

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
                        15.dp
                    )
            ) {


                item {
                    HomeHeroHeader(
                        session =
                            activeSession,
                        totalCalories =
                            animatedCalories
                                .toDouble(),
                        onBack =
                            onBack,
                        onProfile =
                            onProfile
                    )
                }

                item {
                    AnimatedVisibility(
                        visible =
                            resolvedState.loading,
                        enter =
                            fadeIn() +
                                    slideInVertically {
                                        it /
                                                5
                                    },
                        exit =
                            fadeOut() +
                                    slideOutVertically {
                                        it /
                                                5
                                    }
                    ) {
                        HomeLoadingCard(
                            text =
                                "Repairing your saved login..."
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible =
                            !resolvedState.loading &&
                                    errorMessage.isNotBlank(),
                        enter =
                            fadeIn() +
                                    slideInVertically {
                                        it /
                                                5
                                    },
                        exit =
                            fadeOut()
                    ) {
                        HomeErrorCard(
                            message =
                                errorMessage,
                            onRetry = {
                                localRefresh +=
                                    1
                            }
                        )
                    }
                }

                if (
                    activeWorkouts.isNotEmpty()
                ) {
                    item {
                        HomeContinuingWorkoutCard(
                            workouts =
                                activeWorkouts,
                            onContinue = {
                                    activeWorkout:
                                    ActiveWorkoutState ->

                                onOpenActiveWorkout(
                                    activeWorkout
                                )
                            }
                        )
                    }
                }


                /*
                 * Home-only one-minute samples.
                 *
                 * Normal Start Workout still opens:
                 * Activity -> Easy / Medium / Hard.
                 */
                item {
                    HomeOneMinuteSamples(
                        activities =
                            workoutActivityOptions,
                        enabled =
                            true,
                        onStartSample =
                            onStartSampleWorkout
                    )
                }

                item {
                    TodayBurnGoalCard(
                        loading =
                            loading ||
                                    resolvedState.loading,
                        totalWorkouts =
                            animatedWorkoutCount,
                        totalCalories =
                            animatedCalories
                                .toDouble(),
                        workoutTarget =
                            workoutCaloriesGoal,
                        progress =
                            animatedProgress,
                        remainingCalories =
                            remainingCalories,
                        goalCompleted =
                            goalCompleted,
                        goal =
                            savedGoal,
                        activeWorkoutCount =
                            activeWorkouts.size,
                        onStartWorkout =
                            onActivity,
                        onGoal =
                            onGoal
                    )
                }

                item {
                    Text(
                        text =
                            "Today at a glance",
                        color =
                            HomeInk,
                        fontSize =
                            19.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }

                item {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                12.dp
                            )
                    ) {
                        HomeMetricCard(
                            title =
                                "Calories",
                            value =
                                String.format(
                                    Locale.US,
                                    "%.1f",
                                    animatedCalories
                                ),
                            subtitle =
                                "kcal burned",
                            emoji =
                                "🔥",
                            accent =
                                Color(0xFFF97316),
                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )

                        HomeMetricCard(
                            title =
                                "Distance",
                            value =
                                if (
                                    animatedDistance <
                                    1f
                                ) {
                                    String.format(
                                        Locale.US,
                                        "%.0f m",
                                        animatedDistance *
                                                1000f
                                    )
                                } else {
                                    String.format(
                                        Locale.US,
                                        "%.2f km",
                                        animatedDistance
                                    )
                                },
                            subtitle =
                                "today",
                            emoji =
                                "📍",
                            accent =
                                HomeCyan,
                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )
                    }
                }

                item {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                12.dp
                            )
                    ) {
                        HomeMetricCard(
                            title =
                                "Duration",
                            value =
                                formatHomeDuration(
                                    totalSeconds
                                ),
                            subtitle =
                                "active time",
                            emoji =
                                "⏱️",
                            accent =
                                Color(0xFF8B5CF6),
                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )

                        HomeMetricCard(
                            title =
                                "Top Activity",
                            value =
                                topActivity,
                            subtitle =
                                "most active",
                            emoji =
                                "⭐",
                            accent =
                                Color(0xFFEAB308),
                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )
                    }
                }

                item {
                    HomeQuickTools(
                        onSummary =
                            onSummary,
                        onHistory =
                            onHistory,
                        onHealth =
                            onHealth,
                        onGoal =
                            onGoal
                    )
                }

                item {
                    HomeAccountCard(
                        loading =
                            loading ||
                                    resolvedState.loading,
                        onRefresh = {
                            localRefresh +=
                                1
                        },
                        onProfile =
                            onProfile,
                        onLogout =
                            onLogout
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.HomeBackgroundDecoration() {
    Box(
        modifier = Modifier
            .padding(
                top =
                    40.dp,
                end =
                    14.dp
            )
            .size(
                120.dp
            )
            .clip(
                CircleShape
            )
            .background(
                HomePrimary
                    .copy(
                        alpha =
                            0.055f
                    )
            )
            .align(
                Alignment.TopEnd
            )
    )

    Box(
        modifier = Modifier
            .padding(
                start =
                    10.dp,
                bottom =
                    120.dp
            )
            .size(
                84.dp
            )
            .clip(
                CircleShape
            )
            .background(
                HomeCyan
                    .copy(
                        alpha =
                            0.045f
                    )
            )
            .align(
                Alignment.BottomStart
            )
    )
}

@Composable
private fun HomeHeroHeader(
    session: UserSession,
    totalCalories: Double,
    onBack: () -> Unit,
    onProfile: () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                28.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.Transparent
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    8.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors =
                            listOf(
                                Color(0xFF0F172A),
                                Color(0xFF1E3A8A),
                                HomePrimary
                            )
                    )
                )
                .padding(
                    19.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(
                                42.dp
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                Color.White
                                    .copy(
                                        alpha =
                                            0.12f
                                    )
                            )
                            .clickable(
                                onClick =
                                    onBack
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            "‹",
                        color =
                            Color.White,
                        fontSize =
                            30.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Column(
                    modifier =
                        Modifier
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
                            "Good to see you",
                        color =
                            Color.White
                                .copy(
                                    alpha =
                                        0.72f
                                ),
                        fontSize =
                            12.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            session.fullName
                                .ifBlank {
                                    "MoveMate User"
                                },
                        color =
                            Color.White,
                        fontSize =
                            24.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }

                Box(
                    modifier =
                        Modifier
                            .size(
                                48.dp
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                Color.White
                                    .copy(
                                        alpha =
                                            0.16f
                                    )
                            )
                            .clickable(
                                onClick =
                                    onProfile
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            session.fullName
                                .trim()
                                .firstOrNull()
                                ?.uppercase()
                                ?: "U",
                        color =
                            Color.White,
                        fontSize =
                            18.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.Bottom
            ) {
                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Text(
                        text =
                            "Today's movement",
                        color =
                            Color.White
                                .copy(
                                    alpha =
                                        0.68f
                                ),
                        fontSize =
                            11.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            String.format(
                                Locale.US,
                                "%.1f kcal",
                                totalCalories
                            ),
                        color =
                            Color.White,
                        fontSize =
                            30.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }

                Text(
                    text =
                        "MOVE • TRACK • GROW",
                    color =
                        Color.White,
                    fontSize =
                        9.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    modifier =
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    999.dp
                                )
                            )
                            .background(
                                Color.White
                                    .copy(
                                        alpha =
                                            0.12f
                                    )
                            )
                            .padding(
                                horizontal =
                                    10.dp,
                                vertical =
                                    7.dp
                            )
                )
            }
        }
    }
}

@Composable
private fun HomeLoadingCard(
    text: String
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                20.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    17.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(
                        24.dp
                    ),
                color =
                    HomePrimary,
                strokeWidth =
                    3.dp
            )

            Text(
                text =
                    text,
                color =
                    HomeMuted,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HomeErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                21.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFFFFF1F2)
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            Text(
                text =
                    "Could not refresh Home",
                color =
                    Color(0xFFBE123C),
                fontSize =
                    15.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    message,
                color =
                    Color(0xFF9F1239),
                fontSize =
                    12.sp
            )

            OutlinedButton(
                onClick =
                    onRetry
            ) {
                Text(
                    text =
                        "Try Again"
                )
            }
        }
    }
}

@Composable
private fun HomeContinuingWorkoutCard(
    workouts: List<ActiveWorkoutState>,
    onContinue:
        (
        ActiveWorkoutState
    ) -> Unit
) {
    val primaryWorkout =
        workouts.firstOrNull {
            it.isRunning
        } ?: workouts.first()

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource
        .collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (
                pressed
            ) {
                0.985f
            } else {
                1f
            },
        animationSpec =
            spring(
                dampingRatio =
                    0.88f,
                stiffness =
                    500f
            )
    )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .scale(
                    scale
                )
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null
                ) {
                    onContinue(
                        primaryWorkout
                    )
                },
        shape =
            RoundedCornerShape(
                24.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.Transparent
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    6.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors =
                            listOf(
                                Color(0xFF07152F),
                                primaryWorkout
                                    .accentColor
                            )
                    )
                )
                .padding(
                    17.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            52.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                17.dp
                            )
                        )
                        .background(
                            Color.White
                                .copy(
                                    alpha =
                                        0.14f
                                )
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        primaryWorkout
                            .activityEmoji,
                    fontSize =
                        28.sp
                )
            }

            Column(
                modifier =
                    Modifier
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
                        if (
                            primaryWorkout.isRunning
                        ) {
                            "Workout is live"
                        } else {
                            "Workout is paused"
                        },
                    color =
                        Color(0xFFBAE6FD),
                    fontSize =
                        10.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        "${primaryWorkout.activityName} • " +
                                primaryWorkout.levelName,
                    color =
                        Color.White,
                    fontSize =
                        17.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        "${formatHomeDuration(primaryWorkout.elapsedSeconds)} • " +
                                String.format(
                                    Locale.US,
                                    "%.1f live kcal",
                                    workouts
                                        .sumOf {
                                            it.calories
                                        }
                                ),
                    color =
                        Color.White
                            .copy(
                                alpha =
                                    0.76f
                            ),
                    fontSize =
                        11.sp
                )
            }

            Text(
                text =
                    "Continue ›",
                color =
                    Color.White,
                fontSize =
                    12.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun TodayBurnGoalCard(
    loading: Boolean,
    totalWorkouts: Int,
    totalCalories: Double,
    workoutTarget: Double?,
    progress: Float,
    remainingCalories: Double,
    goalCompleted: Boolean,
    goal: GoalCenterData?,
    activeWorkoutCount: Int,
    onStartWorkout: () -> Unit,
    onGoal: () -> Unit
) {
    val safeProgress =
        progress.coerceIn(
            0f,
            1f
        )

    val percent =
        (
                safeProgress *
                        100f
                )
            .roundToInt()
            .coerceIn(
                0,
                100
            )

    val goalCardShape =
        RoundedCornerShape(
            28.dp
        )

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize(),
        shape =
            goalCardShape,
        color =
            Color.Transparent,
        border =
            BorderStroke(
                width =
                    1.dp,
                color =
                    if (
                        goalCompleted
                    ) {
                        HomeGreen
                            .copy(
                                alpha =
                                    0.24f
                            )
                    } else {
                        HomePrimary
                            .copy(
                                alpha =
                                    0.16f
                            )
                    }
            ),
        shadowElevation =
            0.dp,
        tonalElevation =
            0.dp
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                if (
                                    goalCompleted
                                ) {
                                    listOf(
                                        Color(0xFFF0FDF4),
                                        Color.White
                                    )
                                } else {
                                    listOf(
                                        Color(0xFFF8FBFF),
                                        Color.White
                                    )
                                }
                        )
                    )
                    .padding(
                        20.dp
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    15.dp
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(
                            52.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                17.dp
                            )
                        )
                        .background(
                            if (
                                goalCompleted
                            ) {
                                Color(0xFFDCFCE7)
                            } else {
                                Color(0xFFDBEAFE)
                            }
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            if (
                                goalCompleted
                            ) {
                                "✅"
                            } else {
                                "🔥"
                            },
                        fontSize =
                            27.sp
                    )
                }

                Column(
                    modifier =
                        Modifier
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
                            if (
                                goalCompleted
                            ) {
                                "Burn goal completed"
                            } else {
                                "Today's calorie goal"
                            },
                        color =
                            HomeInk,
                        fontSize =
                            20.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            when {
                                loading ->
                                    "Loading your saved goal..."

                                goal !=
                                        null ->
                                    "${goal.goalType} • ${goal.activityFocus}"

                                else ->
                                    "Create a target in Goal Center"
                            },
                        color =
                            HomeMuted,
                        fontSize =
                            11.sp
                    )
                }

                if (
                    workoutTarget !=
                    null
                ) {
                    Text(
                        text =
                            "$percent%",
                        color =
                            if (
                                goalCompleted
                            ) {
                                HomeGreen
                            } else {
                                HomePrimary
                            },
                        fontSize =
                            16.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }

            when {
                loading -> {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical =
                                        14.dp
                                ),
                        horizontalArrangement =
                            Arrangement.Center,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    26.dp
                                ),
                            color =
                                HomePrimary,
                            strokeWidth =
                                3.dp
                        )

                        Text(
                            text =
                                "Loading today's totals...",
                            color =
                                HomeMuted,
                            fontSize =
                                12.sp,
                            modifier =
                                Modifier.padding(
                                    start =
                                        10.dp
                                )
                        )
                    }
                }

                workoutTarget ==
                        null -> {
                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(
                                19.dp
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
                                    16.dp
                                ),
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    7.dp
                                )
                        ) {
                            Text(
                                text =
                                    String.format(
                                        Locale.US,
                                        "%.1f kcal burned today",
                                        totalCalories
                                    ),
                                color =
                                    Color(0xFF9A3412),
                                fontSize =
                                    20.sp,
                                fontWeight =
                                    FontWeight.ExtraBold
                            )

                            Text(
                                text =
                                    "Set Workout Calories Burn in Goal Center to show the exact remaining amount and progress.",
                                color =
                                    Color(0xFF7C2D12),
                                fontSize =
                                    12.sp
                            )
                        }
                    }

                    Button(
                        onClick =
                            onGoal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                52.dp
                            ),
                        shape =
                            RoundedCornerShape(
                                17.dp
                            ),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    HomePrimary
                            )
                    ) {
                        Text(
                            text =
                                "Create Burn Goal",
                            fontWeight =
                                FontWeight.ExtraBold
                        )
                    }
                }

                else -> {
                    Row(
                        verticalAlignment =
                            Alignment.Bottom
                    ) {
                        Text(
                            text =
                                String.format(
                                    Locale.US,
                                    "%.1f",
                                    totalCalories
                                ),
                            color =
                                if (
                                    goalCompleted
                                ) {
                                    HomeGreen
                                } else {
                                    HomePrimaryDark
                                },
                            fontSize =
                                34.sp,
                            fontWeight =
                                FontWeight.ExtraBold
                        )

                        Text(
                            text =
                                " kcal burned",
                            color =
                                HomeMuted,
                            fontSize =
                                12.sp,
                            modifier =
                                Modifier.padding(
                                    bottom =
                                        6.dp,
                                    start =
                                        4.dp
                                )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                12.dp
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
                                    fraction =
                                        safeProgress
                                )
                                .height(
                                    12.dp
                                )
                                .clip(
                                    RoundedCornerShape(
                                        999.dp
                                    )
                                )
                                .background(
                                    Brush.horizontalGradient(
                                        colors =
                                            if (
                                                goalCompleted
                                            ) {
                                                listOf(
                                                    HomeGreen,
                                                    Color(0xFF4ADE80)
                                                )
                                            } else {
                                                listOf(
                                                    HomePrimary,
                                                    HomeCyan
                                                )
                                            }
                                    )
                                )
                        )
                    }

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                9.dp
                            )
                    ) {
                        BurnGoalMetricBox(
                            title =
                                "TARGET",
                            value =
                                String.format(
                                    Locale.US,
                                    "%.0f kcal",
                                    workoutTarget
                                ),
                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )

                        BurnGoalMetricBox(
                            title =
                                if (
                                    goalCompleted
                                ) {
                                    "ABOVE"
                                } else {
                                    "LEFT"
                                },
                            value =
                                if (
                                    goalCompleted
                                ) {
                                    String.format(
                                        Locale.US,
                                        "%.1f kcal",
                                        (
                                                totalCalories -
                                                        workoutTarget
                                                )
                                            .coerceAtLeast(
                                                0.0
                                            )
                                    )
                                } else {
                                    String.format(
                                        Locale.US,
                                        "%.1f kcal",
                                        remainingCalories
                                    )
                                },
                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )

                        BurnGoalMetricBox(
                            title =
                                "WORKOUTS",
                            value =
                                totalWorkouts
                                    .toString(),
                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )
                    }

                    if (
                        activeWorkoutCount >
                        0
                    ) {
                        Text(
                            text =
                                "$activeWorkoutCount active session" +
                                        if (
                                            activeWorkoutCount ==
                                            1
                                        ) {
                                            ""
                                        } else {
                                            "s"
                                        } +
                                        " included in the live totals",
                            color =
                                HomeMuted,
                            fontSize =
                                10.sp,
                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {
                        Button(
                            onClick =
                                onStartWorkout,
                            modifier = Modifier
                                .weight(
                                    1f
                                )
                                .height(
                                    52.dp
                                ),
                            shape =
                                RoundedCornerShape(
                                    17.dp
                                ),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        if (
                                            goalCompleted
                                        ) {
                                            HomeGreen
                                        } else {
                                            HomePrimary
                                        }
                                )
                        ) {
                            Text(
                                text =
                                    if (
                                        goalCompleted
                                    ) {
                                        "Keep Moving"
                                    } else {
                                        "Start Workout"
                                    },
                                fontWeight =
                                    FontWeight.ExtraBold
                            )
                        }

                        OutlinedButton(
                            onClick =
                                onGoal,
                            modifier =
                                Modifier.height(
                                    52.dp
                                ),
                            shape =
                                RoundedCornerShape(
                                    17.dp
                                )
                        ) {
                            Text(
                                text =
                                    "Edit Goal",
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
private fun BurnGoalMetricBox(
    title: String,
    value: String,
    modifier: Modifier
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
                        8.dp,
                    vertical =
                        10.dp
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                title,
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
                HomeInk,
            fontSize =
                13.sp,
            fontWeight =
                FontWeight.ExtraBold,
            maxLines =
                1,
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
private fun HomeMetricCard(
    title: String,
    value: String,
    subtitle: String,
    emoji: String,
    accent: Color,
    modifier: Modifier
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource
        .collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (
                pressed
            ) {
                0.98f
            } else {
                1f
            },
        animationSpec =
            spring(
                dampingRatio =
                    0.88f,
                stiffness =
                    520f
            )
    )

    val elevation by
    animateDpAsState(
        targetValue =
            if (
                pressed
            ) {
                1.dp
            } else {
                4.dp
            },
        animationSpec =
            tween(
                durationMillis =
                    160
            )
    )

    Card(
        modifier =
            modifier.scale(
                scale
            ),
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
                defaultElevation =
                    elevation
            )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource =
                            interactionSource,
                        indication =
                            null
                    ) {}
                    .padding(
                        15.dp
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    5.dp
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(
                                37.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    12.dp
                                )
                            )
                            .background(
                                accent
                                    .copy(
                                        alpha =
                                            0.12f
                                    )
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            emoji,
                        fontSize =
                            19.sp
                    )
                }

                Spacer(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                Box(
                    modifier =
                        Modifier
                            .size(
                                7.dp
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                accent
                            )
                )
            }

            Text(
                text =
                    title,
                color =
                    HomeMuted,
                fontSize =
                    10.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    value,
                color =
                    HomeInk,
                fontSize =
                    18.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                maxLines =
                    1
            )

            Text(
                text =
                    subtitle,
                color =
                    Color(0xFF94A3B8),
                fontSize =
                    9.sp
            )
        }
    }
}

@Composable
private fun HomeQuickTools(
    onSummary: () -> Unit,
    onHistory: () -> Unit,
    onHealth: () -> Unit,
    onGoal: () -> Unit
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                11.dp
            )
    ) {
        Text(
            text =
                "Quick tools",
            color =
                HomeInk,
            fontSize =
                19.sp,
            fontWeight =
                FontWeight.ExtraBold
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            HomeQuickAction(
                emoji =
                    "📊",
                title =
                    "Summary",
                subtitle =
                    "Today's totals",
                accent =
                    HomePrimary,
                onClick =
                    onSummary,
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            HomeQuickAction(
                emoji =
                    "🕘",
                title =
                    "History",
                subtitle =
                    "Past workouts",
                accent =
                    Color(0xFF8B5CF6),
                onClick =
                    onHistory,
                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            HomeQuickAction(
                emoji =
                    "❤️",
                title =
                    "Health",
                subtitle =
                    "Body overview",
                accent =
                    Color(0xFFEF4444),
                onClick =
                    onHealth,
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            HomeQuickAction(
                emoji =
                    "🎯",
                title =
                    "Goals",
                subtitle =
                    "Targets & plans",
                accent =
                    Color(0xFF16A34A),
                onClick =
                    onGoal,
                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }
    }
}

@Composable
private fun HomeQuickAction(
    emoji: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource
        .collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (
                pressed
            ) {
                0.97f
            } else {
                1f
            },
        animationSpec =
            spring(
                dampingRatio =
                    0.84f,
                stiffness =
                    500f
            )
    )

    Card(
        modifier =
            modifier
                .scale(
                    scale
                )
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    onClick =
                        onClick
                ),
        shape =
            RoundedCornerShape(
                20.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    3.dp
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    14.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            42.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                14.dp
                            )
                        )
                        .background(
                            accent
                                .copy(
                                    alpha =
                                        0.12f
                                )
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        emoji,
                    fontSize =
                        21.sp
                )
            }

            Column(
                modifier =
                    Modifier.padding(
                        start =
                            10.dp
                    )
            ) {
                Text(
                    text =
                        title,
                    color =
                        HomeInk,
                    fontSize =
                        13.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        subtitle,
                    color =
                        HomeMuted,
                    fontSize =
                        9.sp
                )
            }
        }
    }
}

@Composable
private fun HomeAccountCard(
    loading: Boolean,
    onRefresh: () -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit
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
                defaultElevation =
                    2.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    15.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            Text(
                text =
                    "Account and data",
                color =
                    HomeInk,
                fontSize =
                    15.sp,
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
                OutlinedButton(
                    onClick =
                        onRefresh,
                    enabled =
                        !loading,
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    shape =
                        RoundedCornerShape(
                            15.dp
                        )
                ) {
                    Text(
                        text =
                            "Refresh"
                    )
                }

                OutlinedButton(
                    onClick =
                        onProfile,
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    shape =
                        RoundedCornerShape(
                            15.dp
                        )
                ) {
                    Text(
                        text =
                            "Profile"
                    )
                }

                OutlinedButton(
                    onClick =
                        onLogout,
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    shape =
                        RoundedCornerShape(
                            15.dp
                        )
                ) {
                    Text(
                        text =
                            "Log Out"
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeFooterMenu(
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onProgress: () -> Unit,
    onGoal: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        color =
            Color.White,
        shadowElevation =
            14.dp,
        shape =
            RoundedCornerShape(
                topStart =
                    25.dp,
                topEnd =
                    25.dp
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start =
                            10.dp,
                        top =
                            9.dp,
                        end =
                            10.dp,
                        bottom =
                            9.dp
                    ),
            horizontalArrangement =
                Arrangement.SpaceAround,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            HomeFooterItem(
                icon =
                    "⌂",
                label =
                    "Home",
                selected =
                    true,
                onClick =
                    onHome
            )

            HomeFooterItem(
                icon =
                    "●",
                label =
                    "Activity",
                selected =
                    false,
                onClick =
                    onActivity
            )

            HomeFooterItem(
                icon =
                    "▥",
                label =
                    "Progress",
                selected =
                    false,
                onClick =
                    onProgress
            )

            HomeFooterItem(
                icon =
                    "◎",
                label =
                    "Goal",
                selected =
                    false,
                onClick =
                    onGoal
            )

            HomeFooterItem(
                icon =
                    "◉",
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
private fun HomeFooterItem(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource
        .collectIsPressedAsState()

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
            spring(
                dampingRatio =
                    0.82f,
                stiffness =
                    520f
            )
    )

    Column(
        modifier =
            Modifier
                .width(
                    67.dp
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
                        HomePrimary
                            .copy(
                                alpha =
                                    0.09f
                            )
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
                if (
                    selected
                ) {
                    HomePrimary
                } else {
                    Color(0xFF94A3B8)
                },
            fontSize =
                18.sp,
            fontWeight =
                FontWeight.ExtraBold
        )

        Text(
            text =
                label,
            color =
                if (
                    selected
                ) {
                    HomePrimary
                } else {
                    Color(0xFF94A3B8)
                },
            fontSize =
                9.sp,
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

private fun friendlyApiMessage(
    response: JSONObject,
    fallback: String
): String {
    val message =
        response.optString(
            "message",
            fallback
        )
            .ifBlank {
                fallback
            }

    val code =
        response.optInt(
            "http_code",
            0
        )

    if (
        code ==
        422 &&
        message.contains(
            "user",
            ignoreCase =
                true
        )
    ) {
        return "The saved login is being repaired. Refresh once more."
    }

    return if (
        code >
        0
    ) {
        "$message\nHTTP: $code"
    } else {
        message
    }
}

private fun formatHomeDuration(
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

    return if (
        hours >
        0
    ) {
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
