package com.example.movemate

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MoveMateNotificationCenterScreen(
    session: UserSession,
    onBack: () -> Unit,
    onHome: () -> Unit = onBack,
    onActivity: () -> Unit = {},
    onSummary: () -> Unit = {},
    onProgress: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    BackHandler(
        onBack =
            onBack
    )

    val context =
        LocalContext.current

    var showContent by
    remember {
        mutableStateOf(
            false
        )
    }

    LaunchedEffect(
        session.userId
    ) {
        MoveMateAlertStore
            .ensureLoaded(
                context
            )

        showContent =
            true
    }

    val alerts =
        MoveMateAlertStore.items
            .filter {
                it.userId ==
                        session.userId
            }
            .sortedByDescending {
                it.createdAtMillis
            }

    val unreadCount =
        alerts.count {
            !it.isRead
        }

    Scaffold(
        containerColor =
            Color(0xFFF4F7FC),
        topBar = {
            NotificationTopBar(
                unreadCount =
                    unreadCount,
                onBack =
                    onBack,
                onHome =
                    onHome
            )
        },
        bottomBar = {
            NotificationFooter(
                onHome =
                    onHome,
                onActivity =
                    onActivity,
                onSummary =
                    onSummary,
                onProgress =
                    onProgress,
                onProfile =
                    onProfile
            )
        }
    ) {
            innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color(0xFFF5F9FF),
                                Color(0xFFEFF6FF),
                                Color(0xFFF8FAFC),
                                Color.White
                            )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(
                        240.dp
                    )
                    .background(
                        color =
                            Color(0xFF2563EB)
                                .copy(
                                    alpha =
                                        0.07f
                                ),
                        shape =
                            CircleShape
                    )
                    .align(
                        Alignment.TopEnd
                    )
            )

            Box(
                modifier = Modifier
                    .size(
                        180.dp
                    )
                    .background(
                        color =
                            Color(0xFF06B6D4)
                                .copy(
                                    alpha =
                                        0.06f
                                ),
                        shape =
                            CircleShape
                    )
                    .align(
                        Alignment.CenterStart
                    )
            )

            AnimatedVisibility(
                visible =
                    showContent,
                enter =
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    450
                            )
                    ) +
                            slideInVertically(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            520
                                    ),
                                initialOffsetY = {
                                    it /
                                            8
                                }
                            )
            ) {
                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            start =
                                16.dp,
                            top =
                                innerPadding
                                    .calculateTopPadding() +
                                        14.dp,
                            end =
                                16.dp,
                            bottom =
                                innerPadding
                                    .calculateBottomPadding() +
                                        26.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        )
                ) {
                    item {
                        NotificationHero(
                            unreadCount =
                                unreadCount,
                            totalCount =
                                alerts.size
                        )
                    }

                    if (
                        alerts.isNotEmpty()
                    ) {
                        item {
                            NotificationActions(
                                unreadCount =
                                    unreadCount,
                                onMarkAllRead = {
                                    MoveMateAlertStore
                                        .markAllRead(
                                            context =
                                                context,
                                            userId =
                                                session.userId
                                        )
                                },
                                onClear = {
                                    MoveMateAlertStore
                                        .clearUser(
                                            context =
                                                context,
                                            userId =
                                                session.userId
                                        )
                                }
                            )
                        }

                        item {
                            NotificationSectionHeader(
                                totalCount =
                                    alerts.size,
                                unreadCount =
                                    unreadCount
                            )
                        }
                    }

                    if (
                        alerts.isEmpty()
                    ) {
                        item {
                            EmptyNotificationCard()
                        }
                    } else {
                        items(
                            items =
                                alerts,
                            key = {
                                it.id
                            }
                        ) {
                                alert ->

                            NotificationItemCard(
                                item =
                                    alert,
                                onClick = {
                                    MoveMateAlertStore
                                        .markRead(
                                            context =
                                                context,
                                            id =
                                                alert.id
                                        )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationActions(
    unreadCount: Int,
    onMarkAllRead: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {
        Button(
            onClick =
                onMarkAllRead,
            enabled =
                unreadCount >
                        0,
            modifier = Modifier
                .weight(
                    1f
                )
                .height(
                    52.dp
                ),
            shape =
                RoundedCornerShape(
                    17.dp
                ),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF2563EB),
                    disabledContainerColor =
                        Color(0xFFDBEAFE),
                    disabledContentColor =
                        Color(0xFF64748B)
                )
        ) {
            Text(
                text =
                    "✓",
                fontSize =
                    15.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Spacer(
                modifier =
                    Modifier.width(
                        7.dp
                    )
            )

            Text(
                text =
                    "Mark all read",
                fontSize =
                    12.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }

        Button(
            onClick =
                onClear,
            modifier = Modifier
                .weight(
                    1f
                )
                .height(
                    52.dp
                ),
            shape =
                RoundedCornerShape(
                    17.dp
                ),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF0F172A)
                )
        ) {
            Text(
                text =
                    "⌫",
                fontSize =
                    16.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Spacer(
                modifier =
                    Modifier.width(
                        7.dp
                    )
            )

            Text(
                text =
                    "Clear alerts",
                fontSize =
                    12.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun NotificationTopBar(
    unreadCount: Int,
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    Surface(
        color =
            Color.White.copy(
                alpha =
                    0.98f
            ),
        shadowElevation =
            8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal =
                        14.dp,
                    vertical =
                        11.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            NotificationRoundButton(
                text =
                    "←",
                contentDescription =
                    "Back",
                onClick =
                    onBack
            )

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
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            "Notifications",
                        color =
                            Color(0xFF0F172A),
                        fontSize =
                            22.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    if (
                        unreadCount >
                        0
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
                                9.sp,
                            fontWeight =
                                FontWeight.ExtraBold,
                            modifier = Modifier
                                .padding(
                                    start =
                                        8.dp
                                )
                                .background(
                                    color =
                                        Color(0xFFEF4444),
                                    shape =
                                        CircleShape
                                )
                                .padding(
                                    horizontal =
                                        7.dp,
                                    vertical =
                                        3.dp
                                )
                        )
                    }
                }

                Text(
                    text =
                        if (
                            unreadCount ==
                            0
                        ) {
                            "You're all caught up"
                        } else {
                            "$unreadCount workout alerts need attention"
                        },
                    color =
                        Color(0xFF64748B),
                    fontSize =
                        10.sp,
                    modifier =
                        Modifier.padding(
                            top =
                                1.dp
                        )
                )
            }

            NotificationRoundButton(
                text =
                    "⌂",
                contentDescription =
                    "Home",
                onClick =
                    onHome
            )
        }
    }
}

@Composable
private fun NotificationRoundButton(
    text: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(
                45.dp
            )
            .clip(
                RoundedCornerShape(
                    16.dp
                )
            )
            .background(
                Brush.linearGradient(
                    colors =
                        listOf(
                            Color(0xFFEFF6FF),
                            Color(0xFFDBEAFE)
                        )
                )
            )
            .border(
                width =
                    1.dp,
                color =
                    Color(0xFFBFDBFE),
                shape =
                    RoundedCornerShape(
                        16.dp
                    )
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
                text,
            color =
                Color(0xFF1D4ED8),
            fontSize =
                20.sp,
            fontWeight =
                FontWeight.ExtraBold
        )
    }
}

@Composable
private fun NotificationHero(
    unreadCount: Int,
    totalCount: Int
) {
    val completionRatio =
        if (
            totalCount >
            0
        ) {
            (
                    (
                            totalCount -
                                    unreadCount
                            )
                        .toFloat() /
                            totalCount
                                .toFloat()
                    )
                .coerceIn(
                    0f,
                    1f
                )
        } else {
            1f
        }

    val animatedRatio by
    animateFloatAsState(
        targetValue =
            completionRatio,
        animationSpec =
            tween(
                durationMillis =
                    650
            ),
        label =
            "notification_read_ratio"
    )

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
                    10.dp
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors =
                            listOf(
                                Color(0xFF071A3D),
                                Color(0xFF1D4ED8),
                                Color(0xFF2563EB),
                                Color(0xFF06B6D4)
                            )
                    )
                )
                .padding(
                    21.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .size(
                        130.dp
                    )
                    .background(
                        color =
                            Color.White.copy(
                                alpha =
                                    0.08f
                            ),
                        shape =
                            CircleShape
                    )
                    .align(
                        Alignment.TopEnd
                    )
            )

            Column {
                Row(
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
                                    Color.White.copy(
                                        alpha =
                                            0.14f
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        19.dp
                                    )
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text =
                                "🔔",
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
                                "WORKOUT ALERT CENTER",
                            color =
                                Color.White.copy(
                                    alpha =
                                        0.72f
                                ),
                            fontSize =
                                9.sp,
                            fontWeight =
                                FontWeight.ExtraBold
                        )

                        Text(
                            text =
                                if (
                                    unreadCount ==
                                    0
                                ) {
                                    "All caught up"
                                } else {
                                    "$unreadCount unread alert" +
                                            if (
                                                unreadCount ==
                                                1
                                            ) {
                                                ""
                                            } else {
                                                "s"
                                            }
                                },
                            color =
                                Color.White,
                            fontSize =
                                25.sp,
                            fontWeight =
                                FontWeight.ExtraBold,
                            modifier =
                                Modifier.padding(
                                    top =
                                        2.dp
                                )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top =
                                20.dp
                        ),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {
                    NotificationHeroMetric(
                        modifier =
                            Modifier.weight(
                                1f
                            ),
                        value =
                            totalCount.toString(),
                        label =
                            "Total alerts",
                        icon =
                            "📨"
                    )

                    NotificationHeroMetric(
                        modifier =
                            Modifier.weight(
                                1f
                            ),
                        value =
                            (
                                    totalCount -
                                            unreadCount
                                    )
                                .coerceAtLeast(
                                    0
                                )
                                .toString(),
                        label =
                            "Read alerts",
                        icon =
                            "✓"
                    )

                    NotificationHeroMetric(
                        modifier =
                            Modifier.weight(
                                1f
                            ),
                        value =
                            "${(animatedRatio * 100f).toInt()}%",
                        label =
                            "Completed",
                        icon =
                            "⚡"
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationHeroMetric(
    modifier: Modifier,
    value: String,
    label: String,
    icon: String
) {
    Column(
        modifier =
            modifier
                .background(
                    color =
                        Color.White.copy(
                            alpha =
                                0.12f
                        ),
                    shape =
                        RoundedCornerShape(
                            17.dp
                        )
                )
                .padding(
                    horizontal =
                        10.dp,
                    vertical =
                        11.dp
                )
    ) {
        Text(
            text =
                icon,
            fontSize =
                14.sp
        )

        Text(
            text =
                value,
            color =
                Color.White,
            fontSize =
                18.sp,
            fontWeight =
                FontWeight.ExtraBold,
            modifier =
                Modifier.padding(
                    top =
                        4.dp
                )
        )

        Text(
            text =
                label,
            color =
                Color.White.copy(
                    alpha =
                        0.68f
                ),
            fontSize =
                8.sp,
            fontWeight =
                FontWeight.Bold,
            modifier =
                Modifier.padding(
                    top =
                        1.dp
                )
        )
    }
}

@Composable
private fun NotificationSectionHeader(
    totalCount: Int,
    unreadCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 2.dp,
                top = 2.dp,
                end = 2.dp
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
                    "Recent alerts",
                color =
                    Color(0xFF0F172A),
                fontSize =
                    18.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    "$totalCount saved • $unreadCount unread",
                color =
                    Color(0xFF64748B),
                fontSize =
                    10.sp,
                modifier =
                    Modifier.padding(
                        top =
                            2.dp
                    )
            )
        }

        Text(
            text =
                "NEWEST FIRST",
            color =
                Color(0xFF2563EB),
            fontSize =
                8.sp,
            fontWeight =
                FontWeight.ExtraBold,
            modifier =
                Modifier
                    .background(
                        color =
                            Color(0xFFDBEAFE),
                        shape =
                            RoundedCornerShape(
                                999.dp
                            )
                    )
                    .padding(
                        horizontal =
                            9.dp,
                        vertical =
                            6.dp
                    )
        )
    }
}

@Composable
private fun NotificationItemCard(
    item: MoveMateAlertItem,
    onClick: () -> Unit
) {
    val cardScale by
    animateFloatAsState(
        targetValue =
            if (
                item.isRead
            ) {
                0.995f
            } else {
                1f
            },
        animationSpec =
            tween(
                durationMillis =
                    220
            ),
        label =
            "notification_card_scale"
    )

    val accentColor =
        notificationAccentColor(
            item.emoji
        )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(
                cardScale
            )
            .clickable(
                onClick =
                    onClick
            ),
        shape =
            RoundedCornerShape(
                24.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (
                        item.isRead
                    ) {
                        Color.White
                    } else {
                        Color(0xFFF8FBFF)
                    }
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    if (
                        item.isRead
                    ) {
                        2.dp
                    } else {
                        7.dp
                    }
            )
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(
                        5.dp
                    )
                    .height(
                        128.dp
                    )
                    .background(
                        color =
                            if (
                                item.isRead
                            ) {
                                Color(0xFFCBD5E1)
                            } else {
                                accentColor
                            }
                    )
            )

            Row(
                modifier =
                    Modifier
                        .weight(
                            1f
                        )
                        .padding(
                            start =
                                14.dp,
                            top =
                                15.dp,
                            end =
                                15.dp,
                            bottom =
                                15.dp
                        ),
                verticalAlignment =
                    Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(
                            51.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                17.dp
                            )
                        )
                        .background(
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        accentColor.copy(
                                            alpha =
                                                0.20f
                                        ),
                                        accentColor.copy(
                                            alpha =
                                                0.08f
                                        )
                                    )
                            )
                        )
                        .border(
                            width =
                                1.dp,
                            color =
                                accentColor.copy(
                                    alpha =
                                        0.20f
                                ),
                            shape =
                                RoundedCornerShape(
                                    17.dp
                                )
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            item.emoji,
                        fontSize =
                            23.sp
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
                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Text(
                            text =
                                item.title,
                            color =
                                Color(0xFF0F172A),
                            fontSize =
                                14.sp,
                            fontWeight =
                                FontWeight.ExtraBold,
                            maxLines =
                                1,
                            overflow =
                                TextOverflow.Ellipsis,
                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )

                        Text(
                            text =
                                if (
                                    item.isRead
                                ) {
                                    "READ"
                                } else {
                                    "NEW"
                                },
                            color =
                                if (
                                    item.isRead
                                ) {
                                    Color(0xFF64748B)
                                } else {
                                    Color.White
                                },
                            fontSize =
                                8.sp,
                            fontWeight =
                                FontWeight.ExtraBold,
                            modifier =
                                Modifier
                                    .background(
                                        color =
                                            if (
                                                item.isRead
                                            ) {
                                                Color(0xFFE2E8F0)
                                            } else {
                                                accentColor
                                            },
                                        shape =
                                            RoundedCornerShape(
                                                999.dp
                                            )
                                    )
                                    .padding(
                                        horizontal =
                                            8.dp,
                                        vertical =
                                            5.dp
                                    )
                        )
                    }

                    Text(
                        text =
                            item.message,
                        color =
                            Color(0xFF475569),
                        fontSize =
                            11.sp,
                        lineHeight =
                            16.sp,
                        maxLines =
                            3,
                        overflow =
                            TextOverflow.Ellipsis,
                        modifier =
                            Modifier.padding(
                                top =
                                    6.dp
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top =
                                    10.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Text(
                            text =
                                "◷",
                            color =
                                Color(0xFF94A3B8),
                            fontSize =
                                12.sp
                        )

                        Text(
                            text =
                                formatNotificationTime(
                                    item.createdAtMillis
                                ),
                            color =
                                Color(0xFF94A3B8),
                            fontSize =
                                9.sp,
                            fontWeight =
                                FontWeight.SemiBold,
                            modifier =
                                Modifier.padding(
                                    start =
                                        5.dp
                                )
                        )

                        Spacer(
                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )

                        if (
                            !item.isRead
                        ) {
                            Text(
                                text =
                                    "Tap to mark read",
                                color =
                                    accentColor,
                                fontSize =
                                    8.sp,
                                fontWeight =
                                    FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotificationCard() {
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
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    5.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        24.dp,
                    vertical =
                        34.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(
                        88.dp
                    )
                    .background(
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    Color(0xFFEFF6FF),
                                    Color(0xFFDBEAFE)
                                )
                        ),
                        shape =
                            RoundedCornerShape(
                                28.dp
                            )
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        "🔕",
                    fontSize =
                        42.sp
                )
            }

            Text(
                text =
                    "No workout alerts yet",
                color =
                    Color(0xFF0F172A),
                fontSize =
                    21.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                modifier =
                    Modifier.padding(
                        top =
                            17.dp
                    )
            )

            Text(
                text =
                    "Start an activity to receive workout-start, halfway and target-completed alerts.",
                color =
                    Color(0xFF64748B),
                fontSize =
                    12.sp,
                lineHeight =
                    18.sp,
                textAlign =
                    TextAlign.Center,
                modifier = Modifier
                    .padding(
                        top =
                            7.dp
                    )
                    .fillMaxWidth(
                        0.88f
                    )
            )

            Text(
                text =
                    "START WORKOUT • EARN ALERTS • TRACK PROGRESS",
                color =
                    Color(0xFF2563EB),
                fontSize =
                    8.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                modifier =
                    Modifier
                        .padding(
                            top =
                                18.dp
                        )
                        .background(
                            color =
                                Color(0xFFEFF6FF),
                            shape =
                                RoundedCornerShape(
                                    999.dp
                                )
                        )
                        .padding(
                            horizontal =
                                12.dp,
                            vertical =
                                7.dp
                        )
            )
        }
    }
}

@Composable
private fun NotificationFooter(
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onSummary: () -> Unit,
    onProgress: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            Color.White.copy(
                alpha =
                    0.99f
            ),
        shadowElevation =
            18.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal =
                        4.dp,
                    vertical =
                        8.dp
                ),
            horizontalArrangement =
                Arrangement.SpaceEvenly,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            NotificationFooterItem(
                icon =
                    "⌂",
                label =
                    "Home",
                selected =
                    false,
                onClick =
                    onHome
            )

            NotificationFooterItem(
                icon =
                    "🏃",
                label =
                    "Activity",
                selected =
                    false,
                onClick =
                    onActivity
            )

            NotificationFooterItem(
                icon =
                    "📋",
                label =
                    "Summary",
                selected =
                    false,
                onClick =
                    onSummary
            )

            NotificationFooterItem(
                icon =
                    "📈",
                label =
                    "Progress",
                selected =
                    false,
                onClick =
                    onProgress
            )

            NotificationFooterItem(
                icon =
                    "🔔",
                label =
                    "Alerts",
                selected =
                    true,
                onClick = {}
            )

            NotificationFooterItem(
                icon =
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
private fun NotificationFooterItem(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(
                56.dp
            )
            .clip(
                RoundedCornerShape(
                    17.dp
                )
            )
            .background(
                if (
                    selected
                ) {
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color(0xFFEFF6FF),
                                Color(0xFFDBEAFE)
                            )
                    )
                } else {
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color.Transparent,
                                Color.Transparent
                            )
                    )
                }
            )
            .clickable(
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
        Box(
            modifier =
                if (
                    selected
                ) {
                    Modifier
                        .size(
                            31.dp
                        )
                        .background(
                            color =
                                Color(0xFF2563EB),
                            shape =
                                RoundedCornerShape(
                                    11.dp
                                )
                        )
                } else {
                    Modifier.size(
                        31.dp
                    )
                },
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    icon,
                fontSize =
                    15.sp,
                color =
                    if (
                        selected
                    ) {
                        Color.White
                    } else {
                        Color.Unspecified
                    }
            )
        }

        Text(
            text =
                label,
            color =
                if (
                    selected
                ) {
                    Color(0xFF2563EB)
                } else {
                    Color(0xFF64748B)
                },
            fontSize =
                8.sp,
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
                        3.dp
                )
        )
    }
}

private fun notificationAccentColor(
    emoji: String
): Color {
    return when {
        emoji.contains(
            "🏆"
        ) ->
            Color(0xFFF59E0B)

        emoji.contains(
            "⚡"
        ) ->
            Color(0xFF7C3AED)

        emoji.contains(
            "🏃"
        ) ->
            Color(0xFF16A34A)

        emoji.contains(
            "🚴"
        ) ->
            Color(0xFF0891B2)

        emoji.contains(
            "🏊"
        ) ->
            Color(0xFF0284C7)

        else ->
            Color(0xFF2563EB)
    }
}

private fun formatNotificationTime(
    value: Long
): String {
    return SimpleDateFormat(
        "dd MMM yyyy • h:mm a",
        Locale.US
    )
        .format(
            Date(
                value
            )
        )
}
