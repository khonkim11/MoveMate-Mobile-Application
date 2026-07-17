package com.example.movemate

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationScreen(
    session: UserSession,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current

    var settings by remember(
        session.userId
    ) {
        mutableStateOf(
            loadMoveMateNotificationSettings(
                context,
                session.userId
            )
        )
    }

    var permissionGranted by remember {
        mutableStateOf(
            MoveMateNotificationManager
                .canPostNotifications(context)
        )
    }

    var saving by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
    }

    var isError by remember {
        mutableStateOf(false)
    }

    var notificationLog by remember(
        session.userId
    ) {
        mutableStateOf(
            loadMoveMateNotificationLog(
                context,
                session.userId
            )
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) { granted ->
            permissionGranted = granted

            message =
                if (granted) {
                    "Notification permission allowed."
                } else {
                    "Notification permission was not allowed."
                }

            isError = !granted
        }

    fun requestPermissionIfRequired() {
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            !permissionGranted
        ) {
            permissionLauncher.launch(
                Manifest.permission
                    .POST_NOTIFICATIONS
            )
        }
    }

    fun saveAndSchedule() {
        if (
            settings.enabled &&
            !permissionGranted
        ) {
            requestPermissionIfRequired()

            message =
                "Allow notification permission, then save again."
            isError = true
            return
        }

        saving = true

        saveMoveMateNotificationSettings(
            context,
            session.userId,
            settings
        )

        MoveMateNotificationManager
            .createChannels(context)

        MoveMateNotificationManager
            .scheduleAll(
                context,
                session.userId,
                settings
            )

        saving = false
        message =
            if (settings.enabled) {
                "Notification settings saved and reminders scheduled."
            } else {
                "Notifications disabled and scheduled reminders cancelled."
            }

        isError = false
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
                    onClick = onBack
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
                    text = "Notifications",
                    color = Color(0xFF0F172A),
                    fontSize = 22.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                Spacer(
                    modifier =
                        Modifier.size(55.dp)
                )
            }
        }

        item {
            NotificationStatusCard(
                enabled = settings.enabled,
                permissionGranted =
                    permissionGranted,
                onEnabledChange = {
                    settings =
                        settings.copy(
                            enabled = it
                        )

                    if (it) {
                        requestPermissionIfRequired()
                    }
                },
                onPermissionClick = {
                    requestPermissionIfRequired()
                },
                onSystemSettingsClick = {
                    context.startActivity(
                        Intent(
                            Settings
                                .ACTION_APP_NOTIFICATION_SETTINGS
                        ).apply {
                            putExtra(
                                Settings
                                    .EXTRA_APP_PACKAGE,
                                context.packageName
                            )
                        }
                    )
                }
            )
        }

        item {
            NotificationSectionCard(
                title = "Workout Reminder",
                subtitle =
                    "A daily prompt at your preferred workout time."
            ) {
                NotificationToggleRow(
                    title =
                        "Daily workout reminder",
                    subtitle =
                        "Remind me to start or continue my fitness plan",
                    checked =
                        settings
                            .workoutReminderEnabled,
                    enabled =
                        settings.enabled,
                    onCheckedChange = {
                        settings =
                            settings.copy(
                                workoutReminderEnabled =
                                    it
                            )
                    }
                )

                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            {
                                    _,
                                    selectedHour,
                                    selectedMinute ->

                                settings =
                                    settings.copy(
                                        workoutHour =
                                            selectedHour,
                                        workoutMinute =
                                            selectedMinute
                                    )
                            },
                            settings.workoutHour,
                            settings.workoutMinute,
                            false
                        ).show()
                    },
                    enabled =
                        settings.enabled &&
                                settings
                                    .workoutReminderEnabled,
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(15.dp)
                ) {
                    Text(
                        text =
                            "Reminder time: " +
                                    formatReminderTime(
                                        settings
                                            .workoutHour,
                                        settings
                                            .workoutMinute
                                    ),
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }

        item {
            NotificationSectionCard(
                title = "Fitness Reminders",
                subtitle =
                    "Helpful prompts for movement, hydration and progress."
            ) {
                NotificationToggleRow(
                    title =
                        "Hydration reminders",
                    subtitle =
                        "Approximately every 3 hours between 8 AM and 8 PM",
                    checked =
                        settings
                            .hydrationReminderEnabled,
                    enabled =
                        settings.enabled,
                    onCheckedChange = {
                        settings =
                            settings.copy(
                                hydrationReminderEnabled =
                                    it
                            )
                    }
                )

                NotificationToggleRow(
                    title =
                        "Evening inactivity reminder",
                    subtitle =
                        "At about 7 PM when no workout is completed today",
                    checked =
                        settings
                            .inactivityReminderEnabled,
                    enabled =
                        settings.enabled,
                    onCheckedChange = {
                        settings =
                            settings.copy(
                                inactivityReminderEnabled =
                                    it
                            )
                    }
                )

                NotificationToggleRow(
                    title =
                        "Weekly progress review",
                    subtitle =
                        "A Sunday reminder to open the Progress section",
                    checked =
                        settings
                            .weeklySummaryEnabled,
                    enabled =
                        settings.enabled,
                    onCheckedChange = {
                        settings =
                            settings.copy(
                                weeklySummaryEnabled =
                                    it
                            )
                    }
                )
            }
        }

        item {
            NotificationSectionCard(
                title = "Achievements",
                subtitle =
                    "Celebrate workout awards and completed calorie goals."
            ) {
                NotificationToggleRow(
                    title =
                        "Workout award alerts",
                    subtitle =
                        "Silver, Gold and Diamond award notifications",
                    checked =
                        settings
                            .achievementAlertsEnabled,
                    enabled =
                        settings.enabled,
                    onCheckedChange = {
                        settings =
                            settings.copy(
                                achievementAlertsEnabled =
                                    it
                            )
                    }
                )

                NotificationToggleRow(
                    title =
                        "Daily goal-complete alert",
                    subtitle =
                        "Notify me when today's workout-calorie goal is reached",
                    checked =
                        settings
                            .goalCompleteAlertsEnabled,
                    enabled =
                        settings.enabled,
                    onCheckedChange = {
                        settings =
                            settings.copy(
                                goalCompleteAlertsEnabled =
                                    it
                            )
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
                        fontWeight =
                            FontWeight.SemiBold,
                        modifier =
                            Modifier.padding(14.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    saveAndSchedule()
                },
                enabled = !saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape =
                    RoundedCornerShape(18.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF2563EB)
                    )
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text =
                            "Save Notification Settings",
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    if (!permissionGranted) {
                        requestPermissionIfRequired()
                        message =
                            "Allow notification permission before testing."
                        isError = true
                    } else {
                        MoveMateNotificationManager
                            .postTestNotification(
                                context,
                                session.userId
                            )

                        notificationLog =
                            loadMoveMateNotificationLog(
                                context,
                                session.userId
                            )

                        message =
                            "Test notification sent."
                        isError = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape =
                    RoundedCornerShape(17.dp)
            ) {
                Text(
                    text = "Send Test Notification",
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        item {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Notifications",
                    color = Color(0xFF0F172A),
                    fontSize = 19.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    modifier =
                        Modifier.weight(1f)
                )

                if (notificationLog.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            clearMoveMateNotificationLog(
                                context,
                                session.userId
                            )

                            notificationLog =
                                emptyList()
                        }
                    ) {
                        Text("Clear")
                    }
                }
            }
        }

        if (notificationLog.isEmpty()) {
            item {
                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(20.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color.White
                        )
                ) {
                    Text(
                        text =
                            "No notifications have been sent yet.",
                        color = Color(0xFF64748B),
                        textAlign =
                            TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    )
                }
            }
        } else {
            items(
                items = notificationLog,
                key = {
                    it.id
                }
            ) { entry ->
                NotificationLogCard(entry)
            }
        }

        item {
            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )
        }
    }
}

@Composable
private fun NotificationStatusCard(
    enabled: Boolean,
    permissionGranted: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onPermissionClick: () -> Unit,
    onSystemSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (
                    enabled &&
                    permissionGranted
                ) {
                    Color(0xFFEAF7EE)
                } else {
                    Color.White
                }
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement =
                Arrangement.spacedBy(11.dp)
        ) {
            NotificationToggleRow(
                title =
                    "MoveMate notifications",
                subtitle =
                    if (permissionGranted) {
                        "Permission is available"
                    } else {
                        "Android notification permission is required"
                    },
                checked = enabled,
                enabled = true,
                onCheckedChange =
                    onEnabledChange
            )

            if (!permissionGranted) {
                Button(
                    onClick =
                        onPermissionClick,
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(15.dp)
                ) {
                    Text(
                        text =
                            "Allow Notifications",
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            OutlinedButton(
                onClick =
                    onSystemSettingsClick,
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(15.dp)
            ) {
                Text(
                    text =
                        "Open Android Notification Settings"
                )
            }
        }
    }
}

@Composable
private fun NotificationSectionCard(
    title: String,
    subtitle: String,
    content:
    @Composable
    androidx.compose.foundation.layout
    .ColumnScope.() -> Unit
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
                Arrangement.spacedBy(10.dp)
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
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color =
                    if (enabled) {
                        Color(0xFF0F172A)
                    } else {
                        Color(0xFF94A3B8)
                    },
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
                onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun NotificationLogCard(
    entry: MoveMateNotificationLogEntry
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = entry.title,
                color = Color(0xFF0F172A),
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text = entry.message,
                color = Color(0xFF475569),
                fontSize = 12.sp
            )

            Text(
                text = formatNotificationDate(
                    entry.timestampMillis
                ),
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
    }
}

private fun formatReminderTime(
    hour: Int,
    minute: Int
): String {
    val calendar =
        java.util.Calendar.getInstance()
            .apply {
                set(
                    java.util.Calendar
                        .HOUR_OF_DAY,
                    hour
                )
                set(
                    java.util.Calendar
                        .MINUTE,
                    minute
                )
            }

    return SimpleDateFormat(
        "hh:mm a",
        Locale.US
    ).format(calendar.time)
}

private fun formatNotificationDate(
    timestampMillis: Long
): String {
    if (timestampMillis <= 0L) {
        return ""
    }

    return SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.US
    ).format(
        Date(timestampMillis)
    )
}