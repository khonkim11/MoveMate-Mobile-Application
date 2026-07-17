package com.example.movemate

import org.json.JSONObject

/*
 * Converts the login PHP JSON response into the app's UserSession.
 *
 * Expected response:
 * {
 *   "success": true,
 *   "user": {
 *     "id": 1,
 *     "full_name": "User Name",
 *     "email": "user@example.com",
 *     "gender": "Male",
 *     "age": 25,
 *     "weight_kg": 65,
 *     "height_cm": 170
 *   }
 * }
 */
fun buildUserSessionFromApiResponse(
    response: JSONObject,
    fallbackFullName: String = "",
    fallbackEmail: String = ""
): UserSession {
    val user =
        response.optJSONObject(
            "user"
        )
            ?: response

    return UserSession(
        userId =
            firstPositiveAuthInt(
                user,
                "id",
                "user_id",
                "userId"
            ),
        fullName =
            firstAuthString(
                json =
                    user,
                keys =
                    arrayOf(
                        "full_name",
                        "fullName",
                        "name",
                        "user_name"
                    ),
                fallbackValue =
                    fallbackFullName
            ),
        email =
            firstAuthString(
                json =
                    user,
                keys =
                    arrayOf(
                        "email",
                        "user_email"
                    ),
                fallbackValue =
                    fallbackEmail
            ),
        gender =
            firstAuthString(
                json =
                    user,
                keys =
                    arrayOf(
                        "gender",
                        "user_gender"
                    )
            ),
        age =
            firstPositiveAuthInt(
                user,
                "age",
                "user_age"
            ),
        weightKg =
            firstPositiveAuthDouble(
                user,
                "weight_kg",
                "weightKg",
                "weight"
            ),
        heightCm =
            firstPositiveAuthDouble(
                user,
                "height_cm",
                "heightCm",
                "height"
            )
    )
}

private fun firstAuthString(
    json: JSONObject,
    keys: Array<String>,
    fallbackValue: String = ""
): String {
    keys.forEach {
            key ->

        val value =
            json.optString(
                key,
                ""
            )
                .trim()

        if (
            value.isNotBlank() &&
            !value.equals(
                "null",
                ignoreCase =
                    true
            )
        ) {
            return value
        }
    }

    return fallbackValue.trim()
}

private fun firstPositiveAuthInt(
    json: JSONObject,
    vararg keys: String
): Int {
    keys.forEach {
            key ->

        val value =
            when (
                val rawValue =
                    json.opt(
                        key
                    )
            ) {
                is Int ->
                    rawValue

                is Long ->
                    rawValue.toInt()

                is Double ->
                    rawValue.toInt()

                is Float ->
                    rawValue.toInt()

                is String ->
                    rawValue
                        .trim()
                        .toIntOrNull()
                        ?: 0

                else ->
                    0
            }

        if (
            value >
            0
        ) {
            return value
        }
    }

    return 0
}

private fun firstPositiveAuthDouble(
    json: JSONObject,
    vararg keys: String
): Double {
    keys.forEach {
            key ->

        val value =
            when (
                val rawValue =
                    json.opt(
                        key
                    )
            ) {
                is Double ->
                    rawValue

                is Float ->
                    rawValue.toDouble()

                is Int ->
                    rawValue.toDouble()

                is Long ->
                    rawValue.toDouble()

                is String ->
                    rawValue
                        .trim()
                        .toDoubleOrNull()
                        ?: 0.0

                else ->
                    0.0
            }

        if (
            value >
            0.0
        ) {
            return value
        }
    }

    return 0.0
}