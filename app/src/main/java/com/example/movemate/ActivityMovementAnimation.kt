package com.example.movemate

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Activity visual for non-route workouts.
 *
 * The progress bar follows elapsed workout time so it works on both a physical
 * phone and the Android emulator.
 *
 * The emoji movement can still react to accelerometer input when available.
 */
@Composable
fun ActivityMovementAnimation(
    activityName: String,
    activityEmoji: String,
    levelName: String,
    isRunning: Boolean,
    isFinished: Boolean,
    elapsedSeconds: Int,
    targetSeconds: Int,
    accentColor: Color,
    onMotionScoreChanged: (Double) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context =
        LocalContext.current

    val sensorManager =
        remember(
            context
        ) {
            context.getSystemService(
                Context.SENSOR_SERVICE
            ) as SensorManager
        }

    val accelerometer =
        remember(
            sensorManager
        ) {
            sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            )
        }

    val latestIsRunning =
        rememberUpdatedState(
            isRunning
        )

    val latestIsFinished =
        rememberUpdatedState(
            isFinished
        )

    val latestMotionCallback =
        rememberUpdatedState(
            onMotionScoreChanged
        )

    var sensedMotionScore by remember(
        activityName,
        levelName
    ) {
        mutableStateOf(
            0f
        )
    }

    var sensorAvailable by remember(
        accelerometer
    ) {
        mutableStateOf(
            accelerometer !=
                    null
        )
    }

    val sensorListener =
        remember(
            activityName,
            levelName
        ) {
            object :
                SensorEventListener {

                override fun onSensorChanged(
                    event: SensorEvent
                ) {
                    if (
                        !latestIsRunning.value ||
                        latestIsFinished.value
                    ) {
                        return
                    }

                    val x =
                        event.values
                            .getOrElse(
                                0
                            ) {
                                0f
                            }

                    val y =
                        event.values
                            .getOrElse(
                                1
                            ) {
                                0f
                            }

                    val z =
                        event.values
                            .getOrElse(
                                2
                            ) {
                                0f
                            }

                    val totalAcceleration =
                        sqrt(
                            x *
                                    x +
                                    y *
                                    y +
                                    z *
                                    z
                        )

                    val linearAcceleration =
                        abs(
                            totalAcceleration -
                                    SensorManager
                                        .GRAVITY_EARTH
                        )

                    val sensitivity =
                        motionSensitivityFor(
                            activityName =
                                activityName,
                            levelName =
                                levelName
                        )

                    val instantScore =
                        (
                                linearAcceleration /
                                        sensitivity *
                                        100f
                                )
                            .coerceIn(
                                0f,
                                100f
                            )

                    sensedMotionScore =
                        sensedMotionScore *
                                0.82f +
                                instantScore *
                                0.18f
                }

                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int
                ) {
                }
            }
        }

    DisposableEffect(
        accelerometer,
        isRunning,
        isFinished
    ) {
        if (
            accelerometer !=
            null &&
            isRunning &&
            !isFinished
        ) {
            sensorAvailable =
                sensorManager
                    .registerListener(
                        sensorListener,
                        accelerometer,
                        SensorManager
                            .SENSOR_DELAY_GAME
                    )
        }

        onDispose {
            sensorManager
                .unregisterListener(
                    sensorListener
                )
        }
    }

    /*
     * Gradually return the sensor animation to its resting position.
     */
    LaunchedEffect(
        isRunning,
        isFinished
    ) {
        while (
            isRunning &&
            !isFinished
        ) {
            delay(
                120L
            )

            sensedMotionScore =
                (
                        sensedMotionScore *
                                0.94f
                        )
                    .coerceAtLeast(
                        0f
                    )
        }
    }

    LaunchedEffect(
        sensedMotionScore
    ) {
        latestMotionCallback
            .value(
                sensedMotionScore
                    .toDouble()
            )
    }

    val smoothMotionScore by
    animateFloatAsState(
        targetValue =
            sensedMotionScore,
        animationSpec =
            tween(
                durationMillis =
                    320
            ),
        label =
            "smooth_motion_score"
    )

    /*
     * This is the real workout progress displayed by the bar.
     *
     * Example:
     * 11 seconds / 60 seconds = 18%.
     */
    val workoutProgress =
        if (
            targetSeconds >
            0
        ) {
            (
                    elapsedSeconds
                        .coerceAtLeast(
                            0
                        )
                        .toFloat() /
                            targetSeconds
                                .toFloat()
                    )
                .coerceIn(
                    0f,
                    1f
                )
        } else {
            0f
        }

    val animatedWorkoutProgress by
    animateFloatAsState(
        targetValue =
            workoutProgress,
        animationSpec =
            tween(
                durationMillis =
                    450
            ),
        label =
            "stationary_workout_progress"
    )

    val workoutProgressPercent =
        (
                animatedWorkoutProgress *
                        100f
                )
            .roundToInt()
            .coerceIn(
                0,
                100
            )

    val infiniteTransition =
        rememberInfiniteTransition(
            label =
                "activity_motion_loop"
        )

    val loop by
    infiniteTransition
        .animateFloat(
            initialValue =
                -1f,
            targetValue =
                1f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                animationDurationFor(
                                    activityName,
                                    levelName
                                )
                        ),
                    repeatMode =
                        RepeatMode.Reverse
                ),
            label =
                "activity_motion_phase"
        )

    val movementFactor =
        if (
            isRunning &&
            !isFinished
        ) {
            /*
             * Keep a visible animation on an emulator while allowing physical
             * phone movement to increase its amplitude.
             */
            0.25f +
                    smoothMotionScore /
                    100f *
                    0.75f
        } else {
            0f
        }

    val visual =
        movementVisualFor(
            activityName
        )

    val intervalText =
        workoutIntervalText(
            activityName =
                activityName,
            levelName =
                levelName,
            elapsedSeconds =
                elapsedSeconds
        )

    Card(
        modifier =
            modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                26.dp
            ),
        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        Color.White
                ),
        elevation =
            CardDefaults
                .cardElevation(
                    5.dp
                )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    18.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {
            Text(
                text =
                    "${visual.title} • $levelName",
                color =
                    Color(0xFF0F172A),
                fontSize =
                    18.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        205.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            22.dp
                        )
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                accentColor.copy(
                                    alpha =
                                        0.20f
                                ),
                                Color(0xFFF8FAFC)
                            )
                        )
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(
                            136.dp
                        )
                        .background(
                            color =
                                Color.White.copy(
                                    alpha =
                                        0.86f
                                ),
                            shape =
                                CircleShape
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            if (
                                activityEmoji
                                    .isBlank()
                            ) {
                                visual.emoji
                            } else {
                                activityEmoji
                            },
                        fontSize =
                            72.sp,
                        modifier =
                            Modifier.graphicsLayer {
                                val amplitude =
                                    movementFactor

                                translationX =
                                    visual
                                        .horizontalPixels *
                                            loop *
                                            amplitude

                                translationY =
                                    visual
                                        .verticalPixels *
                                            loop *
                                            amplitude

                                rotationZ =
                                    visual
                                        .rotationDegrees *
                                            loop *
                                            amplitude

                                val scaleAmount =
                                    visual
                                        .scaleAmount *
                                            abs(
                                                loop
                                            ) *
                                            amplitude

                                scaleX =
                                    1f +
                                            scaleAmount

                                scaleY =
                                    1f +
                                            scaleAmount
                            }
                    )
                }
            }

            Text(
                text =
                    when {
                        isFinished ->
                            "Workout finished"

                        !isRunning &&
                                elapsedSeconds >
                                0 ->
                            "Workout paused"

                        isRunning ->
                            intervalText

                        else ->
                            "Press Start to begin"
                    },
                color =
                    accentColor,
                fontWeight =
                    FontWeight.Bold,
                textAlign =
                    TextAlign.Center
            )

            /*
             * Timer-based progress: this no longer stays at 0% on an emulator.
             */
            LinearProgressIndicator(
                progress =
                    animatedWorkoutProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        10.dp
                    ),
                color =
                    accentColor,
                trackColor =
                    Color(0xFFE2E8F0)
            )

            Text(
                text =
                    "Workout progress: $workoutProgressPercent%",
                color =
                    Color(0xFF334155),
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    if (
                        sensorAvailable
                    ) {
                        "Progress follows the workout timer. " +
                                "The animation also responds to phone movement."
                    } else {
                        "Progress follows the workout timer. " +
                                "The emulator animation runs without a motion sensor."
                    },
                color =
                    Color(0xFF64748B),
                fontSize =
                    11.sp,
                textAlign =
                    TextAlign.Center
            )

            Text(
                text =
                    "${formatWorkoutTime(elapsedSeconds)} / " +
                            formatWorkoutTime(
                                targetSeconds
                            ),
                color =
                    Color(0xFF64748B),
                fontSize =
                    12.sp
            )
        }
    }
}

private data class MovementVisual(
    val title: String,
    val metricTitle: String,
    val emoji: String,
    val horizontalPixels: Float,
    val verticalPixels: Float,
    val rotationDegrees: Float,
    val scaleAmount: Float
)

private fun movementVisualFor(
    activityName: String
): MovementVisual {
    val name =
        activityName
            .lowercase(
                Locale.US
            )

    return when {
        "yoga" in
                name ->
            MovementVisual(
                title =
                    "Breathing & stability",
                metricTitle =
                    "Stability motion",
                emoji =
                    "🧘",
                horizontalPixels =
                    0f,
                verticalPixels =
                    5f,
                rotationDegrees =
                    2f,
                scaleAmount =
                    0.08f
            )

        "swim" in
                name ->
            MovementVisual(
                title =
                    "Swimming stroke motion",
                metricTitle =
                    "Stroke motion",
                emoji =
                    "🏊",
                horizontalPixels =
                    50f,
                verticalPixels =
                    8f,
                rotationDegrees =
                    4f,
                scaleAmount =
                    0.02f
            )

        "hiit" in
                name ->
            MovementVisual(
                title =
                    "HIIT work interval",
                metricTitle =
                    "Active motion",
                emoji =
                    "🔥",
                horizontalPixels =
                    8f,
                verticalPixels =
                    42f,
                rotationDegrees =
                    8f,
                scaleAmount =
                    0.12f
            )

        "lift" in
                name ||
                "weight" in
                name ->
            MovementVisual(
                title =
                    "Controlled lift motion",
                metricTitle =
                    "Lift motion",
                emoji =
                    "🏋️",
                horizontalPixels =
                    0f,
                verticalPixels =
                    30f,
                rotationDegrees =
                    2f,
                scaleAmount =
                    0.05f
            )

        "row" in
                name ->
            MovementVisual(
                title =
                    "Rowing stroke motion",
                metricTitle =
                    "Stroke motion",
                emoji =
                    "🚣",
                horizontalPixels =
                    46f,
                verticalPixels =
                    6f,
                rotationDegrees =
                    7f,
                scaleAmount =
                    0.03f
            )

        else ->
            MovementVisual(
                title =
                    "Body movement",
                metricTitle =
                    "Movement",
                emoji =
                    "🤸",
                horizontalPixels =
                    20f,
                verticalPixels =
                    20f,
                rotationDegrees =
                    8f,
                scaleAmount =
                    0.06f
            )
    }
}

private fun motionSensitivityFor(
    activityName: String,
    levelName: String
): Float {
    val activity =
        activityName
            .lowercase(
                Locale.US
            )

    val level =
        levelName
            .lowercase(
                Locale.US
            )

    val base =
        when {
            "yoga" in
                    activity ->
                0.45f

            "swim" in
                    activity ->
                1.20f

            "hiit" in
                    activity ->
                1.80f

            "lift" in
                    activity ||
                    "weight" in
                    activity ->
                1.25f

            else ->
                1.10f
        }

    return when (
        level
    ) {
        "easy",
        "basic",
        "beginner" ->
            base *
                    0.85f

        "hard",
        "advanced" ->
            base *
                    1.15f

        else ->
            base
    }
}

private fun animationDurationFor(
    activityName: String,
    levelName: String
): Int {
    val activity =
        activityName
            .lowercase(
                Locale.US
            )

    val level =
        levelName
            .lowercase(
                Locale.US
            )

    val baseDuration =
        when {
            "yoga" in
                    activity ->
                2200

            "swim" in
                    activity ->
                1100

            "hiit" in
                    activity ->
                520

            "lift" in
                    activity ||
                    "weight" in
                    activity ->
                1150

            else ->
                900
        }

    return when (
        level
    ) {
        "easy",
        "basic",
        "beginner" ->
            (
                    baseDuration *
                            1.18
                    )
                .toInt()

        "hard",
        "advanced" ->
            (
                    baseDuration *
                            0.82
                    )
                .toInt()

        else ->
            baseDuration
    }
}

private fun workoutIntervalText(
    activityName: String,
    levelName: String,
    elapsedSeconds: Int
): String {
    if (
        !activityName.contains(
            "HIIT",
            ignoreCase =
                true
        )
    ) {
        return "Workout active"
    }

    val level =
        levelName
            .lowercase(
                Locale.US
            )

    val workSeconds:
            Int

    val restSeconds:
            Int

    when (
        level
    ) {
        "easy",
        "basic",
        "beginner" -> {
            workSeconds =
                20

            restSeconds =
                20
        }

        "hard",
        "advanced" -> {
            workSeconds =
                40

            restSeconds =
                10
        }

        else -> {
            workSeconds =
                30

            restSeconds =
                15
        }
    }

    val cycleSeconds =
        workSeconds +
                restSeconds

    val position =
        elapsedSeconds
            .coerceAtLeast(
                0
            ) %
                cycleSeconds

    return if (
        position <
        workSeconds
    ) {
        "WORK • ${workSeconds - position}s remaining"
    } else {
        "RECOVER • ${cycleSeconds - position}s remaining"
    }
}