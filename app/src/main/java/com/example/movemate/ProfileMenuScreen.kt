package com.example.movemate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileMenuScreen(
    session: UserSession,
    onBack: () -> Unit,
    onGoals: () -> Unit,
    onHealth: () -> Unit,
    onProfileDetails: () -> Unit,
    onNotification: () -> Unit,
    onSettings: () -> Unit,
    onAccessibilitySettings: () -> Unit,
    onLogout: () -> Unit,
    onHome: () -> Unit = onBack,
    onActivity: () -> Unit = {},
    onSummary: () -> Unit = {},
    onProgress: () -> Unit = {}
) {
    BackHandler(
        onBack = onBack
    )

    val quickActions = listOf(
        ProfileActionItem("🎯", "Goals", "Targets and daily plans", Color(0xFF2563EB), onGoals),
        ProfileActionItem("🩺", "Health", "Weight and body records", Color(0xFF10B981), onHealth),
        ProfileActionItem("👤", "Profile", "Account information", Color(0xFF06B6D4), onProfileDetails),
        ProfileActionItem("🔔", "Notifications", "Workout alerts and reminders", Color(0xFFF59E0B), onNotification),
        ProfileActionItem("⚙️", "Settings", "Theme and app preferences", Color(0xFF8B5CF6), onSettings),
        ProfileActionItem("♿", "Accessibility", "Display and support options", Color(0xFFEC4899), onAccessibilitySettings)
    )

    Scaffold(
        containerColor = Color(0xFFF4F7FC),
        bottomBar = {
            ProfileFooterBar(
                onHome = onHome,
                onActivity = onActivity,
                onSummary = onSummary,
                onProgress = onProgress,
                onGoals = onGoals,
                onProfile = {}
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF8FBFF),
                            Color(0xFFF1F6FF),
                            Color(0xFFFFFFFF)
                        )
                    )
                ),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 14.dp,
                end = 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 22.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfileTopBar(
                    onBack = onBack,
                    onHome = onHome
                )
            }

            item {
                ProfileHeroCard(
                    session = session,
                    onProfileDetails = onProfileDetails
                )
            }

            item {
                ProfileHighlightsRow(
                    onGoals = onGoals,
                    onHealth = onHealth
                )
            }

            item {
                Text(
                    text = "Profile Actions",
                    color = Color(0xFF0F172A),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "All cards now use one matching rounded design for a cleaner profile page.",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    userScrollEnabled = false
                ) {
                    items(quickActions) { item ->
                        ProfileActionCard(item)
                    }
                }
            }

            item {
                SupportCard(
                    onSettings = onSettings,
                    onLogout = onLogout
                )
            }
        }
    }
}

private data class ProfileActionItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val onClick: () -> Unit
)

@Composable
private fun ProfileTopBar(
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundTopButton(
            symbol = "‹",
            label = "Back",
            container = Color.White,
            content = Color(0xFF2563EB),
            onClick = onBack
        )

        Text(
            text = "Profile",
            color = Color(0xFF0F172A),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )

        RoundTopButton(
            symbol = "⌂",
            label = "Home",
            container = Color(0xFFEAF2FF),
            content = Color(0xFF2563EB),
            onClick = onHome
        )
    }
}

@Composable
private fun RoundTopButton(
    symbol: String,
    label: String,
    container: Color,
    content: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(container)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                color = content,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Text(
            text = label,
            color = Color(0xFF64748B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileHeroCard(
    session: UserSession,
    onProfileDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF1D4ED8),
                            Color(0xFF2563EB),
                            Color(0xFF38BDF8)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👤",
                            fontSize = 30.sp
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = session.fullName
                                .trim()
                                .ifBlank {
                                    "MoveMate Member"
                                },
                            color = Color.White,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Manage your goals, health info, profile settings and account tools from one place.",
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroChip("⚖️", String.format("%.1f kg", session.weightKg))
                    HeroChip("📏", String.format("%.0f cm", session.heightCm))
                    HeroChip("🔥", "Active")
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onProfileDetails),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "Open Profile Details",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
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
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProfileHighlightsRow(
    onGoals: () -> Unit,
    onHealth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HighlightCard(
            modifier = Modifier.weight(1f),
            emoji = "🎯",
            title = "Goal Center",
            value = "Daily targets",
            subtitle = "Workout and calorie plans",
            accent = Color(0xFF2563EB),
            onClick = onGoals
        )

        HighlightCard(
            modifier = Modifier.weight(1f),
            emoji = "💚",
            title = "Health",
            value = "Measurements",
            subtitle = "Weight and profile values",
            accent = Color(0xFF10B981),
            onClick = onHealth
        )
    }
}

@Composable
private fun HighlightCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 20.sp)
            }

            Text(
                text = title,
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                color = Color(0xFF0F172A),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ProfileActionCard(item: ProfileActionItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(item.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, fontSize = 22.sp)
            }

            Text(
                text = item.title,
                color = Color(0xFF0F172A),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = item.subtitle,
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f, fill = true))

            Text(
                text = "Open",
                color = item.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SupportCard(
    onSettings: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Account & Support",
                color = Color(0xFF0F172A),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Update your app preferences or log out safely. Everything now matches one rounded card style.",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            HorizontalDivider(color = Color(0xFFE2E8F0))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onSettings),
                color = Color(0xFFF8FAFC)
            ) {
                Text(
                    text = "Open Settings",
                    color = Color(0xFF334155),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 13.dp)
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onLogout),
                color = Color(0xFFFEF2F2)
            ) {
                Text(
                    text = "Logout",
                    color = Color(0xFFDC2626),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileFooterBar(
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onSummary: () -> Unit,
    onProgress: () -> Unit,
    onGoals: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 16.dp,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FooterNavItem("⌂", "Home", false, onHome)
            FooterNavItem("🏃", "Activity", false, onActivity)
            FooterNavItem("📋", "Summary", false, onSummary)
            FooterNavItem("📈", "Progress", false, onProgress)
            FooterNavItem("🎯", "Goal", false, onGoals)
            FooterNavItem("👤", "Profile", true, onProfile)
        }
    }
}

@Composable
private fun FooterNavItem(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (selected) Color(0xFFE8F0FF)
                    else Color(0xFFF8FAFC)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 16.sp)
        }

        Text(
            text = label,
            color = if (selected) Color(0xFF2563EB) else Color(0xFF64748B),
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
