package com.example.movemate

import org.json.JSONObject

fun buildUserSessionFromAuthResponse(
    response: JSONObject,
    fallbackName: String = "",
    fallbackEmail: String = "",
    fallbackGender: String = "",
    fallbackAge: Int = 0,
    fallbackWeightKg: Double = 0.0,
    fallbackHeightCm: Double = 0.0
): UserSession {
    val user = response.optJSONObject("user")
        ?: response.optJSONObject("data")
        ?: JSONObject()

    return UserSession(
        userId = user.flexibleInt("id", "user_id"),
        fullName = user.flexibleString("full_name", "fullName", "name")
            .ifBlank { fallbackName.trim() },
        email = user.flexibleString("email", "user_email")
            .ifBlank { fallbackEmail.trim().lowercase() },
        gender = user.flexibleString("gender")
            .ifBlank { fallbackGender.trim() },
        age = user.flexibleInt("age").takeIf { it > 0 } ?: fallbackAge,
        weightKg = user.flexibleDouble("weight_kg", "weightKg", "weight")
            .takeIf { it > 0.0 } ?: fallbackWeightKg,
        heightCm = user.flexibleDouble("height_cm", "heightCm", "height")
            .takeIf { it > 0.0 } ?: fallbackHeightCm
    )
}

private fun JSONObject.flexibleString(vararg keys: String): String {
    for (key in keys) {
        if (!has(key) || isNull(key)) continue
        val value = opt(key)?.toString()?.trim().orEmpty()
        if (value.isNotBlank() && !value.equals("null", ignoreCase = true)) {
            return value
        }
    }
    return ""
}

private fun JSONObject.flexibleInt(vararg keys: String): Int {
    for (key in keys) {
        if (!has(key) || isNull(key)) continue
        val value = opt(key)
        val result = when (value) {
            is Number -> value.toInt()
            is String -> value.trim().toIntOrNull() ?: 0
            else -> 0
        }
        if (result != 0) return result
    }
    return 0
}

private fun JSONObject.flexibleDouble(vararg keys: String): Double {
    for (key in keys) {
        if (!has(key) || isNull(key)) continue
        val value = opt(key)
        val result = when (value) {
            is Number -> value.toDouble()
            is String -> value.trim().toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        if (result != 0.0) return result
    }
    return 0.0
}
