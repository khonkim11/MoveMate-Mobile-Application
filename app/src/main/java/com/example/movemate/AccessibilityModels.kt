package com.example.movemate

enum class AccessibilityTextSize(
    val label: String,
    val multiplier: Float
) {
    DEFAULT(
        label = "Default",
        multiplier = 1.0f
    ),
    LARGE(
        label = "Large",
        multiplier = 1.15f
    ),
    EXTRA_LARGE(
        label = "Extra Large",
        multiplier = 1.30f
    )
}

data class MoveMateAccessibilitySettings(
    val textSize:
    AccessibilityTextSize =
        AccessibilityTextSize.DEFAULT,
    val highContrast: Boolean = false,
    val boldText: Boolean = false,
    val reduceMotion: Boolean = false,
    val largerControls: Boolean = false,
    val hapticFeedback: Boolean = true
)