package com.example.movemate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StationaryWorkoutAnimation(
    activityName: String,
    isRunning: Boolean,
    isFinished: Boolean,
    elapsedSeconds: Int,
    targetSeconds: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val accessibilitySettings =
        LocalMoveMateAccessibility.current

    val animatedTime = remember(activityName) {
        Animatable(0f)
    }

    LaunchedEffect(
        elapsedSeconds,
        isRunning,
        isFinished
    ) {
        when {
            elapsedSeconds <= 0 -> {
                animatedTime.snapTo(0f)
            }

            isFinished || !isRunning -> {
                /*
                 * stop() freezes the animation at the exact displayed pose.
                 */
                animatedTime.stop()
            }

            else -> {
                if (
                    accessibilitySettings
                        .reduceMotion
                ) {
                    animatedTime.snapTo(
                        elapsedSeconds.toFloat()
                    )
                } else {
                    animatedTime.animateTo(
                        targetValue =
                            elapsedSeconds.toFloat(),
                        animationSpec = tween(
                            durationMillis = 950,
                            easing = LinearEasing
                        )
                    )
                }
            }
        }
    }

    val targetProgress =
        if (targetSeconds > 0) {
            (elapsedSeconds.toFloat() / targetSeconds.toFloat())
                .coerceIn(0f, 1f)
        } else {
            0f
        }

    val title =
        when (activityName.lowercase()) {
            "yoga" -> "Yoga Breathing Flow"
            "weightlifting" -> "Strength Rep Motion"
            "hiit" -> "HIIT Interval Motion"
            else -> "$activityName Motion"
        }

    val status =
        when {
            isFinished ->
                "Workout stopped • animation is fixed"

            !isRunning && elapsedSeconds > 0 ->
                "Paused • animation is fixed"

            isRunning &&
                    accessibilitySettings.reduceMotion ->
                "Reduced motion • pose updates without smooth movement"

            isRunning ->
                "Animation follows your workout timer"

            else ->
                "Press Start to begin the movement"
        }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF071426),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 17.sp
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val time = animatedTime.value
            val center = Offset(
                x = size.width / 2f,
                y = size.height / 2f
            )

            repeat(5) { index ->
                val fraction = (index + 1) / 6f

                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(
                        0f,
                        size.height * fraction
                    ),
                    end = Offset(
                        size.width,
                        size.height * fraction
                    ),
                    strokeWidth = 1.5f
                )
            }

            when (activityName.lowercase()) {
                "yoga" -> {
                    val breath =
                        ((sin(time * PI / 4.0) + 1.0) / 2.0)
                            .toFloat()

                    val radius =
                        50f + breath * 34f

                    drawCircle(
                        color =
                            accentColor.copy(alpha = 0.18f),
                        radius = radius + 22f,
                        center = center
                    )

                    drawCircle(
                        color = accentColor,
                        radius = radius,
                        center = center
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 13f,
                        center = Offset(
                            center.x,
                            center.y - 56f
                        )
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            center.y - 42f
                        ),
                        end = Offset(
                            center.x,
                            center.y + 28f
                        ),
                        strokeWidth = 9f,
                        cap = StrokeCap.Round
                    )

                    val armSpread =
                        46f + breath * 26f

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            center.y - 12f
                        ),
                        end = Offset(
                            center.x - armSpread,
                            center.y + 8f
                        ),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            center.y - 12f
                        ),
                        end = Offset(
                            center.x + armSpread,
                            center.y + 8f
                        ),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            center.y + 26f
                        ),
                        end = Offset(
                            center.x - 46f,
                            center.y + 72f
                        ),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            center.y + 26f
                        ),
                        end = Offset(
                            center.x + 46f,
                            center.y + 72f
                        ),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }

                "weightlifting" -> {
                    val liftPhase =
                        ((sin(time * PI / 2.0) + 1.0) / 2.0)
                            .toFloat()

                    val barY =
                        center.y + 50f - liftPhase * 100f

                    drawCircle(
                        color = Color.White,
                        radius = 15f,
                        center = Offset(
                            center.x,
                            center.y - 42f
                        )
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            center.y - 26f
                        ),
                        end = Offset(
                            center.x,
                            center.y + 42f
                        ),
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            center.y + 38f
                        ),
                        end = Offset(
                            center.x - 38f,
                            center.y + 88f
                        ),
                        strokeWidth = 9f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            center.y + 38f
                        ),
                        end = Offset(
                            center.x + 38f,
                            center.y + 88f
                        ),
                        strokeWidth = 9f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = accentColor,
                        start = Offset(
                            center.x - 92f,
                            barY
                        ),
                        end = Offset(
                            center.x + 92f,
                            barY
                        ),
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )

                    listOf(-104f, -92f, 92f, 104f)
                        .forEach { offsetX ->
                            drawLine(
                                color = Color(0xFFFFC96B),
                                start = Offset(
                                    center.x + offsetX,
                                    barY - 22f
                                ),
                                end = Offset(
                                    center.x + offsetX,
                                    barY + 22f
                                ),
                                strokeWidth = 12f,
                                cap = StrokeCap.Round
                            )
                        }

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x - 20f,
                            center.y - 14f
                        ),
                        end = Offset(
                            center.x - 54f,
                            barY
                        ),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x + 20f,
                            center.y - 14f
                        ),
                        end = Offset(
                            center.x + 54f,
                            barY
                        ),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }

                "hiit" -> {
                    val jump =
                        abs(
                            sin(time * PI)
                        ).toFloat()

                    val athleteY =
                        center.y + 40f - jump * 72f

                    drawCircle(
                        color =
                            accentColor.copy(alpha = 0.20f),
                        radius = 74f + jump * 18f,
                        center = Offset(
                            center.x,
                            athleteY
                        )
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 15f,
                        center = Offset(
                            center.x,
                            athleteY - 48f
                        )
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            athleteY - 32f
                        ),
                        end = Offset(
                            center.x,
                            athleteY + 28f
                        ),
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )

                    val armWidth =
                        34f + jump * 34f

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            athleteY - 12f
                        ),
                        end = Offset(
                            center.x - armWidth,
                            athleteY - 44f
                        ),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            athleteY - 12f
                        ),
                        end = Offset(
                            center.x + armWidth,
                            athleteY - 44f
                        ),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            athleteY + 25f
                        ),
                        end = Offset(
                            center.x - armWidth,
                            athleteY + 72f
                        ),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = Color.White,
                        start = Offset(
                            center.x,
                            athleteY + 25f
                        ),
                        end = Offset(
                            center.x + armWidth,
                            athleteY + 72f
                        ),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }

                else -> {
                    val pulse =
                        ((cos(time * PI / 2.0) + 1.0) / 2.0)
                            .toFloat()

                    repeat(3) { index ->
                        drawCircle(
                            color =
                                accentColor.copy(
                                    alpha =
                                        0.20f - index * 0.04f
                                ),
                            radius =
                                42f +
                                        index * 28f +
                                        pulse * 18f,
                            center = center,
                            style = Stroke(
                                width = 8f
                            )
                        )
                    }

                    drawCircle(
                        color = accentColor,
                        radius = 42f,
                        center = center
                    )
                }
            }

            drawLine(
                color = Color.White.copy(alpha = 0.18f),
                start = Offset(
                    size.width * 0.08f,
                    size.height - 18f
                ),
                end = Offset(
                    size.width * 0.92f,
                    size.height - 18f
                ),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )

            drawLine(
                color = accentColor,
                start = Offset(
                    size.width * 0.08f,
                    size.height - 18f
                ),
                end = Offset(
                    size.width *
                            (0.08f + 0.84f * targetProgress),
                    size.height - 18f
                ),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }

        Text(
            text = status,
            color = Color(0xFFBFDBFE),
            fontSize = 12.sp
        )
    }
}
