package com.example.movemate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

const val DEFAULT_DAILY_WORKOUT_CALORIE_TARGET =
    500.0

fun effectiveWorkoutCalorieTarget(
    savedGoalTarget: Double
): Double {
    return savedGoalTarget
        .takeIf {
            it >
                    0.0
        }
        ?: DEFAULT_DAILY_WORKOUT_CALORIE_TARGET
}

@Composable
fun animatedTodayCaloriesWithActiveWorkouts(
    userId: Int,
    savedCalories: Double
): Float {
    /*
     * Reading activeWorkouts here makes Compose observe every manager update.
     */
    val activeCalories =
        ActiveWorkoutManager
            .activeWorkouts
            .filter {
                it.userId ==
                        userId &&
                        !it.isFinished
            }
            .sumOf {
                it.calories
            }

    val total =
        savedCalories
            .coerceAtLeast(
                0.0
            ) +
                activeCalories

    val animated by
    animateFloatAsState(
        targetValue =
            total.toFloat(),
        animationSpec =
            tween(
                durationMillis =
                    600
            ),
        label =
            "saved_plus_active_calories"
    )

    return animated
}