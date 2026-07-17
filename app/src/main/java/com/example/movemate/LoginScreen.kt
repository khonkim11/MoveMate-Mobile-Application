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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: (UserSession) -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

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

    var rememberMe by
    remember {
        mutableStateOf(
            false
        )
    }

    var message by
    remember {
        mutableStateOf<String?>(
            null
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
                        17.dp
                    )
            ) {
                item {
                    LoginHeader(
                        loading =
                            loading,
                        onBack =
                            onBack
                    )
                }

                item {
                    LoginWelcomePanel()
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
                                    20.dp
                                ),
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    14.dp
                                )
                        ) {
                            Text(
                                text =
                                    "Login Account",
                                fontSize =
                                    22.sp,
                                fontWeight =
                                    FontWeight.ExtraBold,
                                color =
                                    Color(0xFF0F172A)
                            )

                            Text(
                                text =
                                    "Enter the same account used to save workouts and progress.",
                                fontSize =
                                    12.sp,
                                lineHeight =
                                    17.sp,
                                color =
                                    Color(0xFF64748B)
                            )

                            OutlinedTextField(
                                value =
                                    email,
                                onValueChange = {
                                    email =
                                        it

                                    message =
                                        null
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
                                        null
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
                                visualTransformation =
                                    if (
                                        passwordVisible
                                    ) {
                                        VisualTransformation.None
                                    } else {
                                        PasswordVisualTransformation()
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
                                            KeyboardType.Password
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        18.dp
                                    )
                            )

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked =
                                        rememberMe,
                                    enabled =
                                        !loading,
                                    onCheckedChange = {
                                        rememberMe =
                                            it
                                    }
                                )

                                Text(
                                    text =
                                        "Remember Me",
                                    color =
                                        Color(0xFF374151),
                                    fontSize =
                                        13.sp,
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                )

                                TextButton(
                                    enabled =
                                        !loading,
                                    onClick =
                                        onForgotPassword
                                ) {
                                    Text(
                                        text =
                                            "Forgot password?",
                                        color =
                                            Color(0xFF2563EB),
                                        fontSize =
                                            12.sp,
                                        fontWeight =
                                            FontWeight.ExtraBold
                                    )
                                }
                            }

                            message?.let {
                                    currentMessage ->

                                LoginMessageCard(
                                    message =
                                        currentMessage
                                )
                            }

                            Button(
                                onClick = {
                                    val validation =
                                        validateLoginInput(
                                            email =
                                                email,
                                            password =
                                                password
                                        )

                                    if (
                                        validation !=
                                        null
                                    ) {
                                        message =
                                            validation

                                        return@Button
                                    }

                                    scope.launch {
                                        try {
                                            loading =
                                                true

                                            message =
                                                null

                                            val response =
                                                withContext(
                                                    Dispatchers.IO
                                                ) {
                                                    ApiService
                                                        .loginUser(
                                                            email.trim(),
                                                            password
                                                        )
                                                }

                                            if (
                                                response.optBoolean(
                                                    "success"
                                                )
                                            ) {
                                                val userSession =
                                                    buildUserSessionFromApiResponse(
                                                        response,
                                                        "",
                                                        email.trim()
                                                    )

                                                if (
                                                    userSession.userId <=
                                                    0
                                                ) {
                                                    message =
                                                        "Login succeeded, but the server did not return a valid user ID."
                                                } else {
                                                    onLoginSuccess(
                                                        userSession
                                                    )
                                                }
                                            } else {
                                                message =
                                                    response.optString(
                                                        "message",
                                                        "Invalid email or password."
                                                    )
                                            }
                                        } catch (
                                            exception: Exception
                                        ) {
                                            message =
                                                "Cannot connect server: " +
                                                        (
                                                                exception.message
                                                                    ?: "Unknown error"
                                                                )
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
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            Color(0xFF2563EB),
                                        disabledContainerColor =
                                            Color(0xFF93C5FD)
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        19.dp
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
                                            "Log In to MoveMate",
                                        fontSize =
                                            16.sp,
                                        fontWeight =
                                            FontWeight.ExtraBold
                                    )
                                }
                            }

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.Center,
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Text(
                                    text =
                                        "Don't have an account?",
                                    color =
                                        Color(0xFF64748B),
                                    fontSize =
                                        13.sp
                                )

                                TextButton(
                                    enabled =
                                        !loading,
                                    onClick =
                                        onSignUp
                                ) {
                                    Text(
                                        text =
                                            "Sign Up",
                                        color =
                                            Color(0xFF2563EB),
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
private fun LoginHeader(
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

        Text(
            text =
                "Secure Login",
            color =
                Color.White.copy(
                    alpha =
                        0.82f
                ),
            fontSize =
                12.sp,
            fontWeight =
                FontWeight.Bold,
            modifier =
                Modifier.padding(
                    start =
                        13.dp
                )
        )
    }
}

@Composable
private fun LoginWelcomePanel() {
    Column(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(
                    70.dp
                )
                .background(
                    color =
                        Color.White.copy(
                            alpha =
                                0.14f
                        ),
                    shape =
                        RoundedCornerShape(
                            23.dp
                        )
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    "🏃",
                fontSize =
                    34.sp
            )
        }

        Text(
            text =
                "Welcome Back",
            fontSize =
                31.sp,
            fontWeight =
                FontWeight.ExtraBold,
            color =
                Color.White,
            modifier =
                Modifier.padding(
                    top =
                        13.dp
                )
        )

        Text(
            text =
                "Continue your fitness journey",
            fontSize =
                13.sp,
            color =
                Color.White.copy(
                    alpha =
                        0.84f
                ),
            modifier =
                Modifier.padding(
                    top =
                        4.dp
                )
        )
    }
}

@Composable
private fun LoginMessageCard(
    message: String
) {
    val success =
        message.contains(
            "success",
            ignoreCase =
                true
        )

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                16.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (
                        success
                    ) {
                        Color(0xFFEAF7EE)
                    } else {
                        Color(0xFFFFEBEE)
                    }
            )
    ) {
        Text(
            text =
                (
                        if (
                            success
                        ) {
                            "✓ "
                        } else {
                            "⚠ "
                        }
                        ) +
                        message,
            color =
                if (
                    success
                ) {
                    Color(0xFF15803D)
                } else {
                    Color(0xFFB91C1C)
                },
            fontWeight =
                FontWeight.SemiBold,
            fontSize =
                12.sp,
            modifier =
                Modifier.padding(
                    13.dp
                )
        )
    }
}

fun validateLoginInput(
    email: String,
    password: String
): String? {
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
        password.isBlank()
    ) {
        return "Please enter your password."
    }

    return null
}