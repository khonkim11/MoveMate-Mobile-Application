package com.example.movemate

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

private const val ACCESSIBILITY_PREFS =
    "movemate_accessibility_preferences"

private fun accessibilityTextSizeKey(
    userId: Int
): String {
    return "user_${userId}_text_size"
}

private fun accessibilityUserKeyPrefix(
    userId: Int
): String {
    return "user_${userId}_"
}

val LocalMoveMateAccessibility =
    staticCompositionLocalOf {
        MoveMateAccessibilitySettings()
    }

@Composable
fun rememberMoveMateAccessibilitySettings(
    userId: Int
): MoveMateAccessibilitySettings {
    val context = LocalContext.current

    var settings by remember(
        context,
        userId
    ) {
        mutableStateOf(
            loadMoveMateAccessibilitySettings(
                context,
                userId
            )
        )
    }

    DisposableEffect(
        context,
        userId
    ) {
        val preferences =
            context.getSharedPreferences(
                ACCESSIBILITY_PREFS,
                Context.MODE_PRIVATE
            )

        val listener =
            SharedPreferences
                .OnSharedPreferenceChangeListener {
                        _,
                        changedKey ->

                    if (
                        changedKey != null &&
                        changedKey.startsWith(
                            accessibilityUserKeyPrefix(
                                userId
                            )
                        )
                    ) {
                        settings =
                            loadMoveMateAccessibilitySettings(
                                context,
                                userId
                            )
                    }
                }

        preferences
            .registerOnSharedPreferenceChangeListener(
                listener
            )

        onDispose {
            preferences
                .unregisterOnSharedPreferenceChangeListener(
                    listener
                )
        }
    }

    return settings
}

@Composable
fun MoveMateAccessibilityProvider(
    settings:
    MoveMateAccessibilitySettings,
    content: @Composable () -> Unit
) {
    val baseDensity =
        LocalDensity.current

    /*
     * Respect Android's system font scale.
     * The app-specific selection only increases it when needed.
     */
    val adjustedFontScale = max(
        baseDensity.fontScale,
        settings.textSize.multiplier
    )

    val adjustedDensity = Density(
        density = baseDensity.density,
        fontScale = adjustedFontScale
    )

    val baseTypography =
        MaterialTheme.typography

    val accessibilityTypography =
        if (settings.boldText) {
            baseTypography.withBoldAccessibilityText()
        } else {
            baseTypography
        }

    val colorScheme =
        if (settings.highContrast) {
            moveMateHighContrastColorScheme()
        } else {
            MaterialTheme.colorScheme
        }

    CompositionLocalProvider(
        LocalDensity provides
                adjustedDensity,
        LocalMoveMateAccessibility provides
                settings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography =
                accessibilityTypography,
            shapes = MaterialTheme.shapes,
            content = content
        )
    }
}

@Composable
fun accessibilityControlHeight(): Dp {
    return if (
        LocalMoveMateAccessibility
            .current
            .largerControls
    ) {
        62.dp
    } else {
        50.dp
    }
}

@Composable
fun accessibilityAnimationDuration(
    normalDurationMillis: Int
): Int {
    return if (
        LocalMoveMateAccessibility
            .current
            .reduceMotion
    ) {
        0
    } else {
        normalDurationMillis
    }
}

@Composable
fun rememberMoveMateHapticAction():
            () -> Unit {
    val hapticFeedback =
        LocalHapticFeedback.current

    val enabled =
        LocalMoveMateAccessibility
            .current
            .hapticFeedback

    return remember(
        hapticFeedback,
        enabled
    ) {
        {
            if (enabled) {
                hapticFeedback
                    .performHapticFeedback(
                        HapticFeedbackType
                            .LongPress
                    )
            }
        }
    }
}

private fun Typography
        .withBoldAccessibilityText():
        Typography {
    return copy(
        displayLarge =
            displayLarge.accessibilityBold(),
        displayMedium =
            displayMedium.accessibilityBold(),
        displaySmall =
            displaySmall.accessibilityBold(),
        headlineLarge =
            headlineLarge.accessibilityBold(),
        headlineMedium =
            headlineMedium.accessibilityBold(),
        headlineSmall =
            headlineSmall.accessibilityBold(),
        titleLarge =
            titleLarge.accessibilityBold(),
        titleMedium =
            titleMedium.accessibilityBold(),
        titleSmall =
            titleSmall.accessibilityBold(),
        bodyLarge =
            bodyLarge.accessibilityBold(),
        bodyMedium =
            bodyMedium.accessibilityBold(),
        bodySmall =
            bodySmall.accessibilityBold(),
        labelLarge =
            labelLarge.accessibilityBold(),
        labelMedium =
            labelMedium.accessibilityBold(),
        labelSmall =
            labelSmall.accessibilityBold()
    )
}

private fun TextStyle
        .accessibilityBold():
        TextStyle {
    return copy(
        fontWeight =
            when (fontWeight) {
                FontWeight.Black,
                FontWeight.ExtraBold,
                FontWeight.Bold ->
                    fontWeight

                else ->
                    FontWeight.SemiBold
            }
    )
}

private fun moveMateHighContrastColorScheme() =
    lightColorScheme(
        primary = Color(0xFF0039A6),
        onPrimary = Color.White,
        primaryContainer =
            Color(0xFFD8E2FF),
        onPrimaryContainer =
            Color(0xFF001A4D),

        secondary = Color(0xFF004D40),
        onSecondary = Color.White,
        secondaryContainer =
            Color(0xFFB2DFDB),
        onSecondaryContainer =
            Color(0xFF00251F),

        tertiary = Color(0xFF7A1A00),
        onTertiary = Color.White,
        tertiaryContainer =
            Color(0xFFFFDAD0),
        onTertiaryContainer =
            Color(0xFF3A0900),

        background = Color.White,
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black,
        surfaceVariant =
            Color(0xFFE5E7EB),
        onSurfaceVariant =
            Color(0xFF111827),

        outline = Color(0xFF374151),
        error = Color(0xFFB00020),
        onError = Color.White
    )