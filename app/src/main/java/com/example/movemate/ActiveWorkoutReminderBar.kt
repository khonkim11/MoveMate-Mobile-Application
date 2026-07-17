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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Place this above the app's screen content.
 *
 * It remains visible while the user is on Home, Activity, Summary, Profile,
 * or another page and opens the selected active workout when tapped.
 */
@Composable
fun ActiveWorkoutReminderBar(
    userId: Int,
    onOpenWorkout:
        (
        ActiveWorkoutState
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val active =
        ActiveWorkoutManager
            .activeForUser(
                userId
            )

    if (active.isEmpty()) {
        return
    }

    val selected =
        active.firstOrNull {
            it.isRunning
        } ?: active.first()

    val totalLiveCalories =
        active.sumOf {
            it.calories
        }

    val animatedCalories by
    animateFloatAsState(
        targetValue =
            totalLiveCalories
                .toFloat(),
        animationSpec =
            tween(
                durationMillis =
                    550
            ),
        label =
            "active_header_calories"
    )

    val transition =
        rememberInfiniteTransition(
            label =
                "active_workout_header"
        )

    val pulse by
    transition.animateFloat(
        initialValue =
            0.985f,
        targetValue =
            1.015f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            1400
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label =
            "active_header_pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal =
                    10.dp,
                vertical =
                    6.dp
            )
            .scale(
                if (
                    selected.isRunning
                ) {
                    pulse
                } else {
                    1f
                }
            )
            .clickable {
                onOpenWorkout(
                    selected
                )
            },
        shape =
            RoundedCornerShape(
                18.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.Transparent
            ),
        elevation =
            CardDefaults.cardElevation(
                6.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF0F172A),
                            selected.accentColor
                        )
                    )
                )
                .padding(
                    horizontal =
                        14.dp,
                    vertical =
                        10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    selected.activityEmoji,
                fontSize =
                    27.sp
            )

            Spacer(
                Modifier.width(
                    10.dp
                )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        2.dp
                    )
            ) {
                Text(
                    text =
                        if (
                            active.size >
                            1
                        ) {
                            "${selected.activityName} • ${active.size} active workouts"
                        } else {
                            "${selected.activityName} • ${selected.levelName}"
                        },
                    color =
                        Color.White,
                    fontWeight =
                        FontWeight.ExtraBold,
                    fontSize =
                        14.sp
                )

                Text(
                    text =
                        "${formatWorkoutTime(selected.elapsedSeconds)} • " +
                                String.format(
                                    Locale.US,
                                    "%.1f live kcal",
                                    animatedCalories
                                ),
                    color =
                        Color.White.copy(
                            alpha =
                                0.84f
                        ),
                    fontSize =
                        12.sp
                )
            }

            TextButton(
                onClick = {
                    if (
                        selected.isRunning
                    ) {
                        ActiveWorkoutManager
                            .pause(
                                selected.id
                            )
                    } else {
                        ActiveWorkoutManager
                            .start(
                                selected.id
                            )
                    }
                }
            ) {
                Text(
                    text =
                        if (
                            selected.isRunning
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

            Spacer(
                Modifier.width(
                    2.dp
                )
            )

            Text(
                text =
                    "Open ›",
                color =
                    Color.White,
                fontWeight =
                    FontWeight.ExtraBold,
                fontSize =
                    12.sp
            )
        }
    }
}