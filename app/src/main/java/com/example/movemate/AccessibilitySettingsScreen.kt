package com.example.movemate

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccessibilitySettingsScreen(
    session: UserSession,
    onBack: () -> Unit,
    onStartWorkout: () -> Unit,
    onOpenHistory: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current

    val measurementSystem =
        LocalMeasurementSystem.current

    var summaryLoading by remember(
        session.userId
    ) {
        mutableStateOf(true)
    }

    var summaryError by remember(
        session.userId
    ) {
        mutableStateOf("")
    }

    var todayWorkouts by remember(
        session.userId
    ) {
        mutableStateOf(0)
    }

    var todayCalories by remember(
        session.userId
    ) {
        mutableStateOf(0.0)
    }

    var todayDurationSeconds by remember(
        session.userId
    ) {
        mutableStateOf(0)
    }

    var todayDistanceKm by remember(
        session.userId
    ) {
        mutableStateOf(0.0)
    }

    var draftSettings by remember(
        session.userId
    ) {
        mutableStateOf(
            loadMoveMateAccessibilitySettings(
                context,
                session.userId
            )
        )
    }

    var message by remember {
        mutableStateOf("")
    }

    var isError by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(session.userId) {
        summaryLoading = true
        summaryError = ""

        val summaryResponse =
            WorkoutApiService.getTodaySummary(
                session.userId
            )

        if (
            summaryResponse.optBoolean(
                "success",
                false
            )
        ) {
            todayWorkouts =
                summaryResponse.optInt(
                    "total_workouts",
                    0
                )

            todayCalories =
                summaryResponse.optDouble(
                    "total_calories",
                    0.0
                )

            todayDurationSeconds =
                summaryResponse.optInt(
                    "total_duration_seconds",
                    0
                )

            todayDistanceKm =
                summaryResponse.optDouble(
                    "total_distance_km",
                    0.0
                )
        } else {
            summaryError =
                summaryResponse.optString(
                    "message",
                    "Could not load today's summary."
                )
        }

        summaryLoading = false
    }

    fun saveDraftSettings() {
        saveMoveMateAccessibilitySettings(
            context = context,
            userId = session.userId,
            settings = draftSettings
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFEFF6FF),
                        Color.White
                    )
                )
            ),
        contentPadding =
            PaddingValues(18.dp),
        verticalArrangement =
            Arrangement.spacedBy(15.dp)
    ) {
        item {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.semantics {
                        contentDescription =
                            "Return to profile"
                        role = Role.Button
                    }
                ) {
                    Text(
                        text = "‹ Back",
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                Text(
                    text =
                        "Accessibility Settings",
                    color = Color(0xFF0F172A),
                    fontSize = 21.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    textAlign =
                        TextAlign.Center
                )

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                Box(
                    modifier =
                        Modifier.size(55.dp)
                )
            }
        }

        item {
            AccessibilitySectionCard(
                title = "Text Size",
                subtitle =
                    "Increase text without changing saved fitness data."
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    AccessibilityTextSize
                        .values()
                        .forEach { option ->
                            FilterChip(
                                selected =
                                    draftSettings
                                        .textSize ==
                                            option,
                                onClick = {
                                    draftSettings =
                                        draftSettings.copy(
                                            textSize =
                                                option
                                        )
                                },
                                label = {
                                    Text(option.label)
                                },
                                modifier =
                                    Modifier.weight(1f)
                            )
                        }
                }
            }
        }

        item {
            AccessibilitySectionCard(
                title = "Visual Assistance",
                subtitle =
                    "Improve contrast and readability."
            ) {
                AccessibilityToggleRow(
                    title =
                        "High contrast theme",
                    subtitle =
                        "Use stronger foreground and background contrast",
                    checked =
                        draftSettings
                            .highContrast,
                    onCheckedChange = {
                        draftSettings =
                            draftSettings.copy(
                                highContrast = it
                            )
                    }
                )

                AccessibilityToggleRow(
                    title = "Bold text",
                    subtitle =
                        "Use stronger text weight throughout MoveMate",
                    checked =
                        draftSettings.boldText,
                    onCheckedChange = {
                        draftSettings =
                            draftSettings.copy(
                                boldText = it
                            )
                    }
                )

                AccessibilityToggleRow(
                    title =
                        "Larger controls",
                    subtitle =
                        "Increase important button and switch heights",
                    checked =
                        draftSettings
                            .largerControls,
                    onCheckedChange = {
                        draftSettings =
                            draftSettings.copy(
                                largerControls = it
                            )
                    }
                )
            }
        }

        item {
            AccessibilitySectionCard(
                title = "Motion & Feedback",
                subtitle =
                    "Reduce movement and control physical feedback."
            ) {
                AccessibilityToggleRow(
                    title = "Reduce motion",
                    subtitle =
                        "Replace smooth movement with immediate state changes",
                    checked =
                        draftSettings
                            .reduceMotion,
                    onCheckedChange = {
                        draftSettings =
                            draftSettings.copy(
                                reduceMotion = it
                            )
                    }
                )

                AccessibilityToggleRow(
                    title = "Haptic feedback",
                    subtitle =
                        "Provide vibration feedback for important workout actions",
                    checked =
                        draftSettings
                            .hapticFeedback,
                    onCheckedChange = {
                        draftSettings =
                            draftSettings.copy(
                                hapticFeedback = it
                            )
                    }
                )
            }
        }

        item {
            MoveMateAccessibilityProvider(
                settings = draftSettings
            ) {
                AccessibilityPreviewCard(
                    settings =
                        draftSettings,
                    loading =
                        summaryLoading,
                    errorMessage =
                        summaryError,
                    workoutCount =
                        todayWorkouts,
                    calories =
                        todayCalories,
                    durationSeconds =
                        todayDurationSeconds,
                    distanceKm =
                        todayDistanceKm,
                    measurementSystem =
                        measurementSystem,
                    onStart = {
                        saveDraftSettings()

                        onStartWorkout()
                    },
                    onHistory = {
                        saveDraftSettings()

                        onOpenHistory()
                    }
                )
            }
        }

        if (message.isNotBlank()) {
            item {
                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(18.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                if (isError) {
                                    Color(0xFFFFEBEE)
                                } else {
                                    Color(0xFFEAF7EE)
                                }
                        )
                ) {
                    Text(
                        text = message,
                        color =
                            if (isError) {
                                Color(0xFFB91C1C)
                            } else {
                                Color(0xFF15803D)
                            },
                        modifier =
                            Modifier.padding(14.dp),
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (session.userId <= 0) {
                        message =
                            "Invalid session. Please log in again."
                        isError = true
                    } else {
                        saveDraftSettings()

                        message =
                            "Accessibility settings saved and applied."
                        isError = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        if (
                            draftSettings
                                .largerControls
                        ) {
                            64.dp
                        } else {
                            56.dp
                        }
                    ),
                shape =
                    RoundedCornerShape(18.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (
                                draftSettings
                                    .highContrast
                            ) {
                                Color(0xFF0039A6)
                            } else {
                                Color(0xFF2563EB)
                            }
                    )
            ) {
                Text(
                    text =
                        "Save Accessibility Settings",
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    val defaults =
                        MoveMateAccessibilitySettings()

                    draftSettings = defaults

                    resetMoveMateAccessibilitySettings(
                        context = context,
                        userId = session.userId
                    )

                    message =
                        "Accessibility settings reset to defaults."
                    isError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape =
                    RoundedCornerShape(17.dp)
            ) {
                Text(
                    text = "Reset to Defaults",
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        item {
            Text(
                text =
                    "MoveMate keeps Android's system accessibility settings active. " +
                            "App-specific options add extra support and do not disable TalkBack, " +
                            "system font scaling, magnification or other device features.",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )
        }
    }
}

@Composable
private fun AccessibilitySectionCard(
    title: String,
    subtitle: String,
    content:
    @Composable
    ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement =
                Arrangement.spacedBy(11.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontSize = 18.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text = subtitle,
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )

            content()
        }
    }
}

@Composable
private fun AccessibilityToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(!checked)
            }
            .padding(vertical = 5.dp)
            .semantics {
                contentDescription =
                    "$title. $subtitle. " +
                            if (checked) {
                                "Enabled"
                            } else {
                                "Disabled"
                            }

                role = Role.Switch
            },
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange =
                onCheckedChange
        )
    }
}

@Composable
private fun AccessibilityPreviewCard(
    settings:
    MoveMateAccessibilitySettings,
    loading: Boolean,
    errorMessage: String,
    workoutCount: Int,
    calories: Double,
    durationSeconds: Int,
    distanceKm: Double,
    measurementSystem:
    MeasurementSystem,
    onStart: () -> Unit,
    onHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme
                    .surface
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 5.dp
            )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(11.dp)
        ) {
            Text(
                text = "Live Preview",
                color =
                    MaterialTheme.colorScheme
                        .onSurface,
                fontSize = 19.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            when {
                loading -> {
                    Text(
                        text =
                            "Loading today's fitness information...",
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                errorMessage.isNotBlank() -> {
                    Text(
                        text =
                            "Today's information is temporarily unavailable.",
                        color =
                            MaterialTheme.colorScheme
                                .error,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }

                workoutCount > 0 -> {
                    Text(
                        text =
                            "Today you completed " +
                                    "$workoutCount " +
                                    if (workoutCount == 1) {
                                        "workout"
                                    } else {
                                        "workouts"
                                    } +
                                    " and burned " +
                                    "${calories.toInt()} calories.",
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {
                        AccessibilityPreviewMetric(
                            title = "Active Time",
                            value =
                                formatWorkoutTime(
                                    durationSeconds
                                ),
                            modifier =
                                Modifier.weight(1f)
                        )

                        AccessibilityPreviewMetric(
                            title = "Distance",
                            value =
                                formatDistance(
                                    distanceKm,
                                    measurementSystem
                                ),
                            modifier =
                                Modifier.weight(1f)
                        )
                    }
                }

                else -> {
                    Text(
                        text =
                            "No workout is recorded today. " +
                                    "Start an activity or review your previous workouts.",
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .weight(1f)
                        .height(
                            if (
                                settings
                                    .largerControls
                            ) {
                                62.dp
                            } else {
                                50.dp
                            }
                        )
                        .semantics {
                            contentDescription =
                                "Start a new workout. Opens activity selection."
                            role = Role.Button
                        }
                ) {
                    Text("Start")
                }

                OutlinedButton(
                    onClick = onHistory,
                    modifier = Modifier
                        .weight(1f)
                        .height(
                            if (
                                settings
                                    .largerControls
                            ) {
                                62.dp
                            } else {
                                50.dp
                            }
                        )
                        .semantics {
                            contentDescription =
                                "Open workout history and review saved workouts."
                            role = Role.Button
                        }
                ) {
                    Text("History")
                }
            }

            Text(
                text =
                    if (settings.reduceMotion) {
                        "Reduced motion: enabled"
                    } else {
                        "Reduced motion: disabled"
                    },
                color =
                    MaterialTheme.colorScheme
                        .primary,
                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                text =
                    "Start and History are connected to the real MoveMate screens.",
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun AccessibilityPreviewMetric(
    title: String,
    value: String,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme
                    .surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement =
                Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
                fontSize = 10.sp
            )

            Text(
                text = value,
                color =
                    MaterialTheme.colorScheme
                        .onSurface,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}