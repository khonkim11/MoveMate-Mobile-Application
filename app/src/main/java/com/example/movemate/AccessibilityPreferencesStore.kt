package com.example.movemate

import android.content.Context

private const val ACCESSIBILITY_PREFS =
    "movemate_accessibility_preferences"

private fun accessibilityKey(
    userId: Int,
    name: String
): String {
    return "user_${userId}_$name"
}

fun loadMoveMateAccessibilitySettings(
    context: Context,
    userId: Int
): MoveMateAccessibilitySettings {
    if (userId <= 0) {
        return MoveMateAccessibilitySettings()
    }

    val prefs = context.getSharedPreferences(
        ACCESSIBILITY_PREFS,
        Context.MODE_PRIVATE
    )

    val textSizeName = prefs.getString(
        accessibilityKey(
            userId,
            "text_size"
        ),
        AccessibilityTextSize.DEFAULT.name
    ).orEmpty()

    val textSize =
        AccessibilityTextSize.values()
            .firstOrNull {
                it.name == textSizeName
            }
            ?: AccessibilityTextSize.DEFAULT

    return MoveMateAccessibilitySettings(
        textSize = textSize,
        highContrast = prefs.getBoolean(
            accessibilityKey(
                userId,
                "high_contrast"
            ),
            false
        ),
        boldText = prefs.getBoolean(
            accessibilityKey(
                userId,
                "bold_text"
            ),
            false
        ),
        reduceMotion = prefs.getBoolean(
            accessibilityKey(
                userId,
                "reduce_motion"
            ),
            false
        ),
        largerControls = prefs.getBoolean(
            accessibilityKey(
                userId,
                "larger_controls"
            ),
            false
        ),
        hapticFeedback = prefs.getBoolean(
            accessibilityKey(
                userId,
                "haptic_feedback"
            ),
            true
        )
    )
}

fun saveMoveMateAccessibilitySettings(
    context: Context,
    userId: Int,
    settings: MoveMateAccessibilitySettings
) {
    if (userId <= 0) {
        return
    }

    context.getSharedPreferences(
        ACCESSIBILITY_PREFS,
        Context.MODE_PRIVATE
    )
        .edit()
        .putString(
            accessibilityKey(
                userId,
                "text_size"
            ),
            settings.textSize.name
        )
        .putBoolean(
            accessibilityKey(
                userId,
                "high_contrast"
            ),
            settings.highContrast
        )
        .putBoolean(
            accessibilityKey(
                userId,
                "bold_text"
            ),
            settings.boldText
        )
        .putBoolean(
            accessibilityKey(
                userId,
                "reduce_motion"
            ),
            settings.reduceMotion
        )
        .putBoolean(
            accessibilityKey(
                userId,
                "larger_controls"
            ),
            settings.largerControls
        )
        .putBoolean(
            accessibilityKey(
                userId,
                "haptic_feedback"
            ),
            settings.hapticFeedback
        )
        .apply()
}

fun resetMoveMateAccessibilitySettings(
    context: Context,
    userId: Int
) {
    saveMoveMateAccessibilitySettings(
        context = context,
        userId = userId,
        settings =
            MoveMateAccessibilitySettings()
    )
}