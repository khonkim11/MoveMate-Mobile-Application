package com.example.movemate

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StartScreen(
    onSignUp: () -> Unit,
    onLogin: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        StartAnimatedBackground()

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MoveMate",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Track workouts. Build goals. See progress.",
                color = Color(0xFFE0EAFF),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(42.dp))

            StartPrimaryButton(
                text = "Create Account",
                onClick = onSignUp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Already have an account? Login",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun StartAnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "start_background")

    val moveOne by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 110f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "move_one"
    )

    val moveTwo by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(6500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "move_two"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF020617),
                        Color(0xFF172554),
                        Color(0xFF2563EB),
                        Color(0xFF38BDF8)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = 180f * pulse,
                center = Offset(
                    x = size.width - 90f + moveOne,
                    y = 150f
                )
            )

            drawCircle(
                color = Color(0xFFFFC96B).copy(alpha = 0.24f),
                radius = 250f,
                center = Offset(
                    x = -80f + moveTwo,
                    y = size.height * 0.38f
                )
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = 280f,
                center = Offset(
                    x = size.width * 0.78f - moveTwo,
                    y = size.height - 130f
                )
            )

            drawCircle(
                color = Color(0xFF93C5FD).copy(alpha = 0.18f),
                radius = 110f * pulse,
                center = Offset(
                    x = size.width * 0.18f,
                    y = size.height * 0.78f
                )
            )
        }
    }
}

@Composable
fun StartPrimaryButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC96B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF172554),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
