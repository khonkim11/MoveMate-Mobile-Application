package com.example.movemate

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun ActivityScreen(
    userId: Int,
    onBack: () -> Unit,
    onSelect: (WorkoutActivityOption) -> Unit,
    onHome: () -> Unit = onBack,
    onSummary: () -> Unit = {},
    onProgress: () -> Unit = {},
    onGoal: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    BackHandler(
        onBack = onBack
    )

    val context =
        LocalContext.current

    LaunchedEffect(
        userId
    ) {
        MoveMateAlertStore
            .ensureLoaded(
                context
            )
    }

    val unreadCount by
    remember(
        userId
    ) {
        derivedStateOf {
            MoveMateAlertStore
                .unreadCount(
                    userId
                )
        }
    }

    var openingActivityName by remember {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        containerColor =
            Color(0xFFF5F7FB),
        bottomBar = {
            ActivityFooterBar(
                onHome =
                    onHome,
                onActivity = {},
                onSummary =
                    onSummary,
                onProgress =
                    onProgress,
                onGoal =
                    onGoal,
                onProfile =
                    onProfile
            )
        }
    ) {
            innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color(0xFFF8FBFF),
                                Color(0xFFF1F6FF),
                                Color(0xFFFFFFFF)
                            )
                    )
                ),
            contentPadding =
                PaddingValues(
                    start =
                        20.dp,
                    top =
                        innerPadding
                            .calculateTopPadding() +
                                14.dp,
                    end =
                        20.dp,
                    bottom =
                        innerPadding
                            .calculateBottomPadding() +
                                20.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {
            item {
                ActivityTopBar(
                    unreadCount =
                        unreadCount,
                    onBack =
                        onBack,
                    onHome =
                        onHome,
                    onNotifications =
                        onNotifications
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                ActivityHeroCard()
            }

            items(
                items =
                    workoutActivityOptions,
                key = {
                        activity ->

                    activity.name.lowercase(
                        Locale.US
                    )
                }
            ) {
                    activity ->

                ActivitySelectionCard(
                    activity =
                        activity,
                    enabled =
                        openingActivityName ==
                                null,
                    isOpening =
                        openingActivityName
                            ?.equals(
                                activity.name,
                                ignoreCase =
                                    true
                            ) ==
                                true,
                    onClick = {
                        if (
                            openingActivityName ==
                            null
                        ) {
                            openingActivityName =
                                activity.name

                            onSelect(
                                activity
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ActivityTopBar(
    unreadCount: Int,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onNotifications: () -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        CircleIconButton(
            label =
                "‹",
            containerColor =
                Color.White,
            contentColor =
                Color(0xFF1D4ED8),
            onClick =
                onBack
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            CircleIconButton(
                label =
                    "⌂",
                containerColor =
                    Color(0xFFEAF2FF),
                contentColor =
                    Color(0xFF1D4ED8),
                onClick =
                    onHome
            )

            NotificationBellButton(
                unreadCount =
                    unreadCount,
                onClick =
                    onNotifications
            )
        }
    }
}

@Composable
private fun ActivityHeroCard() {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                28.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.Transparent
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    0.dp
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors =
                            listOf(
                                Color(0xFF1D4ED8),
                                Color(0xFF2563EB),
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
                Text(
                    text =
                        "Choose Activity",
                    color =
                        Color.White,
                    fontSize =
                        30.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        "Pick one activity first, then choose Easy, Medium, or Hard on the next page.",
                    color =
                        Color.White.copy(
                            alpha =
                                0.92f
                        ),
                    fontSize =
                        14.sp,
                    lineHeight =
                        20.sp,
                    modifier =
                        Modifier.padding(
                            top =
                                8.dp
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {
                    HeroChip(
                        emoji =
                            "⚡",
                        text =
                            "Smooth flow"
                    )

                    HeroChip(
                        emoji =
                            "🗺",
                        text =
                            "Map for route workouts"
                    )

                    HeroChip(
                        emoji =
                            "🎯",
                        text =
                            "3 levels"
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroChip(
    emoji: String,
    text: String
) {
    Row(
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    999.dp
                )
            )
            .background(
                Color.White.copy(
                    alpha =
                        0.18f
                )
            )
            .padding(
                horizontal =
                    10.dp,
                vertical =
                    8.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {
        Text(
            text =
                emoji,
            fontSize =
                14.sp
        )

        Text(
            text =
                text,
            color =
                Color.White,
            fontSize =
                11.sp,
            fontWeight =
                FontWeight.SemiBold
        )
    }
}

@Composable
private fun NotificationBellButton(
    unreadCount: Int,
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
                Color(0xFFF1F5F9)
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
                "🔔",
            fontSize =
                18.sp
        )

        if (
            unreadCount >
            0
        ) {
            Box(
                modifier = Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .size(
                        20.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        Color(0xFFDC2626)
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        unreadCount
                            .coerceAtMost(
                                99
                            )
                            .toString(),
                    color =
                        Color.White,
                    fontSize =
                        8.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ActivitySelectionCard(
    activity: WorkoutActivityOption,
    enabled: Boolean,
    isOpening: Boolean,
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
                0.985f
            } else {
                1f
            },
        animationSpec =
            spring(
                dampingRatio =
                    0.78f,
                stiffness =
                    520f
            ),
        label =
            "activity_card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(
                scale
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
            ),
        shape =
            RoundedCornerShape(
                26.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    6.dp
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Surface(
                modifier =
                    Modifier.size(
                        58.dp
                    ),
                shape =
                    CircleShape,
                color =
                    activity
                        .accentColor
                        .copy(
                            alpha =
                                0.12f
                        )
            ) {
                Box(
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            activity.emoji,
                        fontSize =
                            28.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(
                        1f
                    )
                    .padding(
                        start =
                            14.dp
                    )
            ) {
                Text(
                    text =
                        activity.name,
                    color =
                        Color(0xFF172554),
                    fontSize =
                        19.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        activity.description,
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

                Text(
                    text =
                        if (
                            activity.tracksRoute
                        ) {
                            "Route tracking available"
                        } else {
                            "Animation and timer workout"
                        },
                    color =
                        activity.accentColor,
                    fontSize =
                        11.sp,
                    fontWeight =
                        FontWeight.SemiBold,
                    modifier =
                        Modifier.padding(
                            top =
                                8.dp
                        )
                )
            }

            if (
                isOpening
            ) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(
                            22.dp
                        ),
                    color =
                        activity.accentColor,
                    strokeWidth =
                        3.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                999.dp
                            )
                        )
                        .background(
                            activity
                                .accentColor
                                .copy(
                                    alpha =
                                        0.12f
                                )
                        )
                        .padding(
                            horizontal =
                                12.dp,
                            vertical =
                                9.dp
                        )
                ) {
                    Text(
                        text =
                            "Open",
                        color =
                            activity.accentColor,
                        fontSize =
                            12.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityFooterBar(
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onSummary: () -> Unit,
    onProgress: () -> Unit,
    onGoal: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shadowElevation =
            18.dp,
        color =
            Color.White,
        tonalElevation =
            8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        6.dp,
                    vertical =
                        8.dp
                ),
            horizontalArrangement =
                Arrangement.SpaceEvenly,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            FooterItem(
                emoji =
                    "⌂",
                label =
                    "Home",
                selected =
                    false,
                onClick =
                    onHome
            )

            FooterItem(
                emoji =
                    "🏃",
                label =
                    "Activity",
                selected =
                    true,
                onClick =
                    onActivity
            )

            FooterItem(
                emoji =
                    "📋",
                label =
                    "Summary",
                selected =
                    false,
                onClick =
                    onSummary
            )

            FooterItem(
                emoji =
                    "📈",
                label =
                    "Progress",
                selected =
                    false,
                onClick =
                    onProgress
            )

            FooterItem(
                emoji =
                    "🎯",
                label =
                    "Goal",
                selected =
                    false,
                onClick =
                    onGoal
            )

            FooterItem(
                emoji =
                    "👤",
                label =
                    "Profile",
                selected =
                    false,
                onClick =
                    onProfile
            )
        }
    }
}

@Composable
private fun FooterItem(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    18.dp
                )
            )
            .clickable(
                onClick =
                    onClick
            )
            .padding(
                horizontal =
                    8.dp,
                vertical =
                    6.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(
                4.dp
            )
    ) {
        Box(
            modifier = Modifier
                .size(
                    34.dp
                )
                .clip(
                    CircleShape
                )
                .background(
                    if (
                        selected
                    ) {
                        Color(0xFFE8F0FF)
                    } else {
                        Color(0xFFF8FAFC)
                    }
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    emoji,
                fontSize =
                    16.sp
            )
        }

        Text(
            text =
                label,
            fontSize =
                10.sp,
            fontWeight =
                if (
                    selected
                ) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
            color =
                if (
                    selected
                ) {
                    Color(0xFF1D4ED8)
                } else {
                    Color(0xFF64748B)
                },
            textAlign =
                TextAlign.Center
        )
    }
}

@Composable
private fun CircleIconButton(
    label: String,
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
                label,
            color =
                contentColor,
            fontSize =
                20.sp,
            fontWeight =
                FontWeight.Bold
        )
    }
}