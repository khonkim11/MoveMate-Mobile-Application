package com.example.movemate

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Alternative workout-level design.
 *
 * Navigation and start behavior remain compatible with the existing app.
 */
@Composable
fun WorkoutLevelScreen(
    activityOption: WorkoutActivityOption,
    onStart:
        (
        WorkoutActivityOption,
        WorkoutLevel
    ) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit = {},
    onActivities: () -> Unit =
        onBack,
    onProgress: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    BackHandler(
        onBack =
            onBack
    )

    val availableLevels =
        remember(
            activityOption.name,
            activityOption.levels
        ) {
            ensureEasyMediumHardLevels(
                activityOption.levels
            )
        }

    var selectedLevel by remember(
        activityOption.name,
        availableLevels
    ) {
        mutableStateOf(
            availableLevels
                .firstOrNull()
        )
    }

    var launchLocked by remember(
        activityOption.name
    ) {
        mutableStateOf(
            false
        )
    }

    LaunchedEffect(
        launchLocked
    ) {
        if (
            launchLocked
        ) {
            delay(
                1_100L
            )

            launchLocked =
                false
        }
    }

    Scaffold(
        containerColor =
            Color(0xFFF4F7FC),
        bottomBar = {
            LevelBottomSection(
                activityOption =
                    activityOption,
                selectedLevel =
                    selectedLevel,
                launchLocked =
                    launchLocked,
                onStart = {
                    val chosenLevel =
                        selectedLevel

                    if (
                        chosenLevel !=
                        null &&
                        !launchLocked
                    ) {
                        launchLocked =
                            true

                        onStart(
                            activityOption,
                            chosenLevel
                        )
                    }
                },
                onHome =
                    onHome,
                onActivities =
                    onActivities,
                onProgress =
                    onProgress,
                onProfile =
                    onProfile
            )
        }
    ) {
            scaffoldPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                activityOption
                                    .accentColor
                                    .copy(
                                        alpha =
                                            0.10f
                                    ),
                                Color(0xFFF6F9FF),
                                Color.White
                            )
                    )
                ),
            contentPadding =
                PaddingValues(
                    start =
                        18.dp,
                    top =
                        scaffoldPadding
                            .calculateTopPadding() +
                                14.dp,
                    end =
                        18.dp,
                    bottom =
                        scaffoldPadding
                            .calculateBottomPadding() +
                                24.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {
            item {
                LevelTopBar(
                    onBack =
                        onBack,
                    onHome =
                        onHome,
                    accentColor =
                        activityOption
                            .accentColor
                )
            }

            item {
                NewActivityHero(
                    activityOption =
                        activityOption
                )
            }

            item {
                Text(
                    text =
                        "Choose your plan",
                    color =
                        Color(0xFF0F172A),
                    fontSize =
                        22.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        "Switch between Easy, Medium and Hard. " +
                                "The selected plan is shown below.",
                    color =
                        Color(0xFF64748B),
                    fontSize =
                        12.sp,
                    lineHeight =
                        18.sp,
                    modifier =
                        Modifier.padding(
                            top =
                                4.dp
                        )
                )
            }

            item {
                LevelSegmentSelector(
                    levels =
                        availableLevels,
                    selectedLevel =
                        selectedLevel,
                    accentColor =
                        activityOption
                            .accentColor,
                    enabled =
                        !launchLocked,
                    onSelect = {
                            level ->

                        selectedLevel =
                            level
                    }
                )
            }

            item {
                selectedLevel
                    ?.let {
                            level ->

                        SelectedPlanCard(
                            activityOption =
                                activityOption,
                            level =
                                level,
                            levelIndex =
                                availableLevels
                                    .indexOfFirst {
                                            item ->

                                        item.name ==
                                                level.name
                                    }
                                    .coerceAtLeast(
                                        0
                                    )
                        )
                    }
            }

            item {
                PlanGuideCard(
                    activityOption =
                        activityOption,
                    selectedLevel =
                        selectedLevel
                )
            }
        }
    }
}

@Composable
private fun LevelTopBar(
    onBack: () -> Unit,
    onHome: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        LevelCircleButton(
            symbol =
                "←",
            containerColor =
                Color.White,
            contentColor =
                Color(0xFF334155),
            onClick =
                onBack
        )

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    "Workout Plan",
                color =
                    Color(0xFF0F172A),
                fontSize =
                    20.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    "Select your level",
                color =
                    Color(0xFF64748B),
                fontSize =
                    10.sp
            )
        }

        LevelCircleButton(
            symbol =
                "⌂",
            containerColor =
                accentColor.copy(
                    alpha =
                        0.12f
                ),
            contentColor =
                accentColor,
            onClick =
                onHome
        )
    }
}

@Composable
private fun LevelCircleButton(
    symbol: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(
                48.dp
            )
            .clip(
                CircleShape
            )
            .background(
                containerColor
            )
            .clickable(
                onClick =
                    onClick
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                symbol,
            color =
                contentColor,
            fontSize =
                21.sp,
            fontWeight =
                FontWeight.ExtraBold
        )
    }
}

@Composable
private fun NewActivityHero(
    activityOption: WorkoutActivityOption
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                30.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.Transparent
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    7.dp
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors =
                            listOf(
                                Color(0xFF0F172A),
                                activityOption
                                    .accentColor,
                                Color(0xFF38BDF8)
                            )
                    )
                )
                .padding(
                    22.dp
                )
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(
                                72.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    23.dp
                                )
                            )
                            .background(
                                Color.White.copy(
                                    alpha =
                                        0.16f
                                )
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text =
                                activityOption.emoji,
                            fontSize =
                                34.sp
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(
                                1f
                            )
                            .padding(
                                start =
                                    15.dp
                            )
                    ) {
                        Text(
                            text =
                                activityOption.name,
                            color =
                                Color.White,
                            fontSize =
                                28.sp,
                            fontWeight =
                                FontWeight.ExtraBold
                        )

                        Text(
                            text =
                                activityOption.description,
                            color =
                                Color.White.copy(
                                    alpha =
                                        0.84f
                                ),
                            fontSize =
                                12.sp,
                            lineHeight =
                                17.sp,
                            modifier =
                                Modifier.padding(
                                    top =
                                        4.dp
                                )
                        )
                    }
                }

                Row(
                    modifier =
                        Modifier.padding(
                            top =
                                18.dp
                        ),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            9.dp
                        )
                ) {
                    HeroBadge(
                        text =
                            "3 LEVELS"
                    )

                    HeroBadge(
                        text =
                            if (
                                activityOption.tracksRoute
                            ) {
                                "GPS MAP"
                            } else {
                                "TIMER MODE"
                            }
                    )

                    HeroBadge(
                        text =
                            "LIVE CALORIES"
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBadge(
    text: String
) {
    Text(
        text =
            text,
        color =
            Color.White,
        fontSize =
            9.sp,
        fontWeight =
            FontWeight.ExtraBold,
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    999.dp
                )
            )
            .background(
                Color.White.copy(
                    alpha =
                        0.15f
                )
            )
            .padding(
                horizontal =
                    10.dp,
                vertical =
                    7.dp
            )
    )
}

@Composable
private fun LevelSegmentSelector(
    levels: List<WorkoutLevel>,
    selectedLevel: WorkoutLevel?,
    accentColor: Color,
    enabled: Boolean,
    onSelect:
        (
        WorkoutLevel
    ) -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                23.dp
            ),
        color =
            Color.White,
        shadowElevation =
            4.dp
    ) {
        Row(
            modifier =
                Modifier.padding(
                    7.dp
                ),
            horizontalArrangement =
                Arrangement.spacedBy(
                    7.dp
                )
        ) {
            levels.forEachIndexed {
                    index,
                    level ->

                LevelSegment(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    level =
                        level,
                    levelIndex =
                        index,
                    selected =
                        selectedLevel
                            ?.name ==
                                level.name,
                    enabled =
                        enabled,
                    accentColor =
                        accentColor,
                    onClick = {
                        onSelect(
                            level
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun LevelSegment(
    modifier: Modifier,
    level: WorkoutLevel,
    levelIndex: Int,
    selected: Boolean,
    enabled: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource
        .collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (
                pressed
            ) {
                0.95f
            } else {
                1f
            },
        animationSpec =
            spring(
                dampingRatio =
                    0.82f,
                stiffness =
                    520f
            ),
        label =
            "level_segment_scale"
    )

    val backgroundColor by
    animateColorAsState(
        targetValue =
            if (
                selected
            ) {
                accentColor
            } else {
                Color(0xFFF5F7FB)
            },
        animationSpec =
            tween(
                durationMillis =
                    220
            ),
        label =
            "level_segment_color"
    )

    Column(
        modifier =
            modifier
                .scale(
                    scale
                )
                .clip(
                    RoundedCornerShape(
                        17.dp
                    )
                )
                .background(
                    backgroundColor
                )
                .clickable(
                    enabled =
                        enabled,
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    onClick =
                        onClick
                )
                .padding(
                    horizontal =
                        8.dp,
                    vertical =
                        13.dp
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                levelIcon(
                    levelIndex
                ),
            fontSize =
                20.sp
        )

        Text(
            text =
                level.name,
            color =
                if (
                    selected
                ) {
                    Color.White
                } else {
                    Color(0xFF334155)
                },
            fontSize =
                13.sp,
            fontWeight =
                FontWeight.ExtraBold,
            modifier =
                Modifier.padding(
                    top =
                        5.dp
                )
        )

        Text(
            text =
                "${level.targetMinutes} min",
            color =
                if (
                    selected
                ) {
                    Color.White.copy(
                        alpha =
                            0.78f
                    )
                } else {
                    Color(0xFF94A3B8)
                },
            fontSize =
                9.sp,
            fontWeight =
                FontWeight.Bold,
            modifier =
                Modifier.padding(
                    top =
                        2.dp
                )
        )
    }
}

@Composable
private fun SelectedPlanCard(
    activityOption: WorkoutActivityOption,
    level: WorkoutLevel,
    levelIndex: Int
) {
    val targetDistanceKm =
        realisticTargetDistanceKm(
            activity =
                activityOption,
            level =
                level
        )

    val targetSpeedKmh =
        activityOption
            .speedForLevelKmh(
                level
            )

    val borderWidth by
    animateDpAsState(
        targetValue =
            1.5.dp,
        animationSpec =
            tween(
                durationMillis =
                    240
            ),
        label =
            "selected_plan_border"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec =
                    spring(
                        dampingRatio =
                            0.88f,
                        stiffness =
                            360f
                    )
            ),
        shape =
            RoundedCornerShape(
                28.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        border =
            BorderStroke(
                width =
                    borderWidth,
                color =
                    activityOption
                        .accentColor
                        .copy(
                            alpha =
                                0.42f
                        )
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    5.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    19.dp
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
                            RoundedCornerShape(
                                19.dp
                            )
                        )
                        .background(
                            activityOption
                                .accentColor
                                .copy(
                                    alpha =
                                        0.12f
                                )
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            levelIcon(
                                levelIndex
                            ),
                        fontSize =
                            28.sp
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
                            "${level.name} Plan",
                        color =
                            Color(0xFF0F172A),
                        fontSize =
                            22.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            level.description,
                        color =
                            Color(0xFF64748B),
                        fontSize =
                            12.sp,
                        lineHeight =
                            17.sp,
                        modifier =
                            Modifier.padding(
                                top =
                                    3.dp
                            )
                    )
                }

                Text(
                    text =
                        "SELECTED",
                    color =
                        activityOption
                            .accentColor,
                    fontSize =
                        8.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                999.dp
                            )
                        )
                        .background(
                            activityOption
                                .accentColor
                                .copy(
                                    alpha =
                                        0.10f
                                )
                        )
                        .padding(
                            horizontal =
                                8.dp,
                            vertical =
                                6.dp
                        )
                )
            }

            Row(
                modifier =
                    Modifier.padding(
                        top =
                            18.dp
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        9.dp
                    )
            ) {
                PlanMetric(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    icon =
                        "⏱",
                    label =
                        "DURATION",
                    value =
                        "${level.targetMinutes} min",
                    accentColor =
                        activityOption
                            .accentColor
                )

                PlanMetric(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    icon =
                        if (
                            activityOption.tracksRoute
                        ) {
                            "📍"
                        } else {
                            "⚡"
                        },
                    label =
                        if (
                            activityOption.tracksRoute
                        ) {
                            "DISTANCE"
                        } else {
                            "MODE"
                        },
                    value =
                        if (
                            activityOption.tracksRoute
                        ) {
                            formatLevelDistance(
                                targetDistanceKm
                            )
                        } else {
                            "Timer"
                        },
                    accentColor =
                        Color(0xFF06B6D4)
                )

                PlanMetric(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    icon =
                        if (
                            activityOption.tracksRoute
                        ) {
                            "🏃"
                        } else {
                            "🔥"
                        },
                    label =
                        if (
                            activityOption.tracksRoute
                        ) {
                            "PACE"
                        } else {
                            "INTENSITY"
                        },
                    value =
                        if (
                            activityOption.tracksRoute
                        ) {
                            String.format(
                                Locale.US,
                                "%.1f km/h",
                                targetSpeedKmh
                            )
                        } else {
                            levelIntensityText(
                                levelIndex
                            )
                        },
                    accentColor =
                        Color(0xFF8B5CF6)
                )
            }

            AnimatedVisibility(
                visible =
                    true,
                enter =
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    220
                            )
                    ),
                exit =
                    fadeOut(
                        animationSpec =
                            tween(
                                durationMillis =
                                    140
                            )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top =
                                17.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                17.dp
                            )
                        )
                        .background(
                            Color(0xFFF8FAFC)
                        )
                        .padding(
                            13.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            "💡",
                        fontSize =
                            20.sp
                    )

                    Text(
                        text =
                            activityOption.beginnerTip,
                        color =
                            Color(0xFF475569),
                        fontSize =
                            11.sp,
                        lineHeight =
                            16.sp,
                        modifier =
                            Modifier.padding(
                                start =
                                    10.dp
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanMetric(
    modifier: Modifier,
    icon: String,
    label: String,
    value: String,
    accentColor: Color
) {
    Column(
        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        18.dp
                    )
                )
                .background(
                    accentColor.copy(
                        alpha =
                            0.08f
                    )
                )
                .padding(
                    horizontal =
                        8.dp,
                    vertical =
                        13.dp
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                icon,
            fontSize =
                20.sp
        )

        Text(
            text =
                label,
            color =
                Color(0xFF94A3B8),
            fontSize =
                8.sp,
            fontWeight =
                FontWeight.ExtraBold,
            modifier =
                Modifier.padding(
                    top =
                        6.dp
                )
        )

        Text(
            text =
                value,
            color =
                Color(0xFF0F172A),
            fontSize =
                12.sp,
            fontWeight =
                FontWeight.ExtraBold,
            textAlign =
                TextAlign.Center,
            modifier =
                Modifier.padding(
                    top =
                        3.dp
                )
        )
    }
}

@Composable
private fun PlanGuideCard(
    activityOption: WorkoutActivityOption,
    selectedLevel: WorkoutLevel?
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        color =
            Color(0xFF0F172A),
        shadowElevation =
            4.dp
    ) {
        Row(
            modifier =
                Modifier.padding(
                    17.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(
                        44.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        Color.White.copy(
                            alpha =
                                0.12f
                        )
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        "✓",
                    color =
                        Color.White,
                    fontSize =
                        18.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }

            Column(
                modifier = Modifier
                    .weight(
                        1f
                    )
                    .padding(
                        start =
                            12.dp
                    )
            ) {
                Text(
                    text =
                        if (
                            selectedLevel !=
                            null
                        ) {
                            "${selectedLevel.name} ${activityOption.name} is ready"
                        } else {
                            "Choose a plan"
                        },
                    color =
                        Color.White,
                    fontSize =
                        14.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        if (
                            activityOption.tracksRoute
                        ) {
                            "The next page opens the live map, timer, distance and calories."
                        } else {
                            "The next page opens the workout timer, movement animation and calories."
                        },
                    color =
                        Color.White.copy(
                            alpha =
                                0.65f
                        ),
                    fontSize =
                        10.sp,
                    lineHeight =
                        15.sp,
                    modifier =
                        Modifier.padding(
                            top =
                                3.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun LevelBottomSection(
    activityOption: WorkoutActivityOption,
    selectedLevel: WorkoutLevel?,
    launchLocked: Boolean,
    onStart: () -> Unit,
    onHome: () -> Unit,
    onActivities: () -> Unit,
    onProgress: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        color =
            Color.White,
        shadowElevation =
            15.dp,
        shape =
            RoundedCornerShape(
                topStart =
                    26.dp,
                topEnd =
                    26.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    start =
                        14.dp,
                    top =
                        12.dp,
                    end =
                        14.dp,
                    bottom =
                        9.dp
                )
        ) {
            Button(
                onClick =
                    onStart,
                enabled =
                    selectedLevel !=
                            null &&
                            !launchLocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        56.dp
                    ),
                shape =
                    RoundedCornerShape(
                        18.dp
                    ),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            activityOption
                                .accentColor,
                        disabledContainerColor =
                            activityOption
                                .accentColor
                                .copy(
                                    alpha =
                                        0.42f
                                )
                    )
            ) {
                if (
                    launchLocked
                ) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                21.dp
                            ),
                        color =
                            Color.White,
                        strokeWidth =
                            2.5.dp
                    )

                    Text(
                        text =
                            "Opening workout...",
                        color =
                            Color.White,
                        fontWeight =
                            FontWeight.ExtraBold,
                        fontSize =
                            14.sp,
                        modifier =
                            Modifier.padding(
                                start =
                                    9.dp
                            )
                    )
                } else {
                    Text(
                        text =
                            selectedLevel
                                ?.let {
                                        level ->

                                    "Start ${level.name} ${activityOption.name}"
                                }
                                ?: "Choose a level",
                        color =
                            Color.White,
                        fontSize =
                            15.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            8.dp
                    ),
                horizontalArrangement =
                    Arrangement.SpaceAround,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                LevelFooterItem(
                    icon =
                        "⌂",
                    label =
                        "Home",
                    selected =
                        false,
                    enabled =
                        !launchLocked,
                    accentColor =
                        activityOption
                            .accentColor,
                    onClick =
                        onHome
                )

                LevelFooterItem(
                    icon =
                        "🏃",
                    label =
                        "Activities",
                    selected =
                        true,
                    enabled =
                        !launchLocked,
                    accentColor =
                        activityOption
                            .accentColor,
                    onClick =
                        onActivities
                )

                LevelFooterItem(
                    icon =
                        "📈",
                    label =
                        "Progress",
                    selected =
                        false,
                    enabled =
                        !launchLocked,
                    accentColor =
                        activityOption
                            .accentColor,
                    onClick =
                        onProgress
                )

                LevelFooterItem(
                    icon =
                        "👤",
                    label =
                        "Profile",
                    selected =
                        false,
                    enabled =
                        !launchLocked,
                    accentColor =
                        activityOption
                            .accentColor,
                    onClick =
                        onProfile
                )
            }
        }
    }
}

@Composable
private fun LevelFooterItem(
    icon: String,
    label: String,
    selected: Boolean,
    enabled: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource
        .collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (
                pressed
            ) {
                0.91f
            } else {
                1f
            },
        animationSpec =
            spring(
                dampingRatio =
                    0.82f,
                stiffness =
                    500f
            ),
        label =
            "level_footer_scale"
    )

    Column(
        modifier = Modifier
            .width(
                74.dp
            )
            .scale(
                scale
            )
            .clip(
                RoundedCornerShape(
                    16.dp
                )
            )
            .background(
                if (
                    selected
                ) {
                    accentColor.copy(
                        alpha =
                            0.09f
                    )
                } else {
                    Color.Transparent
                }
            )
            .clickable(
                enabled =
                    enabled,
                interactionSource =
                    interactionSource,
                indication =
                    null,
                onClick =
                    onClick
            )
            .padding(
                vertical =
                    7.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                icon,
            fontSize =
                17.sp
        )

        Text(
            text =
                label,
            color =
                if (
                    selected
                ) {
                    accentColor
                } else {
                    Color(0xFF94A3B8)
                },
            fontSize =
                9.sp,
            fontWeight =
                if (
                    selected
                ) {
                    FontWeight.ExtraBold
                } else {
                    FontWeight.Bold
                },
            modifier =
                Modifier.padding(
                    top =
                        2.dp
                )
        )
    }
}

private fun levelIcon(
    index: Int
): String {
    return when (
        index
    ) {
        0 ->
            "🌱"

        1 ->
            "⚡"

        else ->
            "🔥"
    }
}

private fun levelIntensityText(
    index: Int
): String {
    return when (
        index
    ) {
        0 ->
            "Light"

        1 ->
            "Moderate"

        else ->
            "High"
    }
}

private fun formatLevelDistance(
    distanceKm: Double
): String {
    val safeDistance =
        distanceKm.coerceAtLeast(
            0.0
        )

    return if (
        safeDistance <
        1.0
    ) {
        String.format(
            Locale.US,
            "%.0f m",
            safeDistance *
                    1_000.0
        )
    } else {
        String.format(
            Locale.US,
            "%.2f km",
            safeDistance
        )
    }
}

private fun ensureEasyMediumHardLevels(
    levels: List<WorkoutLevel>
): List<WorkoutLevel> {
    val defaults =
        defaultWorkoutLevels()

    fun findLevel(
        group: Int
    ): WorkoutLevel? {
        return levels
            .firstOrNull {
                    level ->

                workoutLevelGroup(
                    level.name
                ) ==
                        group
            }
    }

    val easy =
        (
                findLevel(
                    1
                )
                    ?: defaults[
                        0
                    ]
                )
            .copy(
                name =
                    "Easy"
            )

    val medium =
        (
                findLevel(
                    2
                )
                    ?: defaults[
                        1
                    ]
                )
            .copy(
                name =
                    "Medium"
            )

    val hard =
        (
                findLevel(
                    3
                )
                    ?: defaults[
                        2
                    ]
                )
            .copy(
                name =
                    "Hard"
            )

    return listOf(
        easy,
        medium,
        hard
    )
}

private fun workoutLevelGroup(
    value: String
): Int {
    return when (
        value
            .trim()
            .lowercase(
                Locale.US
            )
    ) {
        "easy",
        "basic",
        "beginner" ->
            1

        "medium",
        "normal",
        "intermediate" ->
            2

        "hard",
        "advanced" ->
            3

        else ->
            0
    }
}
