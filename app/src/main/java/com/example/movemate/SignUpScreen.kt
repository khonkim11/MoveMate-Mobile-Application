package com.example.movemate

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SignUpScreen(
    onBack: () -> Unit,
    onRegistered: (UserSession) -> Unit
) {
    val scope =
        rememberCoroutineScope()

    var fullName by
    remember {
        mutableStateOf(
            ""
        )
    }

    var email by
    remember {
        mutableStateOf(
            ""
        )
    }

    var password by
    remember {
        mutableStateOf(
            ""
        )
    }

    var passwordVisible by
    remember {
        mutableStateOf(
            false
        )
    }

    var gender by
    remember {
        mutableStateOf(
            "Male"
        )
    }

    var age by
    remember {
        mutableStateOf(
            "21"
        )
    }

    var weightText by
    remember {
        mutableStateOf(
            ""
        )
    }

    var weightUnit by
    remember {
        mutableStateOf(
            "kg"
        )
    }

    var heightUnit by
    remember {
        mutableStateOf(
            "cm"
        )
    }

    var heightCmText by
    remember {
        mutableStateOf(
            ""
        )
    }

    var heightFeetText by
    remember {
        mutableStateOf(
            ""
        )
    }

    var heightInchesText by
    remember {
        mutableStateOf(
            ""
        )
    }

    var message by
    remember {
        mutableStateOf(
            ""
        )
    }

    var isError by
    remember {
        mutableStateOf(
            false
        )
    }

    var loading by
    remember {
        mutableStateOf(
            false
        )
    }

    var showContent by
    remember {
        mutableStateOf(
            false
        )
    }

    LaunchedEffect(
        Unit
    ) {
        showContent =
            true
    }

    val weightValue =
        weightText
            .toDoubleOrNull()
            ?: 0.0

    val weightKg =
        if (
            weightUnit ==
            "lb"
        ) {
            weightValue *
                    0.45359237
        } else {
            weightValue
        }

    val heightCmInput =
        heightCmText
            .toDoubleOrNull()
            ?: 0.0

    val heightFeet =
        heightFeetText
            .toIntOrNull()
            ?: 0

    val heightInches =
        heightInchesText
            .toIntOrNull()
            ?: 0

    val heightCm =
        if (
            heightUnit ==
            "cm"
        ) {
            heightCmInput
        } else {
            (
                    heightFeet *
                            30.48
                    ) +
                    (
                            heightInches *
                                    2.54
                            )
        }

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {
        MoveMateAuthBackground()

        AnimatedVisibility(
            visible =
                showContent,
            enter =
                fadeIn(
                    animationSpec =
                        tween(
                            550
                        )
                ) +
                        slideInVertically(
                            animationSpec =
                                tween(
                                    650
                                ),
                            initialOffsetY = {
                                it /
                                        6
                            }
                        )
        ) {
            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start =
                            18.dp,
                        top =
                            18.dp,
                        end =
                            18.dp,
                        bottom =
                            30.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        16.dp
                    )
            ) {
                item {
                    SignUpHeader(
                        loading =
                            loading,
                        onBack =
                            onBack
                    )
                }

                item {
                    SignUpFeatureStrip()
                }

                item {
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
                                    Color.White.copy(
                                        alpha =
                                            0.98f
                                    )
                            ),
                        elevation =
                            CardDefaults.cardElevation(
                                defaultElevation =
                                    12.dp
                            )
                    ) {
                        Column(
                            modifier =
                                Modifier.padding(
                                    19.dp
                                ),
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    14.dp
                                )
                        ) {
                            Text(
                                text =
                                    "Personal information",
                                color =
                                    Color(0xFF0F172A),
                                fontSize =
                                    20.sp,
                                fontWeight =
                                    FontWeight.ExtraBold
                            )

                            Text(
                                text =
                                    "Create the same profile used by workout, goals, calories and progress.",
                                color =
                                    Color(0xFF64748B),
                                fontSize =
                                    12.sp,
                                lineHeight =
                                    17.sp
                            )

                            OutlinedTextField(
                                value =
                                    fullName,
                                onValueChange = {
                                    fullName =
                                        it

                                    message =
                                        ""
                                },
                                label = {
                                    Text(
                                        "Full Name"
                                    )
                                },
                                leadingIcon = {
                                    MoveMateAuthLeadingIcon(
                                        "👤"
                                    )
                                },
                                modifier =
                                    Modifier.fillMaxWidth(),
                                singleLine =
                                    true,
                                enabled =
                                    !loading,
                                shape =
                                    RoundedCornerShape(
                                        18.dp
                                    )
                            )

                            OutlinedTextField(
                                value =
                                    email,
                                onValueChange = {
                                    email =
                                        it

                                    message =
                                        ""
                                },
                                label = {
                                    Text(
                                        "Email"
                                    )
                                },
                                leadingIcon = {
                                    MoveMateAuthLeadingIcon(
                                        "✉"
                                    )
                                },
                                modifier =
                                    Modifier.fillMaxWidth(),
                                singleLine =
                                    true,
                                enabled =
                                    !loading,
                                keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType =
                                            KeyboardType.Email
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        18.dp
                                    )
                            )

                            OutlinedTextField(
                                value =
                                    password,
                                onValueChange = {
                                    password =
                                        it

                                    message =
                                        ""
                                },
                                label = {
                                    Text(
                                        "Password"
                                    )
                                },
                                leadingIcon = {
                                    MoveMateAuthLeadingIcon(
                                        "🔐"
                                    )
                                },
                                trailingIcon = {
                                    MoveMatePasswordEyeToggle(
                                        visible =
                                            passwordVisible,
                                        enabled =
                                            !loading,
                                        onClick = {
                                            passwordVisible =
                                                !passwordVisible
                                        }
                                    )
                                },
                                modifier =
                                    Modifier.fillMaxWidth(),
                                singleLine =
                                    true,
                                enabled =
                                    !loading,
                                visualTransformation =
                                    if (
                                        passwordVisible
                                    ) {
                                        VisualTransformation.None
                                    } else {
                                        PasswordVisualTransformation()
                                    },
                                keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType =
                                            KeyboardType.Password
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        18.dp
                                    )
                            )

                            PasswordRequirementRow(
                                password =
                                    password
                            )

                            Text(
                                text =
                                    "Profile details",
                                color =
                                    Color(0xFF0F172A),
                                fontSize =
                                    17.sp,
                                fontWeight =
                                    FontWeight.ExtraBold,
                                modifier =
                                    Modifier.padding(
                                        top =
                                            4.dp
                                    )
                            )

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        10.dp
                                    )
                            ) {
                                AuthOptionDropdown(
                                    label =
                                        "Gender",
                                    selected =
                                        gender,
                                    options =
                                        listOf(
                                            "Male",
                                            "Female",
                                            "Other",
                                            "Prefer not to say"
                                        ),
                                    onSelected = {
                                        gender =
                                            it
                                    },
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                )

                                AuthOptionDropdown(
                                    label =
                                        "Age",
                                    selected =
                                        age,
                                    options =
                                        (
                                                13..80
                                                )
                                            .map(
                                                Int::toString
                                            ),
                                    onSelected = {
                                        age =
                                            it
                                    },
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                )
                            }

                            AuthUnitToggle(
                                title =
                                    "Weight Unit",
                                icon =
                                    "⚖",
                                first =
                                    "kg",
                                second =
                                    "lb",
                                selected =
                                    weightUnit,
                                onSelected = {
                                    weightUnit =
                                        it

                                    weightText =
                                        ""

                                    message =
                                        ""
                                }
                            )

                            OutlinedTextField(
                                value =
                                    weightText,
                                onValueChange = {
                                    weightText =
                                        it.decimalOnly()

                                    message =
                                        ""
                                },
                                label = {
                                    Text(
                                        "Weight ($weightUnit)"
                                    )
                                },
                                leadingIcon = {
                                    MoveMateAuthLeadingIcon(
                                        "⚖"
                                    )
                                },
                                modifier =
                                    Modifier.fillMaxWidth(),
                                singleLine =
                                    true,
                                enabled =
                                    !loading,
                                keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType =
                                            KeyboardType.Decimal
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        18.dp
                                    )
                            )

                            if (
                                weightKg >
                                0.0
                            ) {
                                ConversionLabel(
                                    text =
                                        "Saved as ${weightKg.roundToInt()} kg"
                                )
                            }

                            AuthUnitToggle(
                                title =
                                    "Height Unit",
                                icon =
                                    "↕",
                                first =
                                    "cm",
                                second =
                                    "ft/in",
                                selected =
                                    heightUnit,
                                onSelected = {
                                    heightUnit =
                                        it

                                    heightCmText =
                                        ""

                                    heightFeetText =
                                        ""

                                    heightInchesText =
                                        ""

                                    message =
                                        ""
                                }
                            )

                            if (
                                heightUnit ==
                                "cm"
                            ) {
                                OutlinedTextField(
                                    value =
                                        heightCmText,
                                    onValueChange = {
                                        heightCmText =
                                            it.decimalOnly()

                                        message =
                                            ""
                                    },
                                    label = {
                                        Text(
                                            "Height (cm)"
                                        )
                                    },
                                    leadingIcon = {
                                        MoveMateAuthLeadingIcon(
                                            "↕"
                                        )
                                    },
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                    singleLine =
                                        true,
                                    enabled =
                                        !loading,
                                    keyboardOptions =
                                        KeyboardOptions(
                                            keyboardType =
                                                KeyboardType.Decimal
                                        ),
                                    shape =
                                        RoundedCornerShape(
                                            18.dp
                                        )
                                )
                            } else {
                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(
                                            10.dp
                                        )
                                ) {
                                    OutlinedTextField(
                                        value =
                                            heightFeetText,
                                        onValueChange = {
                                            heightFeetText =
                                                it.filter(
                                                    Char::isDigit
                                                )

                                            message =
                                                ""
                                        },
                                        label = {
                                            Text(
                                                "Feet"
                                            )
                                        },
                                        modifier =
                                            Modifier.weight(
                                                1f
                                            ),
                                        singleLine =
                                            true,
                                        enabled =
                                            !loading,
                                        keyboardOptions =
                                            KeyboardOptions(
                                                keyboardType =
                                                    KeyboardType.Number
                                            ),
                                        shape =
                                            RoundedCornerShape(
                                                18.dp
                                            )
                                    )

                                    OutlinedTextField(
                                        value =
                                            heightInchesText,
                                        onValueChange = {
                                            heightInchesText =
                                                it.filter(
                                                    Char::isDigit
                                                )

                                            message =
                                                ""
                                        },
                                        label = {
                                            Text(
                                                "Inches"
                                            )
                                        },
                                        modifier =
                                            Modifier.weight(
                                                1f
                                            ),
                                        singleLine =
                                            true,
                                        enabled =
                                            !loading,
                                        keyboardOptions =
                                            KeyboardOptions(
                                                keyboardType =
                                                    KeyboardType.Number
                                            ),
                                        shape =
                                            RoundedCornerShape(
                                                18.dp
                                            )
                                    )
                                }
                            }

                            if (
                                heightCm >
                                0.0
                            ) {
                                ConversionLabel(
                                    text =
                                        "Saved as ${heightCm.roundToInt()} cm"
                                )
                            }

                            if (
                                message.isNotBlank()
                            ) {
                                SignUpMessageCard(
                                    message =
                                        message,
                                    isError =
                                        isError
                                )
                            }

                            Button(
                                onClick = {
                                    val validation =
                                        validateRegisterInput(
                                            fullName =
                                                fullName,
                                            email =
                                                email,
                                            password =
                                                password,
                                            age =
                                                age
                                                    .toIntOrNull()
                                                    ?: 0,
                                            weightKg =
                                                weightKg,
                                            heightCm =
                                                heightCm,
                                            heightUnit =
                                                heightUnit,
                                            heightFeet =
                                                heightFeet,
                                            heightInches =
                                                heightInches
                                        )

                                    if (
                                        validation !=
                                        null
                                    ) {
                                        message =
                                            validation

                                        isError =
                                            true

                                        return@Button
                                    }

                                    scope.launch {
                                        loading =
                                            true

                                        message =
                                            ""

                                        try {
                                            val saveWeightKg =
                                                weightKg
                                                    .roundToInt()
                                                    .toDouble()

                                            val saveHeightCm =
                                                heightCm
                                                    .roundToInt()
                                                    .toDouble()

                                            val response =
                                                withContext(
                                                    Dispatchers.IO
                                                ) {
                                                    ApiService.registerUser(
                                                        fullName.trim(),
                                                        email.trim(),
                                                        password,
                                                        gender,
                                                        age.toIntOrNull() ?: 0,
                                                        saveWeightKg,
                                                        saveHeightCm
                                                    )
                                                }

                                            if (
                                                response.optBoolean(
                                                    "success",
                                                    false
                                                )
                                            ) {
                                                val userSession =
                                                    buildUserSessionFromAuthResponse(
                                                        response =
                                                            response,
                                                        fallbackName =
                                                            fullName,
                                                        fallbackEmail =
                                                            email,
                                                        fallbackGender =
                                                            gender,
                                                        fallbackAge =
                                                            age.toInt(),
                                                        fallbackWeightKg =
                                                            weightKg,
                                                        fallbackHeightCm =
                                                            heightCm
                                                    )

                                                if (
                                                    userSession.userId <=
                                                    0
                                                ) {
                                                    message =
                                                        "Registration succeeded, but the server did not return a valid user ID."

                                                    isError =
                                                        true
                                                } else {
                                                    onRegistered(
                                                        userSession
                                                    )
                                                }
                                            } else {
                                                val baseMessage =
                                                    response.optString(
                                                        "message",
                                                        "Registration failed."
                                                    )

                                                val rawResponse =
                                                    response.optString(
                                                        "raw_response"
                                                    )

                                                val serverError =
                                                    response.optString(
                                                        "server_error"
                                                    )

                                                val url =
                                                    response.optString(
                                                        "url"
                                                    )

                                                message =
                                                    buildString {
                                                        append(
                                                            baseMessage
                                                        )

                                                        if (
                                                            serverError.isNotBlank()
                                                        ) {
                                                            append(
                                                                "\nServer: "
                                                            )

                                                            append(
                                                                serverError
                                                            )
                                                        }

                                                        if (
                                                            rawResponse.isNotBlank()
                                                        ) {
                                                            append(
                                                                "\nResponse: "
                                                            )

                                                            append(
                                                                rawResponse.take(
                                                                    350
                                                                )
                                                            )
                                                        }

                                                        if (
                                                            url.isNotBlank()
                                                        ) {
                                                            append(
                                                                "\nURL: "
                                                            )

                                                            append(
                                                                url
                                                            )
                                                        }
                                                    }

                                                isError =
                                                    true
                                            }
                                        } catch (
                                            exception: Exception
                                        ) {
                                            message =
                                                "Sign up failed: " +
                                                        (
                                                                exception.message
                                                                    ?: exception
                                                                        .javaClass
                                                                        .simpleName
                                                                )

                                            isError =
                                                true
                                        } finally {
                                            loading =
                                                false
                                        }
                                    }
                                },
                                enabled =
                                    !loading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(
                                        58.dp
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        19.dp
                                    ),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            Color(0xFF2563EB),
                                        disabledContainerColor =
                                            Color(0xFF93C5FD)
                                    )
                            ) {
                                if (
                                    loading
                                ) {
                                    CircularProgressIndicator(
                                        modifier =
                                            Modifier.size(
                                                23.dp
                                            ),
                                        color =
                                            Color.White,
                                        strokeWidth =
                                            2.dp
                                    )
                                } else {
                                    Text(
                                        text =
                                            "Create MoveMate Account",
                                        fontSize =
                                            16.sp,
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
    }
}

@Composable
private fun SignUpHeader(
    loading: Boolean,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(
                    46.dp
                )
                .background(
                    color =
                        Color.White.copy(
                            alpha =
                                0.14f
                        ),
                    shape =
                        RoundedCornerShape(
                            16.dp
                        )
                )
                .clickable(
                    enabled =
                        !loading,
                    onClick =
                        onBack
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    "←",
                color =
                    Color.White,
                fontSize =
                    22.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }

        Column(
            modifier =
                Modifier.padding(
                    start =
                        13.dp
                )
        ) {
            Text(
                text =
                    "Create Account",
                color =
                    Color.White,
                fontSize =
                    29.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    "One profile for every MoveMate feature",
                color =
                    Color.White.copy(
                        alpha =
                            0.82f
                    ),
                fontSize =
                    12.sp
            )
        }
    }
}

@Composable
private fun SignUpFeatureStrip() {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        listOf(
            "🏃 Workouts",
            "🎯 Goals",
            "📈 Progress"
        )
            .forEach {
                    feature ->

                Text(
                    text =
                        feature,
                    color =
                        Color.White,
                    fontSize =
                        10.sp,
                    fontWeight =
                        FontWeight.Bold,
                    modifier = Modifier
                        .weight(
                            1f
                        )
                        .background(
                            color =
                                Color.White.copy(
                                    alpha =
                                        0.13f
                                ),
                            shape =
                                RoundedCornerShape(
                                    999.dp
                                )
                        )
                        .padding(
                            horizontal =
                                7.dp,
                            vertical =
                                9.dp
                        )
                )
            }
    }
}

@Composable
private fun PasswordRequirementRow(
    password: String
) {
    val lengthReady =
        password.length >=
                6

    val numberReady =
        password.any(
            Char::isDigit
        )

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        PasswordRequirementChip(
            modifier =
                Modifier.weight(
                    1f
                ),
            text =
                "6+ characters",
            complete =
                lengthReady
        )

        PasswordRequirementChip(
            modifier =
                Modifier.weight(
                    1f
                ),
            text =
                "Contains number",
            complete =
                numberReady
        )
    }
}

@Composable
private fun PasswordRequirementChip(
    modifier: Modifier,
    text: String,
    complete: Boolean
) {
    Text(
        text =
            (
                    if (
                        complete
                    ) {
                        "✓ "
                    } else {
                        "○ "
                    }
                    ) +
                    text,
        color =
            if (
                complete
            ) {
                Color(0xFF15803D)
            } else {
                Color(0xFF64748B)
            },
        fontSize =
            10.sp,
        fontWeight =
            FontWeight.Bold,
        modifier =
            modifier
                .background(
                    color =
                        if (
                            complete
                        ) {
                            Color(0xFFEAF7EE)
                        } else {
                            Color(0xFFF1F5F9)
                        },
                    shape =
                        RoundedCornerShape(
                            999.dp
                        )
                )
                .padding(
                    horizontal =
                        9.dp,
                    vertical =
                        8.dp
                )
    )
}

@Composable
private fun ConversionLabel(
    text: String
) {
    Text(
        text =
            "✓ $text",
        color =
            Color(0xFF2563EB),
        fontSize =
            11.sp,
        fontWeight =
            FontWeight.Bold,
        modifier =
            Modifier.padding(
                start =
                    5.dp
            )
    )
}

fun validateRegisterInput(
    fullName: String,
    email: String,
    password: String,
    age: Int,
    weightKg: Double,
    heightCm: Double,
    heightUnit: String,
    heightFeet: Int,
    heightInches: Int
): String? {
    if (
        fullName
            .trim()
            .length <
        2
    ) {
        return "Please enter your full name."
    }

    if (
        !Patterns.EMAIL_ADDRESS
            .matcher(
                email.trim()
            )
            .matches()
    ) {
        return "Please enter a valid email address."
    }

    if (
        password.length <
        6
    ) {
        return "Password must be at least 6 characters."
    }

    if (
        !password.any(
            Char::isDigit
        )
    ) {
        return "Password must include at least one number."
    }

    if (
        age !in
        13..100
    ) {
        return "Please select a valid age."
    }

    if (
        weightKg !in
        20.0..350.0
    ) {
        return "Please enter a valid weight."
    }

    if (
        heightUnit ==
        "ft/in" &&
        heightInches !in
        0..11
    ) {
        return "Inches must be between 0 and 11."
    }

    if (
        heightUnit ==
        "ft/in" &&
        heightFeet <=
        0
    ) {
        return "Please enter the height in feet."
    }

    if (
        heightCm !in
        80.0..250.0
    ) {
        return "Please enter a valid height."
    }

    return null
}

@Composable
private fun SignUpMessageCard(
    message: String,
    isError: Boolean
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                17.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (
                        isError
                    ) {
                        Color(0xFFFFEBEE)
                    } else {
                        Color(0xFFEAF7EE)
                    }
            )
    ) {
        Text(
            text =
                message,
            modifier =
                Modifier.padding(
                    13.dp
                ),
            color =
                if (
                    isError
                ) {
                    Color(0xFFB91C1C)
                } else {
                    Color(0xFF15803D)
                },
            fontWeight =
                FontWeight.SemiBold,
            fontSize =
                12.sp
        )
    }
}

@Composable
private fun AuthOptionDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier =
        Modifier
) {
    var expanded by
    remember {
        mutableStateOf(
            false
        )
    }

    Column(
        modifier =
            modifier
    ) {
        Text(
            text =
                label,
            color =
                Color(0xFF64748B),
            fontSize =
                11.sp,
            fontWeight =
                FontWeight.Bold,
            modifier =
                Modifier.padding(
                    start =
                        4.dp,
                    bottom =
                        5.dp
                )
        )

        Box(
            modifier =
                Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    expanded =
                        true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        56.dp
                    ),
                shape =
                    RoundedCornerShape(
                        17.dp
                    )
            ) {
                Text(
                    text =
                        selected,
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    color =
                        Color(0xFF0F172A),
                    fontSize =
                        12.sp
                )

                Text(
                    text =
                        "⌄",
                    color =
                        Color(0xFF2563EB),
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }

            DropdownMenu(
                expanded =
                    expanded,
                onDismissRequest = {
                    expanded =
                        false
                }
            ) {
                options.forEach {
                        option ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                option
                            )
                        },
                        onClick = {
                            onSelected(
                                option
                            )

                            expanded =
                                false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthUnitToggle(
    title: String,
    icon: String,
    first: String,
    second: String,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column {
        Text(
            text =
                "$icon $title",
            color =
                Color(0xFF64748B),
            fontSize =
                11.sp,
            fontWeight =
                FontWeight.Bold,
            modifier =
                Modifier.padding(
                    start =
                        4.dp,
                    bottom =
                        6.dp
                )
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            listOf(
                first,
                second
            )
                .forEach {
                        unit ->

                    if (
                        selected ==
                        unit
                    ) {
                        Button(
                            onClick = {
                                onSelected(
                                    unit
                                )
                            },
                            modifier =
                                Modifier.weight(
                                    1f
                                ),
                            shape =
                                RoundedCornerShape(
                                    16.dp
                                )
                        ) {
                            Text(
                                unit
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                onSelected(
                                    unit
                                )
                            },
                            modifier =
                                Modifier.weight(
                                    1f
                                ),
                            shape =
                                RoundedCornerShape(
                                    16.dp
                                )
                        ) {
                            Text(
                                unit
                            )
                        }
                    }
                }
        }
    }
}

private fun String.decimalOnly(): String {
    var dotUsed =
        false

    return filter {
            character ->

        when {
            character.isDigit() ->
                true

            character ==
                    '.' &&
                    !dotUsed -> {
                dotUsed =
                    true

                true
            }

            else ->
                false
        }
    }
}
