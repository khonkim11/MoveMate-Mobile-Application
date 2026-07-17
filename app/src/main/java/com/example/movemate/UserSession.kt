package com.example.movemate

import android.content.Context

/** Keep this as the only UserSession data class in the project. */
data class UserSession(
    val userId: Int = 0,
    val fullName: String = "",
    val email: String = "",
    val gender: String = "",
    val age: Int = 0,
    val weightKg: Double = 0.0,
    val heightCm: Double = 0.0
)

private const val SESSION_PREF_NAME = "movemate_session"

fun saveSession(context: Context, session: UserSession) {
    context.getSharedPreferences(SESSION_PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean("is_logged_in", true)
        .putInt("user_id", session.userId)
        .putString("full_name", session.fullName)
        .putString("email", session.email)
        .putString("gender", session.gender)
        .putInt("age", session.age)
        .putString("weight_kg", session.weightKg.toString())
        .putString("height_cm", session.heightCm.toString())
        .apply()
}

fun loadSession(context: Context): UserSession? {
    val prefs = context.getSharedPreferences(SESSION_PREF_NAME, Context.MODE_PRIVATE)
    val loggedIn = prefs.getBoolean("is_logged_in", false)
    val userId = prefs.getInt("user_id", 0)
    val email = prefs.getString("email", "").orEmpty()

    if (!loggedIn || userId <= 0 || email.isBlank()) return null

    return UserSession(
        userId = userId,
        fullName = prefs.getString("full_name", "MoveMate User").orEmpty(),
        email = email,
        gender = prefs.getString("gender", "").orEmpty(),
        age = prefs.getInt("age", 0),
        weightKg = prefs.getString("weight_kg", "0")?.toDoubleOrNull() ?: 0.0,
        heightCm = prefs.getString("height_cm", "0")?.toDoubleOrNull() ?: 0.0
    )
}

fun clearSession(context: Context) {
    context.getSharedPreferences(SESSION_PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .clear()
        .apply()
}

fun updateSavedProfile(
    context: Context,
    fullName: String,
    gender: String,
    age: Int,
    weightKg: Double,
    heightCm: Double
): UserSession? {
    val current = loadSession(context) ?: return null
    val updated = current.copy(
        fullName = fullName.trim().ifBlank { current.fullName },
        gender = gender.trim(),
        age = age.coerceAtLeast(0),
        weightKg = weightKg.coerceAtLeast(0.0),
        heightCm = heightCm.coerceAtLeast(0.0)
    )
    saveSession(context, updated)
    return updated
}
