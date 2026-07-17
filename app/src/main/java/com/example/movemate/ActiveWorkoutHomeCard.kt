package com.example.movemate

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Returns unfinished workouts belonging to the current user.
 *
 * When the session user ID is temporarily unavailable, it falls back to the
 * active workouts in the current app process. Logout must call
 * ActiveWorkoutManager.clearUser(oldUserId).
 */
fun activeWorkoutsForHome(
    sessionUserId: Int
): List<ActiveWorkoutState> {
    val unfinished =
        ActiveWorkoutManager
            .activeWorkouts
            .filter {
                !it.isFinished
            }

    if (
        sessionUserId >
        0
    ) {
        val matchingUser =
            unfinished.filter {
                it.userId ==
                        sessionUserId
            }

        if (
            matchingUser.isNotEmpty()
        ) {
            return matchingUser
        }
    }

    return unfinished
}

/**
 * Designed to replace the red HomeMessageCard area.
 *
 * It shows a workout that continued running after the user pressed Home
 * without stopping or finishing the session.
 */
@Composable
fun ActiveWorkoutHomeCard(
    workout: ActiveWorkoutState,
    activeWorkoutCount: Int,
    onContinue: (
        ActiveWorkoutState
    ) -> Unit,
    onStartAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedCalories by
    animateFloatAsState(
        targetValue =
            workout.calories
                .toFloat(),
        animationSpec =
            tween(
                durationMillis =
                    600
            ),
        label =
            "home_active_calories"
    )

    val animatedProgress by
    animateFloatAsState(
        targetValue =
            workout.progress,
        animationSpec =
            tween(
                durationMillis =
                    500
            ),
        label =
            "home_active_progress"
    )

    val infiniteTransition =
        rememberInfiniteTransition(
            label =
                "home_active_workout_pulse"
        )

    val pulse by
    infiniteTransition
        .animateFloat(
            initialValue =
                0.96f,
            targetValue =
                1.05f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                900
                        ),
                    repeatMode =
                        RepeatMode.Reverse
                ),
            label =
                "home_active_status_pulse"
        )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onContinue(
                    workout
                )
            },
        shape =
            RoundedCornerShape(
                26.dp
            ),
        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        Color.Transparent
                ),
        elevation =
            CardDefaults
                .cardElevation(
                    defaultElevation =
                        8.dp
                )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF08162F),
                            workout
                                .accentColor,
                            workout
                                .accentColor
                                .copy(
                                    alpha =
                                        0.78f
                                )
                        )
                    )
                )
                .padding(
                    18.dp
                )
        ) {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        13.dp
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
                                13.dp
                            )
                            .scale(
                                if (
                                    workout.isRunning
                                ) {
                                    pulse
                                } else {
                                    1f
                                }
                            )
                            .background(
                                color =
                                    if (
                                        workout.isRunning
                                    ) {
                                        Color(0xFF4ADE80)
                                    } else {
                                        Color(0xFFFBBF24)
                                    },
                                shape =
                                    CircleShape
                            )
                    )

                    Spacer(
                        Modifier.width(
                            8.dp
                        )
                    )

                    Text(
                        text =
                            if (
                                workout.isRunning
                            ) {
                                "WORKOUT STILL RUNNING"
                            } else {
                                "WORKOUT PAUSED"
                            },
                        color =
                            Color.White,
                        fontSize =
                            12.sp,
                        fontWeight =
                            FontWeight.ExtraBold,
                        letterSpacing =
                            0.8.sp
                    )

                    Spacer(
                        Modifier.weight(
                            1f
                        )
                    )

                    if (
                        activeWorkoutCount >
                        1
                    ) {
                        Text(
                            text =
                                "$activeWorkoutCount active",
                            color =
                                Color.White,
                            fontSize =
                                11.sp,
                            fontWeight =
                                FontWeight.Bold,
                            modifier =
                                Modifier
                                    .background(
                                        color =
                                            Color.White
                                                .copy(
                                                    alpha =
                                                        0.18f
                                                ),
                                        shape =
                                            RoundedCornerShape(
                                                50.dp
                                            )
                                    )
                                    .padding(
                                        horizontal =
                                            9.dp,
                                        vertical =
                                            5.dp
                                    )
                        )
                    }
                }

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(
                                58.dp
                            )
                            .background(
                                color =
                                    Color.White
                                        .copy(
                                            alpha =
                                                0.18f
                                        ),
                                shape =
                                    RoundedCornerShape(
                                        18.dp
                                    )
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text =
                                workout
                                    .activityEmoji,
                            fontSize =
                                31.sp
                        )
                    }

                    Spacer(
                        Modifier.width(
                            12.dp
                        )
                    )

                    Column(
                        modifier =
                            Modifier.weight(
                                1f
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                3.dp
                            )
                    ) {
                        Text(
                            text =
                                workout
                                    .activityName,
                            color =
                                Color.White,
                            fontSize =
                                22.sp,
                            fontWeight =
                                FontWeight.ExtraBold,
                            maxLines =
                                1,
                            overflow =
                                TextOverflow.Ellipsis
                        )

                        Text(
                            text =
                                "${workout.levelName} level • " +
                                        if (
                                            workout.isRunning
                                        ) {
                                            "Continuing in background"
                                        } else {
                                            "Ready to resume"
                                        },
                            color =
                                Color.White
                                    .copy(
                                        alpha =
                                            0.84f
                                    ),
                            fontSize =
                                12.sp
                        )
                    }

                    Text(
                        text =
                            formatWorkoutTime(
                                workout
                                    .elapsedSeconds
                            ),
                        color =
                            Color.White,
                        fontSize =
                            22.sp,
                        fontWeight =
                            FontWeight.ExtraBold
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
                    ActiveWorkoutMiniMetric(
                        label =
                            "Live calories",
                        value =
                            String.format(
                                Locale.US,
                                "%.1f kcal",
                                animatedCalories
                            ),
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    ActiveWorkoutMiniMetric(
                        label =
                            if (
                                workout.tracksRoute
                            ) {
                                "Distance"
                            } else {
                                "Progress"
                            },
                        value =
                            if (
                                workout.tracksRoute
                            ) {
                                formatActiveWorkoutDistance(
                                    workout
                                        .distanceKm
                                )
                            } else {
                                String.format(
                                    Locale.US,
                                    "%.0f%%",
                                    animatedProgress *
                                            100f
                                )
                            },
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )
                }

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            6.dp
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                9.dp
                            )
                            .background(
                                color =
                                    Color.White
                                        .copy(
                                            alpha =
                                                0.22f
                                        ),
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
                                        animatedProgress
                                            .coerceIn(
                                                0f,
                                                1f
                                            )
                                )
                                .height(
                                    9.dp
                                )
                                .background(
                                    color =
                                        Color(0xFFFFD166),
                                    shape =
                                        RoundedCornerShape(
                                            50.dp
                                        )
                                )
                        )
                    }

                    Text(
                        text =
                            "${formatWorkoutTime(workout.elapsedSeconds)} / " +
                                    "${formatWorkoutTime(workout.targetSeconds)} target",
                        color =
                            Color.White
                                .copy(
                                    alpha =
                                        0.78f
                                ),
                        fontSize =
                            11.sp
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
                        onClick = {
                            onContinue(
                                workout
                            )
                        },
                        modifier = Modifier
                            .weight(
                                1f
                            )
                            .height(
                                48.dp
                            ),
                        shape =
                            RoundedCornerShape(
                                15.dp
                            ),
                        colors =
                            ButtonDefaults
                                .buttonColors(
                                    containerColor =
                                        Color.White
                                )
                    ) {
                        Text(
                            text =
                                "Continue Workout",
                            color =
                                Color(0xFF10234A),
                            fontWeight =
                                FontWeight.ExtraBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (
                                workout.isRunning
                            ) {
                                ActiveWorkoutManager
                                    .pause(
                                        workout.id
                                    )
                            } else {
                                ActiveWorkoutManager
                                    .start(
                                        workout.id
                                    )
                            }
                        },
                        modifier =
                            Modifier.height(
                                48.dp
                            ),
                        shape =
                            RoundedCornerShape(
                                15.dp
                            )
                    ) {
                        Text(
                            text =
                                if (
                                    workout.isRunning
                                ) {
                                    "Pause"
                                } else {
                                    "Resume"
                                },
                            color =
                                Color.White,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }

                Text(
                    text =
                        "Start another activity without deleting this session",
                    color =
                        Color.White
                            .copy(
                                alpha =
                                    0.82f
                            ),
                    fontSize =
                        12.sp,
                    fontWeight =
                        FontWeight.SemiBold,
                    modifier =
                        Modifier.clickable(
                            onClick =
                                onStartAnother
                        )
                )
            }
        }
    }
}

@Composable
private fun ActiveWorkoutMiniMetric(
    label: String,
    value: String,
    modifier: Modifier
) {
    Column(
        modifier =
            modifier
                .background(
                    color =
                        Color.White
                            .copy(
                                alpha =
                                    0.15f
                            ),
                    shape =
                        RoundedCornerShape(
                            15.dp
                        )
                )
                .padding(
                    horizontal =
                        12.dp,
                    vertical =
                        10.dp
                ),
        verticalArrangement =
            Arrangement.spacedBy(
                3.dp
            )
    ) {
        Text(
            text =
                label,
            color =
                Color.White
                    .copy(
                        alpha =
                            0.75f
                    ),
            fontSize =
                10.sp,
            fontWeight =
                FontWeight.Bold
        )

        Text(
            text =
                value,
            color =
                Color.White,
            fontSize =
                16.sp,
            fontWeight =
                FontWeight.ExtraBold
        )
    }
}

private fun formatActiveWorkoutDistance(
    distanceKm: Double
): String {
    return if (
        distanceKm <
        1.0
    ) {
        String.format(
            Locale.US,
            "%.1f m",
            distanceKm
                .coerceAtLeast(
                    0.0
                ) *
                    1000.0
        )
    } else {
        String.format(
            Locale.US,
            "%.2f km",
            distanceKm
        )
    }
}
