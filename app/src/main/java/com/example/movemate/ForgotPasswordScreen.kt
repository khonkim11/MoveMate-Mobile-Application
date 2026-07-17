package com.example.movemate

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onPasswordUpdated: () -> Unit
) {
    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    var email by
    remember {
        mutableStateOf(
            ""
        )
    }

    var newPassword by
    remember {
        mutableStateOf(
            ""
        )
    }

    var confirmPassword by
    remember {
        mutableStateOf(
            ""
        )
    }

    var newPasswordVisible by
    remember {
        mutableStateOf(
            false
        )
    }

    var confirmPasswordVisible by
    remember {
        mutableStateOf(
            false
        )
    }

    var isLoading by
    remember {
        mutableStateOf(
            false
        )
    }

    var errorMessage by
    remember {
        mutableStateOf(
            ""
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

    fun validateForm():
            Boolean {
        errorMessage =
            ""

        if (
            email
                .trim()
                .isBlank()
        ) {
            errorMessage =
                "Please enter your email."

            return false
        }

        if (
            !android.util.Patterns
                .EMAIL_ADDRESS
                .matcher(
                    email.trim()
                )
                .matches()
        ) {
            errorMessage =
                "Please enter a valid email address."

            return false
        }

        if (
            newPassword.length <
            6
        ) {
            errorMessage =
                "Password must be at least 6 characters."

            return false
        }

        if (
            newPassword !=
            confirmPassword
        ) {
            errorMessage =
                "Passwords do not match."

            return false
        }

        return true
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
                        18.dp
                    )
            ) {
                item {
                    ForgotPasswordHeader(
                        loading =
                            isLoading,
                        onBack =
                            onBack
                    )
                }

                item {
                    ForgotPasswordHero()
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
                                    "Create new password",
                                color =
                                    Color(0xFF0F172A),
                                fontSize =
                                    21.sp,
                                fontWeight =
                                    FontWeight.ExtraBold
                            )

                            Text(
                                text =
                                    "Use your registered email. Your existing password-update service and login return flow remain unchanged.",
                                color =
                                    Color(0xFF64748B),
                                fontSize =
                                    12.sp,
                                lineHeight =
                                    17.sp
                            )

                            OutlinedTextField(
                                value =
                                    email,
                                onValueChange = {
                                    email =
                                        it

                                    errorMessage =
                                        ""
                                },
                                label = {
                                    Text(
                                        "Registered Email"
                                    )
                                },
                                leadingIcon = {
                                    MoveMateAuthLeadingIcon(
                                        "✉"
                                    )
                                },
                                singleLine =
                                    true,
                                enabled =
                                    !isLoading,
                                keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType =
                                            KeyboardType.Email
                                    ),
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape =
                                    RoundedCornerShape(
                                        18.dp
                                    )
                            )

                            OutlinedTextField(
                                value =
                                    newPassword,
                                onValueChange = {
                                    newPassword =
                                        it

                                    errorMessage =
                                        ""
                                },
                                label = {
                                    Text(
                                        "New Password"
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
                                            newPasswordVisible,
                                        enabled =
                                            !isLoading,
                                        onClick = {
                                            newPasswordVisible =
                                                !newPasswordVisible
                                        }
                                    )
                                },
                                singleLine =
                                    true,
                                enabled =
                                    !isLoading,
                                visualTransformation =
                                    if (
                                        newPasswordVisible
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
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape =
                                    RoundedCornerShape(
                                        18.dp
                                    )
                            )

                            OutlinedTextField(
                                value =
                                    confirmPassword,
                                onValueChange = {
                                    confirmPassword =
                                        it

                                    errorMessage =
                                        ""
                                },
                                label = {
                                    Text(
                                        "Confirm Password"
                                    )
                                },
                                leadingIcon = {
                                    MoveMateAuthLeadingIcon(
                                        "✓"
                                    )
                                },
                                trailingIcon = {
                                    MoveMatePasswordEyeToggle(
                                        visible =
                                            confirmPasswordVisible,
                                        enabled =
                                            !isLoading,
                                        onClick = {
                                            confirmPasswordVisible =
                                                !confirmPasswordVisible
                                        }
                                    )
                                },
                                singleLine =
                                    true,
                                enabled =
                                    !isLoading,
                                visualTransformation =
                                    if (
                                        confirmPasswordVisible
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
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape =
                                    RoundedCornerShape(
                                        18.dp
                                    )
                            )

                            PasswordMatchIndicator(
                                newPassword =
                                    newPassword,
                                confirmPassword =
                                    confirmPassword
                            )

                            if (
                                errorMessage.isNotBlank()
                            ) {
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
                                                Color(0xFFFFEBEE)
                                        )
                                ) {
                                    Text(
                                        text =
                                            "⚠ $errorMessage",
                                        color =
                                            Color(0xFFB91C1C),
                                        fontSize =
                                            12.sp,
                                        fontWeight =
                                            FontWeight.SemiBold,
                                        modifier =
                                            Modifier.padding(
                                                13.dp
                                            )
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (
                                        !validateForm()
                                    ) {
                                        return@Button
                                    }

                                    scope.launch {
                                        isLoading =
                                            true

                                        try {
                                            val response =
                                                withContext(
                                                    Dispatchers.IO
                                                ) {
                                                    ApiService.updatePassword(
                                                        email.trim(),
                                                        newPassword
                                                    )
                                                }

                                            val success =
                                                response.optBoolean(
                                                    "success",
                                                    false
                                                )

                                            val responseMessage =
                                                response.optString(
                                                    "message",
                                                    if (
                                                        success
                                                    ) {
                                                        "Password updated successfully."
                                                    } else {
                                                        "Password update failed."
                                                    }
                                                )

                                            if (
                                                success
                                            ) {
                                                Toast.makeText(
                                                    context,
                                                    responseMessage,
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                onPasswordUpdated()
                                            } else {
                                                errorMessage =
                                                    responseMessage
                                            }
                                        } catch (
                                            exception: Exception
                                        ) {
                                            errorMessage =
                                                exception.message
                                                    ?: "Password update failed."
                                        } finally {
                                            isLoading =
                                                false
                                        }
                                    }
                                },
                                enabled =
                                    !isLoading,
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
                                    isLoading
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
                                            "Update Password",
                                        fontWeight =
                                            FontWeight.ExtraBold,
                                        fontSize =
                                            16.sp
                                    )
                                }
                            }

                            TextButton(
                                enabled =
                                    !isLoading,
                                onClick =
                                    onBack,
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text =
                                        "Back to Login",
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

@Composable
private fun ForgotPasswordHeader(
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
                "Account Recovery",
            color =
                Color.White.copy(
                    alpha =
                        0.84f
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
private fun ForgotPasswordHero() {
    Column(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(
                    72.dp
                )
                .background(
                    color =
                        Color.White.copy(
                            alpha =
                                0.14f
                        ),
                    shape =
                        RoundedCornerShape(
                            24.dp
                        )
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    "🔑",
                fontSize =
                    35.sp
            )
        }

        Text(
            text =
                "Reset Password",
            color =
                Color.White,
            fontSize =
                30.sp,
            fontWeight =
                FontWeight.ExtraBold,
            modifier =
                Modifier.padding(
                    top =
                        13.dp
                )
        )

        Text(
            text =
                "Secure your MoveMate account",
            color =
                Color.White.copy(
                    alpha =
                        0.83f
                ),
            fontSize =
                13.sp,
            modifier =
                Modifier.padding(
                    top =
                        4.dp
                )
        )
    }
}

@Composable
private fun PasswordMatchIndicator(
    newPassword: String,
    confirmPassword: String
) {
    val hasMinimumLength =
        newPassword.length >=
                6

    val passwordsMatch =
        confirmPassword.isNotEmpty() &&
                newPassword ==
                confirmPassword

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        PasswordStatusChip(
            modifier =
                Modifier.weight(
                    1f
                ),
            text =
                "6+ characters",
            complete =
                hasMinimumLength
        )

        PasswordStatusChip(
            modifier =
                Modifier.weight(
                    1f
                ),
            text =
                "Passwords match",
            complete =
                passwordsMatch
        )
    }
}

@Composable
private fun PasswordStatusChip(
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
                        8.dp,
                    vertical =
                        8.dp
                )
    )
}
