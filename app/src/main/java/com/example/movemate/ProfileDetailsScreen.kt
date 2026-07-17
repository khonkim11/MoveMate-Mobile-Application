package com.example.movemate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileDetailsScreen(
    session: UserSession,
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    BackHandler(
        onBack =
            onBack
    )

    var loading by remember(
        session.userId
    ) {
        mutableStateOf(
            true
        )
    }

    var errorMessage by remember(
        session.userId
    ) {
        mutableStateOf(
            ""
        )
    }

    var details by remember(
        session.userId
    ) {
        mutableStateOf<ProfileDetailsData?>(
            null
        )
    }

    var refreshToken by remember {
        mutableIntStateOf(
            0
        )
    }

    LaunchedEffect(
        session.userId,
        refreshToken
    ) {
        loading =
            true

        errorMessage =
            ""

        val result =
            ProfileDetailsApiService
                .loadProfileDetails(
                    session
                )

        loading =
            false

        if (
            result.success
        ) {
            details =
                result.data
        } else {
            errorMessage =
                result.message
        }
    }

    Scaffold(
        containerColor =
            Color(0xFFF4F7FC)
    ) {
            innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFEFF6FF),
                            Color(0xFFF8FAFC),
                            Color.White
                        )
                    )
                ),
            contentPadding =
                PaddingValues(
                    start =
                        18.dp,
                    top =
                        innerPadding
                            .calculateTopPadding() +
                                12.dp,
                    end =
                        18.dp,
                    bottom =
                        innerPadding
                            .calculateBottomPadding() +
                                28.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    15.dp
                )
        ) {
            item {
                ProfileDetailsTopBar(
                    onBack =
                        onBack,
                    onHome =
                        onHome,
                    onRefresh = {
                        refreshToken +=
                            1
                    }
                )
            }

            if (
                loading
            ) {
                item {
                    ProfileDetailsLoadingCard()
                }
            }

            if (
                errorMessage.isNotBlank()
            ) {
                item {
                    ProfileDetailsErrorCard(
                        message =
                            errorMessage,
                        onRetry = {
                            refreshToken +=
                                1
                        }
                    )
                }
            }

            details
                ?.let {
                        profile ->

                    item {
                        ProfileDetailsHero(
                            profile =
                                profile
                        )
                    }

                    item {
                        ProfileSummarySection(
                            summary =
                                profile.summary
                        )
                    }

                    item {
                        ProfilePersonalInfoCard(
                            profile =
                                profile
                        )
                    }

                    item {
                        ProfileAwardCollectionCard(
                            awardCounts =
                                profile.awardCounts
                        )
                    }

                    item {
                        ProfileSectionHeader(
                            title =
                                "Achievements",
                            subtitle =
                                "${profile.summary.unlockedAchievements} of " +
                                        "${profile.summary.totalAchievements} unlocked"
                        )
                    }

                    if (
                        profile.achievements
                            .isEmpty()
                    ) {
                        item {
                            ProfileEmptyCard(
                                text =
                                    "No achievement definitions were returned."
                            )
                        }
                    } else {
                        items(
                            items =
                                profile.achievements,
                            key = {
                                it.key
                            }
                        ) {
                                achievement ->

                            ProfileAchievementCard(
                                achievement =
                                    achievement
                            )
                        }
                    }

                    item {
                        ProfileSectionHeader(
                            title =
                                "Activity Breakdown",
                            subtitle =
                                "Finished workouts grouped by activity"
                        )
                    }

                    if (
                        profile.activityStats
                            .isEmpty()
                    ) {
                        item {
                            ProfileEmptyCard(
                                text =
                                    "Complete a workout to create activity statistics."
                            )
                        }
                    } else {
                        items(
                            items =
                                profile.activityStats,
                            key = {
                                it.activityName
                            }
                        ) {
                                stat ->

                            ProfileActivityStatCard(
                                stat =
                                    stat
                            )
                        }
                    }

                    item {
                        ProfileSectionHeader(
                            title =
                                "Finished Workouts",
                            subtitle =
                                "${profile.workouts.size} recent saved workout" +
                                        if (
                                            profile.workouts.size ==
                                            1
                                        ) {
                                            ""
                                        } else {
                                            "s"
                                        }
                        )
                    }

                    if (
                        profile.workouts
                            .isEmpty()
                    ) {
                        item {
                            ProfileEmptyCard(
                                text =
                                    "No finished workouts yet. Saved workouts will appear here."
                            )
                        }
                    } else {
                        items(
                            items =
                                profile.workouts,
                            key = {
                                it.id
                            }
                        ) {
                                workout ->

                            ProfileFinishedWorkoutCard(
                                workout =
                                    workout
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun ProfileDetailsTopBar(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        TextButton(
            onClick =
                onBack
        ) {
            Text(
                text =
                    "‹ Back",
                fontWeight =
                    FontWeight.Bold
            )
        }

        Column(
            modifier =
                Modifier.weight(
                    1f
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    "Profile Details",
                color =
                    Color(0xFF0F172A),
                fontSize =
                    22.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    "Workouts and achievements",
                color =
                    Color(0xFF64748B),
                fontSize =
                    10.sp
            )
        }

        TextButton(
            onClick =
                onRefresh
        ) {
            Text(
                text =
                    "Refresh",
                fontWeight =
                    FontWeight.Bold
            )
        }

        TextButton(
            onClick =
                onHome
        ) {
            Text(
                text =
                    "Home",
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileDetailsLoadingCard() {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    18.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(
                        25.dp
                    ),
                color =
                    Color(0xFF2563EB),
                strokeWidth =
                    3.dp
            )

            Text(
                text =
                    "Loading finished workouts and achievements...",
                color =
                    Color(0xFF475569),
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileDetailsErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFFFFF1F2)
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    17.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            Text(
                text =
                    "Could not load Profile Details",
                color =
                    Color(0xFFBE123C),
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    message,
                color =
                    Color(0xFF9F1239),
                fontSize =
                    12.sp
            )

            Button(
                onClick =
                    onRetry
            ) {
                Text(
                    text =
                        "Try Again"
                )
            }
        }
    }
}

@Composable
private fun ProfileDetailsHero(
    profile: ProfileDetailsData
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
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1D4ED8),
                            Color(0xFF38BDF8)
                        )
                    )
                )
                .padding(
                    21.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    13.dp
                )
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(
                            66.dp
                        )
                        .clip(
                            CircleShape
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
                            profile.fullName
                                .trim()
                                .firstOrNull()
                                ?.uppercase()
                                ?: "U",
                        color =
                            Color.White,
                        fontSize =
                            27.sp,
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
                                14.dp
                        )
                ) {
                    Text(
                        text =
                            profile.fullName
                                .ifBlank {
                                    "MoveMate User"
                                },
                        color =
                            Color.White,
                        fontSize =
                            24.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            profile.email,
                        color =
                            Color.White.copy(
                                alpha =
                                    0.78f
                            ),
                        fontSize =
                            12.sp
                    )
                }

                Text(
                    text =
                        "🏆",
                    fontSize =
                        32.sp
                )
            }

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                ProfileHeroChip(
                    text =
                        "${profile.summary.totalWorkouts} workouts"
                )

                ProfileHeroChip(
                    text =
                        "${profile.summary.activeDays} active days"
                )

                ProfileHeroChip(
                    text =
                        "${profile.summary.unlockedAchievements} achievements"
                )
            }
        }
    }
}

@Composable
private fun ProfileHeroChip(
    text: String
) {
    Surface(
        shape =
            RoundedCornerShape(
                999.dp
            ),
        color =
            Color.White.copy(
                alpha =
                    0.15f
            )
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
            modifier =
                Modifier.padding(
                    horizontal =
                        9.dp,
                    vertical =
                        7.dp
                )
        )
    }
}

@Composable
private fun ProfileSummarySection(
    summary: ProfileWorkoutSummary
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {
        ProfileSectionHeader(
            title =
                "Lifetime Summary",
            subtitle =
                "All finished workouts saved in MySQL"
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            ProfileSummaryCard(
                title =
                    "Workouts",
                value =
                    summary.totalWorkouts
                        .toString(),
                emoji =
                    "✅",
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            ProfileSummaryCard(
                title =
                    "Active Days",
                value =
                    summary.activeDays
                        .toString(),
                emoji =
                    "📅",
                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            ProfileSummaryCard(
                title =
                    "Duration",
                value =
                    formatProfileDuration(
                        summary.totalDurationSeconds
                    ),
                emoji =
                    "⏱",
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            ProfileSummaryCard(
                title =
                    "Distance",
                value =
                    formatProfileDistance(
                        summary.totalDistanceKm
                    ),
                emoji =
                    "📍",
                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            ProfileSummaryCard(
                title =
                    "Calories",
                value =
                    String.format(
                        Locale.US,
                        "%.1f kcal",
                        summary.totalCalories
                    ),
                emoji =
                    "🔥",
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            ProfileSummaryCard(
                title =
                    "Best Session",
                value =
                    formatProfileDuration(
                        summary.longestWorkoutSeconds
                    ),
                emoji =
                    "⭐",
                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }
    }
}

@Composable
private fun ProfileSummaryCard(
    title: String,
    value: String,
    emoji: String,
    modifier: Modifier
) {
    Card(
        modifier =
            modifier,
        shape =
            RoundedCornerShape(
                20.dp
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
                    14.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    5.dp
                )
        ) {
            Text(
                text =
                    emoji,
                fontSize =
                    22.sp
            )

            Text(
                text =
                    title,
                color =
                    Color(0xFF64748B),
                fontSize =
                    10.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    value,
                color =
                    Color(0xFF0F172A),
                fontSize =
                    17.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ProfilePersonalInfoCard(
    profile: ProfileDetailsData
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                23.dp
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
                    17.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            Text(
                text =
                    "Personal Information",
                color =
                    Color(0xFF0F172A),
                fontSize =
                    18.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            ProfileInfoRow(
                label =
                    "Gender",
                value =
                    profile.gender
                        .ifBlank {
                            "Not set"
                        }
            )

            ProfileInfoRow(
                label =
                    "Age",
                value =
                    if (
                        profile.age >
                        0
                    ) {
                        "${profile.age} years"
                    } else {
                        "Not set"
                    }
            )

            ProfileInfoRow(
                label =
                    "Weight",
                value =
                    String.format(
                        Locale.US,
                        "%.1f kg",
                        profile.weightKg
                    )
            )

            ProfileInfoRow(
                label =
                    "Height",
                value =
                    String.format(
                        Locale.US,
                        "%.0f cm",
                        profile.heightCm
                    )
            )

            ProfileInfoRow(
                label =
                    "First workout",
                value =
                    formatProfileDate(
                        profile.summary.firstWorkoutAt
                    )
            )

            ProfileInfoRow(
                label =
                    "Latest workout",
                value =
                    formatProfileDate(
                        profile.summary.latestWorkoutAt
                    )
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text =
                label,
            color =
                Color(0xFF64748B),
            fontSize =
                12.sp,
            modifier =
                Modifier.weight(
                    1f
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
                FontWeight.Bold,
            textAlign =
                TextAlign.End
        )
    }
}

@Composable
private fun ProfileAwardCollectionCard(
    awardCounts: ProfileAwardCounts
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                23.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFFFFFBEB)
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    17.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {
            Text(
                text =
                    "Award Collection",
                color =
                    Color(0xFF78350F),
                fontSize =
                    18.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                ProfileAwardCount(
                    emoji =
                        "🌱",
                    title =
                        "Starter",
                    count =
                        awardCounts.starter
                )

                ProfileAwardCount(
                    emoji =
                        "🥈",
                    title =
                        "Silver",
                    count =
                        awardCounts.silver
                )

                ProfileAwardCount(
                    emoji =
                        "🥇",
                    title =
                        "Gold",
                    count =
                        awardCounts.gold
                )

                ProfileAwardCount(
                    emoji =
                        "💎",
                    title =
                        "Diamond",
                    count =
                        awardCounts.diamond
                )
            }
        }
    }
}

@Composable
private fun ProfileAwardCount(
    emoji: String,
    title: String,
    count: Int
) {
    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                emoji,
            fontSize =
                25.sp
        )

        Text(
            text =
                count.toString(),
            color =
                Color(0xFF78350F),
            fontSize =
                18.sp,
            fontWeight =
                FontWeight.ExtraBold
        )

        Text(
            text =
                title,
            color =
                Color(0xFF92400E),
            fontSize =
                8.sp,
            fontWeight =
                FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileSectionHeader(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text =
                title,
            color =
                Color(0xFF0F172A),
            fontSize =
                20.sp,
            fontWeight =
                FontWeight.ExtraBold
        )

        Text(
            text =
                subtitle,
            color =
                Color(0xFF64748B),
            fontSize =
                11.sp,
            modifier =
                Modifier.padding(
                    top =
                        2.dp
                )
        )
    }
}

@Composable
private fun ProfileAchievementCard(
    achievement: ProfileAchievementItem
) {
    val cardColor =
        if (
            achievement.unlocked
        ) {
            Color(0xFFF0FDF4)
        } else {
            Color.White
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                21.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    cardColor
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    if (
                        achievement.unlocked
                    ) {
                        3.dp
                    } else {
                        1.dp
                    }
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    15.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(
                        50.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        if (
                            achievement.unlocked
                        ) {
                            Color(0xFFDCFCE7)
                        } else {
                            Color(0xFFF1F5F9)
                        }
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        if (
                            achievement.unlocked
                        ) {
                            achievement.emoji
                        } else {
                            "🔒"
                        },
                    fontSize =
                        25.sp
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
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        5.dp
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
                            achievement.title,
                        color =
                            Color(0xFF0F172A),
                        fontSize =
                            15.sp,
                        fontWeight =
                            FontWeight.ExtraBold,
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    Text(
                        text =
                            if (
                                achievement.unlocked
                            ) {
                                "UNLOCKED"
                            } else {
                                achievement.tier
                            },
                        color =
                            if (
                                achievement.unlocked
                            ) {
                                Color(0xFF15803D)
                            } else {
                                Color(0xFF64748B)
                            },
                        fontSize =
                            8.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }

                Text(
                    text =
                        achievement.description,
                    color =
                        Color(0xFF64748B),
                    fontSize =
                        11.sp
                )

                LinearProgressIndicator(
                    progress =
                        achievement.progressFraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            7.dp
                        ),
                    color =
                        if (
                            achievement.unlocked
                        ) {
                            Color(0xFF16A34A)
                        } else {
                            Color(0xFF2563EB)
                        },
                    trackColor =
                        Color(0xFFE2E8F0)
                )

                Text(
                    text =
                        String.format(
                            Locale.US,
                            "%.0f / %.0f",
                            achievement.progressValue
                                .coerceAtMost(
                                    achievement.targetValue
                                ),
                            achievement.targetValue
                        ),
                    color =
                        Color(0xFF94A3B8),
                    fontSize =
                        9.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileActivityStatCard(
    stat: ProfileActivityStat
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                20.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    15.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        profileActivityEmoji(
                            stat.activityName
                        ),
                    fontSize =
                        27.sp
                )

                Text(
                    text =
                        stat.activityName,
                    color =
                        Color(0xFF0F172A),
                    fontSize =
                        16.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    modifier =
                        Modifier.padding(
                            start =
                                10.dp
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
                        "${stat.workoutCount} workouts",
                    color =
                        Color(0xFF2563EB),
                    fontSize =
                        11.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            HorizontalDivider(
                color =
                    Color(0xFFE2E8F0)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                ProfileSmallMetric(
                    label =
                        "Time",
                    value =
                        formatProfileDuration(
                            stat.durationSeconds
                        )
                )

                ProfileSmallMetric(
                    label =
                        "Distance",
                    value =
                        formatProfileDistance(
                            stat.distanceKm
                        )
                )

                ProfileSmallMetric(
                    label =
                        "Calories",
                    value =
                        String.format(
                            Locale.US,
                            "%.1f",
                            stat.calories
                        )
                )
            }
        }
    }
}

@Composable
private fun ProfileSmallMetric(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                label,
            color =
                Color(0xFF94A3B8),
            fontSize =
                8.sp,
            fontWeight =
                FontWeight.Bold
        )

        Text(
            text =
                value,
            color =
                Color(0xFF0F172A),
            fontSize =
                11.sp,
            fontWeight =
                FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ProfileFinishedWorkoutCard(
    workout: ProfileFinishedWorkoutItem
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(
                            45.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                14.dp
                            )
                        )
                        .background(
                            Color(0xFFEFF6FF)
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            profileActivityEmoji(
                                workout.activityType
                            ),
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
                                11.dp
                        )
                ) {
                    Text(
                        text =
                            workout.workoutName
                                .ifBlank {
                                    workout.activityType
                                },
                        color =
                            Color(0xFF0F172A),
                        fontSize =
                            15.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            formatProfileDate(
                                workout.workoutAt
                            ),
                        color =
                            Color(0xFF64748B),
                        fontSize =
                            10.sp
                    )
                }

                Surface(
                    shape =
                        RoundedCornerShape(
                            999.dp
                        ),
                    color =
                        profileAwardColor(
                            workout.awardTier
                        )
                            .copy(
                                alpha =
                                    0.14f
                            )
                ) {
                    Text(
                        text =
                            profileAwardEmoji(
                                workout.awardTier
                            ) +
                                    " " +
                                    workout.awardTier
                                        .uppercase(
                                            Locale.US
                                        ),
                        color =
                            profileAwardColor(
                                workout.awardTier
                            ),
                        fontSize =
                            9.sp,
                        fontWeight =
                            FontWeight.ExtraBold,
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    9.dp,
                                vertical =
                                    6.dp
                            )
                    )
                }
            }

            HorizontalDivider(
                color =
                    Color(0xFFE2E8F0)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                ProfileSmallMetric(
                    label =
                        "Level",
                    value =
                        workout.levelName
                            .ifBlank {
                                workout.intensity
                            }
                )

                ProfileSmallMetric(
                    label =
                        "Time",
                    value =
                        formatProfileDuration(
                            workout.durationSeconds
                        )
                )

                ProfileSmallMetric(
                    label =
                        "Distance",
                    value =
                        formatProfileDistance(
                            workout.distanceKm
                        )
                )

                ProfileSmallMetric(
                    label =
                        "Calories",
                    value =
                        String.format(
                            Locale.US,
                            "%.1f",
                            workout.calories
                        )
                )
            }

            if (
                workout.notes.isNotBlank()
            ) {
                Text(
                    text =
                        workout.notes,
                    color =
                        Color(0xFF64748B),
                    fontSize =
                        10.sp,
                    lineHeight =
                        14.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileEmptyCard(
    text: String
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                20.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )
    ) {
        Text(
            text =
                text,
            color =
                Color(0xFF64748B),
            textAlign =
                TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    22.dp
                )
        )
    }
}

private fun formatProfileDuration(
    totalSeconds: Int
): String {
    val safeSeconds =
        totalSeconds.coerceAtLeast(
            0
        )

    val hours =
        safeSeconds /
                3600

    val minutes =
        (
                safeSeconds %
                        3600
                ) /
                60

    return when {
        hours >
                0 ->
            "${hours}h ${minutes}m"

        minutes >
                0 ->
            "${minutes}m"

        else ->
            "${safeSeconds}s"
    }
}

private fun formatProfileDistance(
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

private fun formatProfileDate(
    rawDate: String
): String {
    if (
        rawDate.isBlank()
    ) {
        return "No workout yet"
    }

    val inputFormats =
        listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
        )

    val parsedDate =
        inputFormats
            .firstNotNullOfOrNull {
                    pattern ->

                runCatching {
                    SimpleDateFormat(
                        pattern,
                        Locale.US
                    )
                        .apply {
                            isLenient =
                                false
                        }
                        .parse(
                            rawDate
                        )
                }
                    .getOrNull()
            }

    return if (
        parsedDate !=
        null
    ) {
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.US
        )
            .format(
                parsedDate
            )
    } else {
        rawDate
    }
}

private fun profileActivityEmoji(
    activityName: String
): String {
    return when {
        activityName.contains(
            "run",
            ignoreCase =
                true
        ) ->
            "🏃"

        activityName.contains(
            "walk",
            ignoreCase =
                true
        ) ->
            "🚶"

        activityName.contains(
            "cycl",
            ignoreCase =
                true
        ) ->
            "🚴"

        activityName.contains(
            "weight",
            ignoreCase =
                true
        ) ->
            "🏋️"

        activityName.contains(
            "yoga",
            ignoreCase =
                true
        ) ->
            "🧘"

        activityName.contains(
            "swim",
            ignoreCase =
                true
        ) ->
            "🏊"

        activityName.contains(
            "hiit",
            ignoreCase =
                true
        ) ->
            "⚡"

        else ->
            "💪"
    }
}

private fun profileAwardEmoji(
    awardTier: String
): String {
    return when (
        awardTier
            .trim()
            .uppercase(
                Locale.US
            )
    ) {
        "DIAMOND" ->
            "💎"

        "GOLD" ->
            "🥇"

        "SILVER" ->
            "🥈"

        else ->
            "🌱"
    }
}

private fun profileAwardColor(
    awardTier: String
): Color {
    return when (
        awardTier
            .trim()
            .uppercase(
                Locale.US
            )
    ) {
        "DIAMOND" ->
            Color(0xFF7C3AED)

        "GOLD" ->
            Color(0xFFD97706)

        "SILVER" ->
            Color(0xFF475569)

        else ->
            Color(0xFF16A34A)
    }
}
