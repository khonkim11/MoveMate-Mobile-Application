package com.example.movemate

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

@Composable
fun WorkoutSessionScreen(
    session: UserSession,
    activity: WorkoutActivityOption,
    level: WorkoutLevel,
    existingWorkoutId: String? = null,
    onBack: () -> Unit,
    onViewTodaySummary: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    val context =
        LocalContext.current

    val accessibilitySettings =
        LocalMoveMateAccessibility.current

    val hapticFeedback =
        LocalHapticFeedback.current

    fun performWorkoutHaptic() {
        if (
            accessibilitySettings
                .hapticFeedback
        ) {
            hapticFeedback
                .performHapticFeedback(
                    HapticFeedbackType.LongPress
                )
        }
    }

    val workoutId =
        remember(
            session.userId,
            activity.name,
            level.name,
            existingWorkoutId
        ) {
            existingWorkoutId
                ?.takeIf {
                        candidateId ->

                    ActiveWorkoutManager
                        .find(candidateId)
                        ?.isFinished ==
                            false
                }
                ?: ActiveWorkoutManager
                    .getOrCreate(
                        userId = session.userId,
                        activity = activity,
                        level = level,
                        weightKg = session.weightKg
                    )
        }

    /*
     * SnapshotStateList observation:
     * the screen recomposes every time the manager replaces this workout.
     */
    val activeWorkout =
        ActiveWorkoutManager
            .activeWorkouts
            .firstOrNull {
                it.id ==
                        workoutId
            }

    var isSaving by remember(
        workoutId
    ) {
        mutableStateOf(
            false
        )
    }

    var message by remember(
        workoutId
    ) {
        mutableStateOf(
            ""
        )
    }

    var completion by remember(
        workoutId
    ) {
        mutableStateOf<WorkoutCompletionData?>(
            null
        )
    }

    /*
     * Prevents the one-minute completion effect from saving the same workout
     * more than once.
     */
    var automaticSaveStarted by remember(
        workoutId
    ) {
        mutableStateOf(
            false
        )
    }

    /*
     * Let the workout page finish its first composition before creating the
     * Google map. A slow map/API initialization must not block navigation.
     */
    var mapReadyToMount by remember(
        workoutId
    ) {
        mutableStateOf(
            false
        )
    }

    LaunchedEffect(
        workoutId
    ) {
        delay(
            350L
        )

        mapReadyToMount =
            true
    }

    if (
        activeWorkout ==
        null &&
        completion ==
        null
    ) {
        LaunchedEffect(
            workoutId
        ) {
            onBack()
        }

        return
    }

    val elapsedMilliseconds =
        activeWorkout
            ?.elapsedMilliseconds
            ?: 0L

    val elapsedSeconds =
        activeWorkout
            ?.elapsedSeconds
            ?: 0

    val isRunning =
        activeWorkout
            ?.isRunning
            ?: false

    val isFinished =
        activeWorkout
            ?.isFinished
            ?: false

    val calculatedCalories =
        activeWorkout
            ?.calories
            ?: 0.0

    val distanceKm =
        if (
            activity.tracksRoute
        ) {
            activeWorkout
                ?.distanceKm
                ?: 0.0
        } else {
            0.0
        }

    val recordedRoutePoints: List<LatLng> =
        activeWorkout
            ?.routePoints
            .orEmpty()

    val targetSeconds =
        level.targetMinutes
            .coerceAtLeast(
                1
            ) *
                60

    val oneMinuteSample =
        isOneMinuteSampleWorkout(
            levelName =
                level.name,
            targetSeconds =
                targetSeconds
        )

    val targetDistanceKm =
        realisticTargetDistanceKm(
            activity =
                activity,
            level =
                level
        )

    WorkoutAlertEffects(
        userId =
            session.userId,
        workoutId =
            workoutId,
        activityName =
            activity.name,
        activityEmoji =
            activity.emoji,
        levelName =
            level.name,
        targetSeconds =
            targetSeconds,
        elapsedMilliseconds =
            elapsedMilliseconds,
        distanceKm =
            distanceKm,
        calories =
            calculatedCalories,
        isRunning =
            isRunning,
        isFinished =
            isFinished
    )

    val progress =
        activeWorkout
            ?.progress
            ?: 0f

    val animatedCalories by
    animateFloatAsState(
        targetValue =
            calculatedCalories
                .toFloat(),
        animationSpec =
            tween(
                durationMillis =
                    1_300,
                easing =
                    LinearOutSlowInEasing
            ),
        label =
            "manager_live_calories"
    )

    val animatedDistanceMetres by
    animateFloatAsState(
        targetValue =
            (
                    distanceKm *
                            1_000.0
                    )
                .toFloat(),
        animationSpec =
            tween(
                durationMillis =
                    3_000,
                easing =
                    LinearEasing
            ),
        label =
            "manager_live_distance"
    )

    LaunchedEffect(
        elapsedSeconds,
        targetSeconds,
        oneMinuteSample
    ) {
        if (
            !oneMinuteSample &&
            elapsedSeconds >=
            targetSeconds &&
            targetSeconds >
            0 &&
            message.isBlank()
        ) {
            message =
                "Target reached. Finish to save your " +
                        targetAwardForLevel(
                            level.name
                        ).title +
                        " award."
        }
    }

    /*
     * One-minute Home samples complete without a Stop/Finish tap.
     *
     * ActiveWorkoutManager has already clamped time to exactly 60 seconds and
     * set isFinished=true. This effect saves that fixed snapshot, removes the
     * active entry and opens the normal award completion screen.
     */
    LaunchedEffect(
        workoutId,
        oneMinuteSample,
        isFinished
    ) {
        if (
            !oneMinuteSample ||
            !isFinished ||
            automaticSaveStarted ||
            completion !=
            null
        ) {
            return@LaunchedEffect
        }

        automaticSaveStarted =
            true

        isSaving =
            true

        message =
            "One-minute sample completed. Saving automatically..."

        val finalElapsedSeconds =
            targetSeconds

        val finalDistanceKm =
            distanceKm

        val finalCalories =
            calculatedCalories

        val finalRoutePoints =
            recordedRoutePoints

        try {
            val response =
                WorkoutApiService
                    .saveWorkout(
                        userId =
                            session.userId,
                        activityType =
                            activity.name,
                        workoutName =
                            "${activity.name} - ${level.name}",
                        durationSeconds =
                            finalElapsedSeconds,
                        targetSeconds =
                            targetSeconds,
                        distanceKm =
                            finalDistanceKm,
                        calories =
                            finalCalories,
                        intensity =
                            level.name,
                        levelName =
                            level.name,
                        notes =
                            String.format(
                                Locale.US,
                                "Automatic 1-minute sample; MET %.1f; weight %.1f kg",
                                activity
                                    .metForLevel(
                                        level
                                    ),
                                session
                                    .weightKg
                                    .takeIf {
                                        it in
                                                20.0..350.0
                                    }
                                    ?: 70.0
                            ),
                        routeJson =
                            if (
                                activity.tracksRoute
                            ) {
                                workoutSessionRoutePointsToJson(
                                    finalRoutePoints
                                )
                            } else {
                                "[]"
                            }
                    )

            if (
                response.optBoolean(
                    "success",
                    false
                )
            ) {
                /*
                 * Build the completion data before removing the manager entry.
                 * Setting completion immediately switches the UI from the
                 * saving spinner to WorkoutCompletionScreen.
                 */
                val completionData =
                    response
                        .toCompletion(
                            activity =
                                activity,
                            level =
                                level,
                            elapsedSeconds =
                                finalElapsedSeconds,
                            distanceKm =
                                finalDistanceKm,
                            calories =
                                finalCalories,
                            targetSeconds =
                                targetSeconds
                        )

                ActiveWorkoutManager
                    .remove(
                        workoutId
                    )

                completion =
                    completionData

                message =
                    ""

                launch {
                    runCatching {
                        MoveMateNotificationManager
                            .recordWorkoutCompleted(
                                context,
                                session.userId
                            )
                    }
                }
            } else {
                /*
                 * The timer and calories stay stopped at 01:00.
                 * The existing Finish button becomes the save retry.
                 */
                automaticSaveStarted =
                    false

                message =
                    response.optString(
                        "message",
                        "The sample stopped at 01:00, but automatic saving failed. Tap Finish to retry saving."
                    )
            }
        } catch (
            exception: Exception
        ) {
            automaticSaveStarted =
                false

            message =
                "The sample stopped at 01:00, but saving could not finish: " +
                        (
                                exception.message
                                    ?: exception
                                        .javaClass
                                        .simpleName
                                ) +
                        ". Tap Finish to retry."
        } finally {
            /*
             * This always runs, even if the request or JSON conversion fails.
             * The page can no longer remain permanently on the saving spinner.
             */
            isSaving =
                false
        }
    }

    /*
     * Returning Home does not stop or pause the active workout.
     */
    BackHandler {
        if (
            completion !=
            null
        ) {
            onViewTodaySummary()
        } else {
            onBack()
        }
    }

    completion?.let {
        WorkoutCompletionScreen(
            completion =
                it,
            accentColor =
                activity.accentColor,
            onViewTodaySummary =
                onViewTodaySummary
        )

        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        activity
                            .accentColor
                            .copy(
                                alpha =
                                    0.18f
                            ),
                        Color(0xFFF8FAFC),
                        Color.White
                    )
                )
            )
    ) {
        LazyColumn(
            modifier =
                Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    18.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    14.dp
                )
        ) {
            item {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick =
                            onBack
                    ) {
                        Text(
                            text =
                                if (
                                    isRunning
                                ) {
                                    "‹ Home • keep running"
                                } else {
                                    "‹ Home"
                                },
                            color =
                                Color(0xFF0F172A),
                            fontWeight =
                                FontWeight.Bold
                        )
                    }

                    Spacer(
                        Modifier.weight(
                            1f
                        )
                    )

                    Text(
                        "${activity.emoji} ${activity.name}",
                        color =
                            Color(0xFF0F172A),
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }

            item {
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
                                Color.White
                        ),
                    elevation =
                        CardDefaults.cardElevation(
                            5.dp
                        )
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                20.dp
                            ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {
                        Text(
                            "${level.name} Level",
                            color =
                                activity.accentColor,
                            fontWeight =
                                FontWeight.ExtraBold
                        )

                        Text(
                            formatWorkoutTime(
                                elapsedSeconds
                            ),
                            color =
                                Color(0xFF0F172A),
                            fontSize =
                                52.sp,
                            fontWeight =
                                FontWeight.ExtraBold
                        )

                        Text(
                            when {
                                isSaving ->
                                    "Saving workout"

                                oneMinuteSample &&
                                        isFinished ->
                                    "One-minute sample complete"

                                isRunning ->
                                    "Workout continues across MoveMate screens"

                                elapsedSeconds >
                                        0 ->
                                    "Workout paused"

                                else ->
                                    "Ready to start"
                            },
                            color =
                                if (
                                    isRunning
                                ) {
                                    activity.accentColor
                                } else {
                                    Color(0xFF64748B)
                                },
                            fontWeight =
                                FontWeight.Bold,
                            textAlign =
                                TextAlign.Center
                        )

                        LinearProgressIndicator(
                            progress =
                                progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    10.dp
                                ),
                            color =
                                activity.accentColor,
                            trackColor =
                                Color(0xFFE2E8F0)
                        )

                        Text(
                            "${formatWorkoutTime(elapsedSeconds)} / " +
                                    "${formatWorkoutTime(targetSeconds)} target",
                            color =
                                Color(0xFF64748B),
                            fontSize =
                                12.sp
                        )
                    }
                }
            }

            item {
                if (
                    activity.tracksRoute &&
                    mapReadyToMount
                ) {
                    WorkoutRouteMap(
                        userId =
                            session.userId,
                        title =
                            "Real Google GPS Map",
                        activityName =
                            activity.name,
                        expectedSpeedKmh =
                            activity
                                .speedForLevelKmh(
                                    level
                                ),
                        isRunning =
                            isRunning,
                        isFinished =
                            isSaving ||
                                    isFinished,
                        elapsedSeconds =
                            elapsedSeconds,
                        targetSeconds =
                            targetSeconds,
                        currentDistanceKm =
                            distanceKm,
                        targetDistanceKm =
                            targetDistanceKm,
                        initialRoutePoints =
                            recordedRoutePoints,
                        accentColor =
                            activity.accentColor,
                        onActualDistanceChanged = {
                                value: Double ->

                            ActiveWorkoutManager
                                .updateDistance(
                                    workoutId =
                                        workoutId,
                                    distanceKm =
                                        value
                                )
                        },
                        onRouteChanged = {
                                points: List<LatLng> ->

                            ActiveWorkoutManager
                                .updateRoute(
                                    workoutId =
                                        workoutId,
                                    routePoints =
                                        points
                                )
                        }
                    )
                } else if (
                    activity.tracksRoute
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
                                    Color.White
                            )
                    ) {
                        Row(
                            modifier =
                                Modifier.padding(
                                    18.dp
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
                                        25.dp
                                    ),
                                color =
                                    activity.accentColor,
                                strokeWidth =
                                    3.dp
                            )

                            Column {
                                Text(
                                    text =
                                        "Opening workout...",
                                    color =
                                        Color(0xFF0F172A),
                                    fontWeight =
                                        FontWeight.ExtraBold
                                )

                                Text(
                                    text =
                                        "Preparing the Google road map. Controls are still available.",
                                    color =
                                        Color(0xFF64748B),
                                    fontSize =
                                        12.sp
                                )
                            }
                        }
                    }
                } else {
                    ActivityMovementAnimation(
                        activityName =
                            activity.name,
                        activityEmoji =
                            activity.emoji,
                        levelName =
                            level.name,
                        isRunning =
                            isRunning,
                        isFinished =
                            isSaving ||
                                    isFinished,
                        elapsedSeconds =
                            elapsedSeconds,
                        targetSeconds =
                            targetSeconds,
                        accentColor =
                            activity.accentColor
                    )
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
                    WorkoutLiveMetricCard(
                        title =
                            "Calories",
                        value =
                            String.format(
                                Locale.US,
                                "%.1f kcal",
                                animatedCalories
                            ),
                        emoji =
                            "🔥",
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    WorkoutLiveMetricCard(
                        title =
                            if (
                                activity.tracksRoute
                            ) {
                                "Distance"
                            } else {
                                "Target"
                            },
                        value =
                            if (
                                activity.tracksRoute
                            ) {
                                formatDistanceMetersForWorkout(
                                    animatedDistanceMetres
                                        .toDouble() /
                                            1_000.0
                                )
                            } else {
                                "${level.targetMinutes} min"
                            },
                        emoji =
                            if (
                                activity.tracksRoute
                            ) {
                                "📍"
                            } else {
                                "🎯"
                            },
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )
                }
            }

            item {
                WorkoutLevelAwardCard(
                    levelName =
                        level.name,
                    elapsedSeconds =
                        elapsedSeconds,
                    targetSeconds =
                        targetSeconds,
                    accentColor =
                        activity.accentColor
                )
            }

            if (
                message.isNotBlank()
            ) {
                item {
                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    if (
                                        message.contains(
                                            "Target reached",
                                            ignoreCase =
                                                true
                                        )
                                    ) {
                                        Color(0xFFEAF7EE)
                                    } else {
                                        Color(0xFFFFEBEE)
                                    }
                            ),
                        shape =
                            RoundedCornerShape(
                                18.dp
                            )
                    ) {
                        Text(
                            text =
                                message,
                            color =
                                if (
                                    message.contains(
                                        "Target reached",
                                        ignoreCase =
                                            true
                                    )
                                ) {
                                    Color(0xFF15803D)
                                } else {
                                    Color(0xFFB91C1C)
                                },
                            fontWeight =
                                FontWeight.SemiBold,
                            modifier =
                                Modifier.padding(
                                    14.dp
                                )
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
                    OutlinedButton(
                        onClick = {
                            performWorkoutHaptic()

                            ActiveWorkoutManager
                                .pause(
                                    workoutId
                                )
                        },
                        enabled =
                            isRunning &&
                                    !isSaving,
                        modifier = Modifier
                            .weight(
                                1f
                            )
                            .height(
                                54.dp
                            ),
                        shape =
                            RoundedCornerShape(
                                17.dp
                            )
                    ) {
                        Text(
                            "Pause",
                            fontWeight =
                                FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            performWorkoutHaptic()

                            ActiveWorkoutManager
                                .start(
                                    workoutId
                                )

                            if (
                                elapsedSeconds <
                                targetSeconds
                            ) {
                                message =
                                    ""
                            }
                        },
                        enabled =
                            !isRunning &&
                                    !isSaving &&
                                    !isFinished,
                        modifier = Modifier
                            .weight(
                                1f
                            )
                            .height(
                                54.dp
                            ),
                        shape =
                            RoundedCornerShape(
                                17.dp
                            ),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    activity.accentColor
                            )
                    ) {
                        Text(
                            if (
                                elapsedSeconds >
                                0
                            ) {
                                "Resume"
                            } else {
                                "Start"
                            },
                            fontWeight =
                                FontWeight.ExtraBold
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        performWorkoutHaptic()

                        ActiveWorkoutManager
                            .reset(
                                workoutId
                            )

                        completion =
                            null

                        automaticSaveStarted =
                            false

                        message =
                            ""
                    },
                    enabled =
                        !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            50.dp
                        ),
                    shape =
                        RoundedCornerShape(
                            17.dp
                        )
                ) {
                    Text(
                        "Reset Workout",
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick =
                        onBack,
                    enabled =
                        !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            50.dp
                        ),
                    shape =
                        RoundedCornerShape(
                            17.dp
                        )
                ) {
                    Text(
                        text =
                            if (
                                isRunning
                            ) {
                                "Go Home — Keep This Workout Running"
                            } else {
                                "Go Home"
                            },
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }

            item {
                if (
                    isSaving
                ) {
                    Column(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color =
                                activity.accentColor
                        )

                        Text(
                            "Saving workout and updating today's calories...",
                            color =
                                Color(0xFF64748B),
                            fontSize =
                                12.sp,
                            modifier =
                                Modifier.padding(
                                    top =
                                        8.dp
                                )
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            performWorkoutHaptic()

                            when {
                                session.userId <=
                                        0 -> {
                                    message =
                                        "Your session is invalid. Please log in again."
                                }

                                elapsedSeconds <
                                        5 -> {
                                    message =
                                        "Complete at least 5 seconds before finishing."
                                }

                                else -> {
                                    ActiveWorkoutManager
                                        .pause(
                                            workoutId
                                        )

                                    isSaving =
                                        true

                                    message =
                                        ""

                                    scope.launch {
                                        val response =
                                            WorkoutApiService
                                                .saveWorkout(
                                                    userId =
                                                        session.userId,
                                                    activityType =
                                                        activity.name,
                                                    workoutName =
                                                        "${activity.name} - ${level.name}",
                                                    durationSeconds =
                                                        elapsedSeconds,
                                                    targetSeconds =
                                                        targetSeconds,
                                                    distanceKm =
                                                        distanceKm,
                                                    calories =
                                                        calculatedCalories,
                                                    intensity =
                                                        level.name,
                                                    levelName =
                                                        level.name,
                                                    notes =
                                                        String.format(
                                                            Locale.US,
                                                            "MET %.1f; weight %.1f kg",
                                                            activity
                                                                .metForLevel(
                                                                    level
                                                                ),
                                                            session
                                                                .weightKg
                                                                .takeIf {
                                                                    it in
                                                                            20.0..350.0
                                                                }
                                                                ?: 70.0
                                                        ),
                                                    routeJson =
                                                        if (
                                                            activity
                                                                .tracksRoute
                                                        ) {
                                                            workoutSessionRoutePointsToJson(
                                                                recordedRoutePoints
                                                            )
                                                        } else {
                                                            "[]"
                                                        }
                                                )

                                        isSaving =
                                            false

                                        if (
                                            response.optBoolean(
                                                "success",
                                                false
                                            )
                                        ) {
                                            val completionData =
                                                response
                                                    .toCompletion(
                                                        activity =
                                                            activity,
                                                        level =
                                                            level,
                                                        elapsedSeconds =
                                                            elapsedSeconds,
                                                        distanceKm =
                                                            distanceKm,
                                                        calories =
                                                            calculatedCalories,
                                                        targetSeconds =
                                                            targetSeconds
                                                    )

                                            /*
                                             * Saved sessions leave the active
                                             * reminder list immediately.
                                             */
                                            ActiveWorkoutManager
                                                .remove(
                                                    workoutId
                                                )

                                            completion =
                                                completionData

                                            launch {
                                                runCatching {
                                                    MoveMateNotificationManager
                                                        .recordWorkoutCompleted(
                                                            context,
                                                            session.userId
                                                        )
                                                }
                                            }
                                        } else {
                                            message =
                                                response
                                                    .optString(
                                                        "message",
                                                        "Could not save the workout."
                                                    )
                                        }
                                    }
                                }
                            }
                        },
                        enabled =
                            elapsedSeconds >=
                                    5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                58.dp
                            ),
                        shape =
                            RoundedCornerShape(
                                18.dp
                            ),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color(0xFF0F172A)
                            )
                    ) {
                        Text(
                            text =
                                if (
                                    oneMinuteSample &&
                                    isFinished
                                ) {
                                    "Retry Automatic Save"
                                } else {
                                    "Finish, Save & Get Award"
                                },
                            fontWeight =
                                FontWeight.ExtraBold
                        )
                    }
                }
            }

            item {
                Text(
                    text =
                        activity.beginnerTip,
                    color =
                        Color(0xFF475569),
                    textAlign =
                        TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom =
                                20.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun WorkoutLiveMetricCard(
    title: String,
    value: String,
    emoji: String,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(emoji, fontSize = 22.sp)
            Text(
                title,
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                value,
                color = Color(0xFF0F172A),
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun WorkoutAwardPreviewCard(
    award: WorkoutAwardTier,
    elapsedSeconds: Int,
    targetSeconds: Int,
    accentColor: Color
) {
    val ratio =
        if (targetSeconds > 0) {
            elapsedSeconds.toDouble() / targetSeconds
        } else 0.0

    val nextText = when {
        ratio < 0.50 -> "Reach 50% of the target to unlock Silver."
        ratio < 1.00 -> "Silver unlocked. Reach 100% for Gold."
        ratio < 1.30 -> "Gold unlocked. Reach 130% for Diamond."
        else -> "Diamond unlocked. Excellent work!"
    }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.10f)
        )
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(award.emoji, fontSize = 38.sp)

            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    "Live Award: ${award.title}",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    nextText,
                    color = Color(0xFF475569),
                    fontSize = 12.sp
                )
            }
        }
    }
}


private fun JSONObject.toCompletion(
    activity: WorkoutActivityOption,
    level: WorkoutLevel,
    elapsedSeconds: Int,
    distanceKm: Double,
    calories: Double,
    targetSeconds: Int
): WorkoutCompletionData {
    val workout = optJSONObject("workout") ?: JSONObject()
    val today = optJSONObject("today_summary") ?: JSONObject()

    val award = WorkoutAwardTier.fromServer(
        workout.optString(
            "award_tier",
            calculateLevelWorkoutAward(
                levelName =
                    level.name,
                durationSeconds =
                    elapsedSeconds,
                targetSeconds =
                    targetSeconds
            ).serverValue
        )
    )

    return WorkoutCompletionData(
        activityName = workout.optString(
            "activity_type",
            activity.name
        ),
        levelName = workout.optString(
            "level_name",
            level.name
        ),
        durationSeconds = workout.optInt(
            "duration_seconds",
            elapsedSeconds
        ),
        distanceKm = workout.optDouble(
            "distance_km",
            distanceKm
        ),
        calories = workout.optDouble(
            "calories",
            calories
        ),
        awardTier = award,
        todayWorkoutCount = today.optInt("total_workouts", 1),
        todayCalories = today.optDouble("total_calories", calories),
        todayDurationSeconds = today.optInt(
            "total_duration_seconds",
            elapsedSeconds
        ),
        todayDistanceKm = today.optDouble(
            "total_distance_km",
            distanceKm
        )
    )
}

@Composable
private fun WorkoutCompletionScreen(
    completion: WorkoutCompletionData,
    accentColor: Color,
    onViewTodaySummary: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.65f,
        animationSpec = tween(650),
        label = "completion_scale"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020617),
                        accentColor,
                        Color(0xFF0F172A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(26) { index ->
                val x = size.width * (index / 26f)
                val y =
                    size.height *
                            (0.10f + ((index * 37) % 80) / 100f)

                drawCircle(
                    Color.White.copy(alpha = 0.15f),
                    radius = 6f + (index % 4) * 2f,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
                .scale(scale),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(14.dp)
        ) {
            Column(
                Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    Modifier
                        .size(108.dp)
                        .background(
                            accentColor.copy(alpha = 0.14f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        completion.awardTier.emoji,
                        fontSize = 58.sp
                    )
                }

                Text(
                    "Workout Complete!",
                    color = Color(0xFF0F172A),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    "${completion.awardTier.title} Award",
                    color = accentColor,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    completion.awardTier.message,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CompletionMetric(
                        "Time",
                        formatWorkoutTime(completion.durationSeconds),
                        Modifier.weight(1f)
                    )
                    CompletionMetric(
                        "Calories",
                        "${completion.calories.workoutFormatOneDecimal()} kcal",
                        Modifier.weight(1f)
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CompletionMetric(
                        "Distance",
                        formatDistanceMetersForWorkout(
                            completion.distanceKm
                        ),
                        Modifier.weight(1f)
                    )
                    CompletionMetric(
                        "Level",
                        completion.levelName,
                        Modifier.weight(1f)
                    )
                }

                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            "Today's Updated Total",
                            color = Color(0xFF172554),
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "${completion.todayWorkoutCount} workouts • " +
                                    "${completion.todayCalories.workoutFormatOneDecimal()} kcal",
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${formatWorkoutTime(completion.todayDurationSeconds)} • " +
                                    formatDistanceMetersForWorkout(
                                        completion.todayDistanceKm
                                    ),
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    }
                }

                Button(
                    onClick = onViewTodaySummary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(
                        "View Today's Summary",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionMetric(
    title: String,
    value: String,
    modifier: Modifier
) {
    Card(
        modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
    ) {
        Column(
            Modifier.padding(13.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
            Text(
                value,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}


private fun formatDistanceMetersForWorkout(
    distanceKm: Double
): String {
    val metres =
        distanceKm
            .coerceAtLeast(
                0.0
            ) *
                1000.0

    return if (
        metres <
        1000.0
    ) {
        String.format(
            Locale.US,
            "%.1f m",
            metres
        )
    } else {
        String.format(
            Locale.US,
            "%.2f km",
            distanceKm
        )
    }
}

private fun workoutSessionRoutePointsToJson(
    routePoints: List<LatLng>
): String {
    if (
        routePoints.isEmpty()
    ) {
        return "[]"
    }

    val jsonArray =
        JSONArray()

    routePoints.forEach {
            point ->

        if (
            point.latitude.isFinite() &&
            point.longitude.isFinite()
        ) {
            jsonArray.put(
                JSONObject()
                    .put(
                        "latitude",
                        point.latitude
                    )
                    .put(
                        "longitude",
                        point.longitude
                    )
            )
        }
    }

    return jsonArray.toString()
}

