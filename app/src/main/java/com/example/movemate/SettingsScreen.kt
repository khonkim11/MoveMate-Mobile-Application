package com.example.movemate

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun SettingsScreen(
    session: UserSession,
    onBack: () -> Unit,
    onSessionUpdated: (UserSession) -> Unit,
    onLogout: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context =
        androidx.compose.ui.platform.LocalContext.current

    val scope = rememberCoroutineScope()

    var imageUriString by remember(session.userId) {
        mutableStateOf(
            getSavedProfileImageUri(
                context,
                session.userId
            )
        )
    }

    var fullName by remember(session.userId, session.fullName) {
        mutableStateOf(session.fullName)
    }

    var gender by remember(session.userId, session.gender) {
        mutableStateOf(
            session.gender.ifBlank {
                "Prefer not to say"
            }
        )
    }

    var ageText by remember(session.userId, session.age) {
        mutableStateOf(
            if (session.age > 0) {
                session.age.toString()
            } else {
                ""
            }
        )
    }


    var currentPassword by remember {
        mutableStateOf("")
    }

    var newPassword by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var showCurrentPassword by remember {
        mutableStateOf(false)
    }

    var showNewPassword by remember {
        mutableStateOf(false)
    }

    var appSettings by remember(session.userId) {
        mutableStateOf(
            loadFitnessAppSettings(
                context,
                session.userId
            )
        )
    }

    val measurementInput =
        rememberMeasurementProfileInputState(
            userId = session.userId,
            initialSystem =
                if (
                    appSettings.useMetricUnits
                ) {
                    MeasurementSystem.METRIC
                } else {
                    MeasurementSystem.IMPERIAL
                },
            initialWeightKg =
                session.weightKg,
            initialHeightCm =
                session.heightCm
        )

    var saving by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
    }

    var isError by remember {
        mutableStateOf(false)
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { selectedUri: Uri? ->
        if (selectedUri != null) {
            try {
                context.contentResolver
                    .takePersistableUriPermission(
                        selectedUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
            } catch (_: SecurityException) {
                // Some providers already grant sufficient access.
            }

            imageUriString = selectedUri.toString()

            saveProfileImageUri(
                context = context,
                userId = session.userId,
                uriString = imageUriString
            )

            message = "Profile image updated on this device."
            isError = false
        }
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
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text(
                        text = "‹ Back",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Settings",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(modifier = Modifier.size(62.dp))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 7.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MoveMateProfileAvatar(
                        userId = session.userId,
                        name = fullName,
                        email = session.email,
                        imageUriString = imageUriString,
                        size = 126.dp
                    )

                    Text(
                        text = fullName.ifBlank {
                            "MoveMate User"
                        },
                        color = Color(0xFF0F172A),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = session.email,
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                imagePicker.launch(
                                    arrayOf("image/*")
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Choose Photo",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                removeSavedProfileImage(
                                    context,
                                    session.userId
                                )

                                imageUriString = ""
                                message =
                                    "Profile image removed."
                                isError = false
                            },
                            enabled = imageUriString.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Remove",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text =
                            "The selected image remains available " +
                                    "after restarting the app.",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            SettingsSectionCard(
                title = "Account & Fitness Profile",
                subtitle =
                    "Update information used for BMI, goals and workout guidance."
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        message = ""
                    },
                    label = {
                        Text("Full Name")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = session.email,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text("Email")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Text(
                    text = "Gender",
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Male",
                        "Female",
                        "Other",
                        "Prefer not to say"
                    ).chunked(2).forEach { rowOptions ->
                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            rowOptions.forEach { option ->
                                FilterChip(
                                    selected = gender == option,
                                    onClick = {
                                        gender = option
                                    },
                                    label = {
                                        Text(option)
                                    },
                                    modifier =
                                        Modifier.weight(1f),
                                    colors =
                                        FilterChipDefaults
                                            .filterChipColors(
                                                selectedContainerColor =
                                                    Color(0xFFDBEAFE),
                                                selectedLabelColor =
                                                    Color(0xFF1D4ED8)
                                            )
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = ageText,
                    onValueChange = {
                        ageText =
                            it.filter(Char::isDigit)
                    },
                    label = {
                        Text("Age")
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),
                    shape =
                        RoundedCornerShape(16.dp)
                )

                MeasurementProfileFields(
                    state =
                        measurementInput,
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }
        }

        item {
            SettingsSectionCard(
                title = "Change Password",
                subtitle =
                    "Leave these fields empty when you do not want to change it."
            ) {
                PasswordSettingsField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                    },
                    label = "Current Password",
                    visible = showCurrentPassword,
                    onToggleVisibility = {
                        showCurrentPassword =
                            !showCurrentPassword
                    }
                )

                PasswordSettingsField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                    },
                    label = "New Password",
                    visible = showNewPassword,
                    onToggleVisibility = {
                        showNewPassword =
                            !showNewPassword
                    }
                )

                PasswordSettingsField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                    },
                    label = "Confirm New Password",
                    visible = showNewPassword,
                    onToggleVisibility = {
                        showNewPassword =
                            !showNewPassword
                    }
                )

                Text(
                    text =
                        "Use at least 6 characters and include a number.",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }
        }

        item {
            SettingsSectionCard(
                title = "Fitness Preferences",
                subtitle =
                    "These preferences are saved separately for this account."
            ) {
                Text(
                    text = "Measurement units",
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected =
                            appSettings.useMetricUnits,
                        onClick = {
                            measurementInput
                                .selectSystem(
                                    MeasurementSystem.METRIC
                                )

                            appSettings =
                                appSettings.copy(
                                    useMetricUnits = true
                                )

                            saveMeasurementSystem(
                                context,
                                session.userId,
                                MeasurementSystem.METRIC
                            )

                            message =
                                "Metric units selected."
                            isError = false
                        },
                        label = {
                            Text("Metric (kg, cm, km)")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected =
                            !appSettings.useMetricUnits,
                        onClick = {
                            measurementInput
                                .selectSystem(
                                    MeasurementSystem.IMPERIAL
                                )

                            appSettings =
                                appSettings.copy(
                                    useMetricUnits = false
                                )

                            saveMeasurementSystem(
                                context,
                                session.userId,
                                MeasurementSystem.IMPERIAL
                            )

                            message =
                                "Imperial units selected."
                            isError = false
                        },
                        label = {
                            Text("Imperial")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                SettingsSwitchRow(
                    title = "Notifications",
                    subtitle =
                        "Allow workout and progress alerts",
                    checked =
                        appSettings.notificationsEnabled,
                    onCheckedChange = {
                        appSettings =
                            appSettings.copy(
                                notificationsEnabled = it
                            )
                    }
                )

                SettingsSwitchRow(
                    title = "Workout reminders",
                    subtitle =
                        "Save reminder preference for planned sessions",
                    checked =
                        appSettings.workoutRemindersEnabled,
                    onCheckedChange = {
                        appSettings =
                            appSettings.copy(
                                workoutRemindersEnabled = it
                            )
                    }
                )

                SettingsSwitchRow(
                    title = "Weekly progress summary",
                    subtitle =
                        "Show a weekly fitness recap preference",
                    checked =
                        appSettings.weeklySummaryEnabled,
                    onCheckedChange = {
                        appSettings =
                            appSettings.copy(
                                weeklySummaryEnabled = it
                            )
                    }
                )

                SettingsSwitchRow(
                    title = "Workout sounds",
                    subtitle =
                        "Enable timer and completion sounds",
                    checked =
                        appSettings.soundEnabled,
                    onCheckedChange = {
                        appSettings =
                            appSettings.copy(
                                soundEnabled = it
                            )
                    }
                )

                SettingsSwitchRow(
                    title = "Vibration",
                    subtitle =
                        "Enable workout control feedback",
                    checked =
                        appSettings.vibrationEnabled,
                    onCheckedChange = {
                        appSettings =
                            appSettings.copy(
                                vibrationEnabled = it
                            )
                    }
                )
            }
        }

        if (message.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
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
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val metricWeightKg =
                        measurementInput
                            .weightKgOrNull()

                    val metricHeightCm =
                        measurementInput
                            .heightCmOrNull()

                    val validation =
                        validateSettingsInput(
                            fullName = fullName,
                            ageText = ageText,
                            weightKg =
                                metricWeightKg,
                            heightCm =
                                metricHeightCm,
                            currentPassword =
                                currentPassword,
                            newPassword = newPassword,
                            confirmPassword =
                                confirmPassword
                        )

                    if (validation != null) {
                        message = validation
                        isError = true
                        return@Button
                    }

                    val age =
                        ageText.toIntOrNull() ?: 0

                    /*
                     * Always send metric values to PHP/MySQL.
                     * Imperial input is converted before saving.
                     */
                    val weight =
                        metricWeightKg ?: 0.0

                    val height =
                        metricHeightCm ?: 0.0

                    saving = true
                    message = ""

                    scope.launch {
                        val response =
                            SettingsApiService
                                .updateAccountSettings(
                                    userId =
                                        session.userId,
                                    fullName =
                                        fullName,
                                    gender =
                                        gender,
                                    age =
                                        age,
                                    weightKg =
                                        weight,
                                    heightCm =
                                        height,
                                    currentPassword =
                                        currentPassword,
                                    newPassword =
                                        newPassword
                                )

                        saving = false

                        if (
                            response.optBoolean(
                                "success",
                                false
                            )
                        ) {
                            val updatedSession =
                                response
                                    .optJSONObject("user")
                                    .toSettingsSession(
                                        fallback = session
                                    )

                            saveSession(
                                context,
                                updatedSession
                            )

                            saveFitnessAppSettings(
                                context = context,
                                userId =
                                    updatedSession.userId,
                                settings = appSettings
                            )

                            onSessionUpdated(
                                updatedSession
                            )

                            fullName =
                                updatedSession.fullName

                            measurementInput
                                .updateFromMetric(
                                    weightKg =
                                        updatedSession
                                            .weightKg,
                                    heightCm =
                                        updatedSession
                                            .heightCm
                                )

                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""

                            message =
                                response.optString(
                                    "message",
                                    "Settings saved successfully."
                                )

                            isError = false
                        } else {
                            message =
                                response.optString(
                                    "message",
                                    "Could not save settings."
                                )

                            isError = true
                        }
                    }
                },
                enabled = !saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    disabledContainerColor =
                        Color(0xFF93C5FD)
                )
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(23.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save All Settings",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "Log Out",
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
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
private fun SettingsSwitchRow(
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
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun PasswordSettingsField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation =
            if (visible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
        trailingIcon = {
            TextButton(
                onClick = onToggleVisibility
            ) {
                Text(
                    text =
                        if (visible) {
                            "Hide"
                        } else {
                            "Show"
                        }
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

private fun validateSettingsInput(
    fullName: String,
    ageText: String,
    weightKg: Double?,
    heightCm: Double?,
    currentPassword: String,
    newPassword: String,
    confirmPassword: String
): String? {
    if (fullName.trim().length < 2) {
        return "Please enter a valid full name."
    }

    val age = ageText.toIntOrNull()

    if (age == null || age !in 13..100) {
        return "Age must be between 13 and 100."
    }

    if (
        weightKg == null ||
        weightKg !in 20.0..350.0
    ) {
        return "Enter a valid weight. Accepted range: 20–350 kg or 44–772 lb."
    }

    if (
        heightCm == null ||
        heightCm !in 80.0..250.0
    ) {
        return "Enter a valid height. Accepted range: 80–250 cm."
    }

    val changingPassword =
        currentPassword.isNotBlank() ||
                newPassword.isNotBlank() ||
                confirmPassword.isNotBlank()

    if (changingPassword) {
        if (currentPassword.isBlank()) {
            return "Enter your current password."
        }

        if (newPassword.length < 6) {
            return "New password must have at least 6 characters."
        }

        if (!newPassword.any(Char::isDigit)) {
            return "New password must include a number."
        }

        if (newPassword != confirmPassword) {
            return "New passwords do not match."
        }

        if (newPassword == currentPassword) {
            return "The new password must be different."
        }
    }

    return null
}

private fun String.settingsDecimalOnly(): String {
    var usedDecimalPoint = false

    return filter { character ->
        when {
            character.isDigit() -> true

            character == '.' &&
                    !usedDecimalPoint -> {
                usedDecimalPoint = true
                true
            }

            else -> false
        }
    }
}

private fun JSONObject?.toSettingsSession(
    fallback: UserSession
): UserSession {
    val json = this ?: JSONObject()

    return UserSession(
        userId = json.optInt(
            "id",
            fallback.userId
        ),
        fullName = json.optString(
            "full_name",
            fallback.fullName
        ),
        email = json.optString(
            "email",
            fallback.email
        ),
        gender = json.optString(
            "gender",
            fallback.gender
        ),
        age = json.optInt(
            "age",
            fallback.age
        ),
        weightKg = json.optDouble(
            "weight_kg",
            fallback.weightKg
        ),
        heightCm = json.optDouble(
            "height_cm",
            fallback.heightCm
        )
    )
}
