package com.example.movemate

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MoveMateAuthBackground() {
    val transition =
        rememberInfiniteTransition(
            label =
                "auth_background"
        )

    val firstMove by
    transition.animateFloat(
        initialValue =
            0f,
        targetValue =
            1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            6_500
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label =
            "auth_first_move"
    )

    val secondMove by
    transition.animateFloat(
        initialValue =
            1f,
        targetValue =
            0f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            8_200
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label =
            "auth_second_move"
    )

    Box(
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    colors =
                        listOf(
                            Color(0xFF07132E),
                            Color(0xFF172554),
                            Color(0xFF1D4ED8),
                            Color(0xFF38BDF8)
                        )
                )
            )
    ) {
        Canvas(
            modifier =
                Modifier.matchParentSize()
        ) {
            drawCircle(
                color =
                    Color.White.copy(
                        alpha =
                            0.11f
                    ),
                radius =
                    size.minDimension *
                            0.30f,
                center =
                    Offset(
                        x =
                            size.width *
                                    (
                                            0.14f +
                                                    firstMove *
                                                    0.17f
                                            ),
                        y =
                            size.height *
                                    0.13f
                    )
            )

            drawCircle(
                color =
                    Color(0xFF93C5FD)
                        .copy(
                            alpha =
                                0.17f
                        ),
                radius =
                    size.minDimension *
                            0.38f,
                center =
                    Offset(
                        x =
                            size.width *
                                    (
                                            0.82f -
                                                    secondMove *
                                                    0.14f
                                            ),
                        y =
                            size.height *
                                    0.76f
                    )
            )

            drawCircle(
                color =
                    Color(0xFFC4B5FD)
                        .copy(
                            alpha =
                                0.12f
                        ),
                radius =
                    size.minDimension *
                            0.18f,
                center =
                    Offset(
                        x =
                            size.width *
                                    0.85f,
                        y =
                            size.height *
                                    (
                                            0.23f +
                                                    firstMove *
                                                    0.10f
                                            )
                    )
            )
        }
    }
}

@Composable
fun MoveMatePasswordEyeToggle(
    visible: Boolean,
    enabled: Boolean =
        true,
    onClick: () -> Unit
) {
    val scale by
    animateFloatAsState(
        targetValue =
            if (
                visible
            ) {
                1.10f
            } else {
                1f
            },
        animationSpec =
            tween(
                durationMillis =
                    170
            ),
        label =
            "password_eye_scale"
    )

    val rotation by
    animateFloatAsState(
        targetValue =
            if (
                visible
            ) {
                0f
            } else {
                -8f
            },
        animationSpec =
            tween(
                durationMillis =
                    170
            ),
        label =
            "password_eye_rotation"
    )

    IconButton(
        enabled =
            enabled,
        onClick =
            onClick,
        modifier =
            Modifier.graphicsLayer {
                scaleX =
                    scale

                scaleY =
                    scale

                rotationZ =
                    rotation
            }
    ) {
        Icon(
            painter =
                painterResource(
                    id =
                        if (
                            visible
                        ) {
                            /*
                             * Password is visible; tapping this icon hides it.
                             */
                            R.drawable.ic_eye_off
                        } else {
                            /*
                             * Password is hidden; tapping this icon reveals it.
                             */
                            R.drawable.ic_eye
                        }
                ),
            contentDescription =
                if (
                    visible
                ) {
                    "Hide password"
                } else {
                    "Show password"
                },
            tint =
                Color.Unspecified,
            modifier =
                Modifier.size(
                    25.dp
                )
        )
    }
}

@Composable
fun MoveMateAuthLeadingIcon(
    symbol: String,
    backgroundColor: Color =
        Color(0xFFEFF6FF)
) {
    Box(
        modifier = Modifier
            .size(
                34.dp
            )
            .background(
                color =
                    backgroundColor,
                shape =
                    CircleShape
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                symbol,
            fontSize =
                15.sp
        )
    }
}