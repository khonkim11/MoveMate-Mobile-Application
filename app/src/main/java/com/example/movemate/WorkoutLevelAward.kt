package com.example.movemate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

fun targetAwardForLevel(
    levelName: String
): WorkoutAwardTier {
    return when (
        levelName
            .trim()
            .lowercase(
                Locale.US
            )
    ) {
        "hard" ->
            WorkoutAwardTier.DIAMOND

        "medium",
        "normal" ->
            WorkoutAwardTier.GOLD

        "easy",
        "quick",
        "quick 1 min",
        "quick 1 minute" ->
            WorkoutAwardTier.SILVER

        else ->
            WorkoutAwardTier.STARTER
    }
}

fun calculateLevelWorkoutAward(
    levelName: String,
    durationSeconds: Int,
    targetSeconds: Int
): WorkoutAwardTier {
    if (
        durationSeconds <=
        0 ||
        targetSeconds <=
        0
    ) {
        return WorkoutAwardTier.STARTER
    }

    return if (
        durationSeconds >=
        targetSeconds
    ) {
        targetAwardForLevel(
            levelName
        )
    } else {
        WorkoutAwardTier.STARTER
    }
}

@Composable
fun WorkoutLevelAwardCard(
    levelName: String,
    elapsedSeconds: Int,
    targetSeconds: Int,
    accentColor: Color,
    modifier: Modifier =
        Modifier
) {
    val targetAward =
        targetAwardForLevel(
            levelName
        )

    val progress =
        if (
            targetSeconds >
            0
        ) {
            (
                    elapsedSeconds
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

    val unlocked =
        progress >=
                1f

    val displayAward =
        if (
            unlocked
        ) {
            targetAward
        } else {
            WorkoutAwardTier.STARTER
        }

    val cardBackground =
        Brush.linearGradient(
            colors =
                listOf(
                    accentColor.copy(
                        alpha =
                            0.13f
                    ),
                    awardColor(
                        targetAward
                    )
                        .copy(
                            alpha =
                                0.10f
                        ),
                    Color.White
                )
        )

    Card(
        modifier =
            modifier.fillMaxWidth(),
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
                    3.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    cardBackground
                )
                .padding(
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
                            58.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            awardColor(
                                targetAward
                            )
                                .copy(
                                    alpha =
                                        if (
                                            unlocked
                                        ) {
                                            0.20f
                                        } else {
                                            0.10f
                                        }
                                )
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            if (
                                unlocked
                            ) {
                                targetAward.emoji
                            } else {
                                "🔒"
                            },
                        fontSize =
                            29.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(
                            1f
                        )
                        .padding(
                            start =
                                13.dp
                        )
                ) {
                    Text(
                        text =
                            "${levelName.trim().ifBlank { "Workout" }} Level Award",
                        color =
                            Color(0xFF64748B),
                        fontSize =
                            10.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            if (
                                unlocked
                            ) {
                                "${displayAward.title} Unlocked"
                            } else {
                                "Unlock ${targetAward.title}"
                            },
                        color =
                            Color(0xFF0F172A),
                        fontSize =
                            19.sp,
                        fontWeight =
                            FontWeight.ExtraBold,
                        modifier =
                            Modifier.padding(
                                top =
                                    2.dp
                            )
                    )

                    Text(
                        text =
                            if (
                                unlocked
                            ) {
                                "${targetAward.message} This award will be saved with the workout."
                            } else {
                                "Complete 100% of this workout to earn the ${targetAward.title} award."
                            },
                        color =
                            Color(0xFF475569),
                        fontSize =
                            11.sp,
                        lineHeight =
                            16.sp,
                        modifier =
                            Modifier.padding(
                                top =
                                    4.dp
                            )
                    )
                }
            }

            LinearProgressIndicator(
                progress = {
                    progress
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            15.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            999.dp
                        )
                    ),
                color =
                    awardColor(
                        targetAward
                    ),
                trackColor =
                    Color(0xFFE2E8F0)
            )

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
                        "${(progress * 100f).toInt()}% completed",
                    color =
                        Color(0xFF64748B),
                    fontSize =
                        10.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        if (
                            unlocked
                        ) {
                            "AWARD READY"
                        } else {
                            "TARGET 100%"
                        },
                    color =
                        awardColor(
                            targetAward
                        ),
                    fontSize =
                        10.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun awardColor(
    award: WorkoutAwardTier
): Color {
    return when (
        award
    ) {
        WorkoutAwardTier.DIAMOND ->
            Color(0xFF0284C7)

        WorkoutAwardTier.GOLD ->
            Color(0xFFCA8A04)

        WorkoutAwardTier.SILVER ->
            Color(0xFF64748B)

        WorkoutAwardTier.STARTER ->
            Color(0xFF7C3AED)
    }
}