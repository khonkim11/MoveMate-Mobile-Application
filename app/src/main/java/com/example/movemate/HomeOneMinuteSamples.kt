package com.example.movemate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Home-page carousel for one-minute sample workouts.
 *
 * It does not replace the normal workout flow.
 */
@Composable
fun HomeOneMinuteSamples(
    activities: List<WorkoutActivityOption>,
    enabled: Boolean =
        true,
    onStartSample:
        (
        WorkoutActivityOption,
        WorkoutLevel
    ) -> Unit
) {
    val availableActivities =
        activities.filter {
            it.levels.isNotEmpty()
        }

    if (
        availableActivities.isEmpty()
    ) {
        return
    }

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                25.dp
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
        Column(
            modifier =
                Modifier.padding(
                    vertical =
                        17.dp
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            17.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Text(
                        text =
                            "1-Minute Samples",
                        color =
                            Color(0xFF0F172A),
                        fontSize =
                            19.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            "Try an activity before choosing a full level.",
                        color =
                            Color(0xFF64748B),
                        fontSize =
                            11.sp,
                        modifier =
                            Modifier.padding(
                                top =
                                    3.dp
                            )
                    )
                }

                Surface(
                    shape =
                        RoundedCornerShape(
                            999.dp
                        ),
                    color =
                        Color(0xFFFFEDD5)
                ) {
                    Text(
                        text =
                            "60 SEC",
                        color =
                            Color(0xFFEA580C),
                        fontSize =
                            9.sp,
                        fontWeight =
                            FontWeight.ExtraBold,
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    10.dp,
                                vertical =
                                    7.dp
                            )
                    )
                }
            }

            LazyRow(
                modifier =
                    Modifier.padding(
                        top =
                            14.dp
                    ),
                contentPadding =
                    PaddingValues(
                        start =
                            17.dp,
                        end =
                            17.dp
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        11.dp
                    )
            ) {
                items(
                    items =
                        availableActivities,
                    key = {
                            activity ->

                        "home-sample-${
                            activity.name.lowercase(
                                Locale.US
                            )
                        }"
                    }
                ) {
                        activity ->

                    HomeOneMinuteSampleCard(
                        activity =
                            activity,
                        enabled =
                            enabled,
                        onClick = {
                            onStartSample(
                                activity,
                                oneMinuteSampleLevel(
                                    activity
                                )
                            )
                        }
                    )
                }
            }

            if (
                !enabled
            ) {
                Text(
                    text =
                        "Finish or pause your current workout before starting another sample.",
                    color =
                        Color(0xFFB45309),
                    fontSize =
                        10.sp,
                    modifier =
                        Modifier.padding(
                            start =
                                17.dp,
                            top =
                                12.dp,
                            end =
                                17.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun HomeOneMinuteSampleCard(
    activity: WorkoutActivityOption,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(
                142.dp
            )
            .height(
                154.dp
            )
            .clickable(
                enabled =
                    enabled,
                onClick =
                    onClick
            ),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (
                        enabled
                    ) {
                        Color(0xFFF8FAFC)
                    } else {
                        Color(0xFFF1F5F9)
                    }
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    if (
                        enabled
                    ) {
                        2.dp
                    } else {
                        0.dp
                    }
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    14.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .size(
                        46.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        activity
                            .accentColor
                            .copy(
                                alpha =
                                    if (
                                        enabled
                                    ) {
                                        0.14f
                                    } else {
                                        0.07f
                                    }
                            )
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        activity.emoji,
                    fontSize =
                        23.sp
                )
            }

            Text(
                text =
                    activity.name,
                color =
                    if (
                        enabled
                    ) {
                        Color(0xFF0F172A)
                    } else {
                        Color(0xFF94A3B8)
                    },
                fontSize =
                    14.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                modifier =
                    Modifier.padding(
                        top =
                            10.dp
                    )
            )

            Text(
                text =
                    if (
                        activity.tracksRoute
                    ) {
                        "Map sample"
                    } else {
                        "Timer sample"
                    },
                color =
                    Color(0xFF64748B),
                fontSize =
                    10.sp,
                modifier =
                    Modifier.padding(
                        top =
                            3.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            Text(
                text =
                    if (
                        enabled
                    ) {
                        "Start 1 min →"
                    } else {
                        "Unavailable"
                    },
                color =
                    if (
                        enabled
                    ) {
                        activity.accentColor
                    } else {
                        Color(0xFF94A3B8)
                    },
                fontSize =
                    10.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}
