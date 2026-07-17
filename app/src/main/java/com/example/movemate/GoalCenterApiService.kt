package com.example.movemate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object GoalCenterApiService {

    private const val BASE_URL =
        "http://10.0.2.2/movemate_api/"

    suspend fun saveUserGoal(
        userId: Int,
        goal: GoalCenterData
    ): JSONObject =
        post(
            endpoint =
                "goal_center_save.php",
            params = mapOf(
                "user_id" to
                        userId.toString(),
                "goal_type" to
                        goal.goalType,
                "activity_focus" to
                        goal.activityFocus,
                "intensity" to
                        goal.intensity,
                "schedule" to
                        goal.schedule,
                "daily_calories_target" to
                        goal.dailyCaloriesTarget
                            .toInt()
                            .toString(),
                "workout_calories_target" to
                        goal.workoutCaloriesTarget
                            .toInt()
                            .toString(),
                "steps_target" to
                        goal.stepsTarget
                            .toInt()
                            .toString(),
                "water_target_ml" to
                        goal.waterTargetMl
                            .toInt()
                            .toString(),
                "workout_minutes_target" to
                        goal.workoutMinutesTarget
                            .toInt()
                            .toString()
            )
        )

    suspend fun getUserGoal(
        userId: Int
    ): JSONObject =
        get(
            endpoint =
                "goal_center_get.php",
            params = mapOf(
                "user_id" to
                        userId.toString()
            )
        )

    suspend fun deleteUserGoal(
        userId: Int
    ): JSONObject =
        post(
            endpoint =
                "goal_center_delete.php",
            params = mapOf(
                "user_id" to
                        userId.toString()
            )
        )

    private suspend fun post(
        endpoint: String,
        params: Map<String, String>
    ): JSONObject =
        withContext(
            Dispatchers.IO
        ) {
            request(
                method =
                    "POST",
                endpoint =
                    endpoint,
                params =
                    params
            )
        }

    private suspend fun get(
        endpoint: String,
        params: Map<String, String>
    ): JSONObject =
        withContext(
            Dispatchers.IO
        ) {
            request(
                method =
                    "GET",
                endpoint =
                    endpoint,
                params =
                    params
            )
        }

    private fun request(
        method: String,
        endpoint: String,
        params: Map<String, String>
    ): JSONObject {
        val formBody =
            params.entries
                .joinToString(
                    "&"
                ) {
                        (key, value) ->

                    "${key.encode()}=" +
                            value.encode()
                }

        val urlText =
            if (
                method ==
                "GET"
            ) {
                "$BASE_URL$endpoint?$formBody"
            } else {
                "$BASE_URL$endpoint"
            }

        var connection:
                HttpURLConnection? =
            null

        return try {
            connection =
                URL(
                    urlText
                )
                    .openConnection()
                        as
                        HttpURLConnection

            connection.requestMethod =
                method

            connection.connectTimeout =
                15_000

            connection.readTimeout =
                15_000

            connection.doInput =
                true

            connection.useCaches =
                false

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            if (
                method ==
                "POST"
            ) {
                connection.doOutput =
                    true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8"
                )

                connection.outputStream
                    .bufferedWriter(
                        Charsets.UTF_8
                    )
                    .use { writer ->
                        writer.write(
                            formBody
                        )

                        writer.flush()
                    }
            }

            val code =
                connection.responseCode

            val stream =
                if (
                    code in
                    200..299
                ) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val responseText =
                stream
                    ?.bufferedReader(
                        Charsets.UTF_8
                    )
                    ?.use {
                        it.readText()
                    }
                    .orEmpty()
                    .trim()

            if (
                responseText
                    .startsWith(
                        "{"
                    )
            ) {
                JSONObject(
                    responseText
                )
                    .apply {
                        if (!has("http_code")) {
                            put(
                                "http_code",
                                code
                            )
                        }
                    }
            } else {
                errorJson(
                    message =
                        if (
                            responseText.isBlank()
                        ) {
                            "Server returned an empty response."
                        } else {
                            "Server returned non-JSON data."
                        },
                    httpCode =
                        code
                )
            }
        } catch (
            exception: Exception
        ) {
            errorJson(
                message =
                    "Cannot connect to $endpoint: " +
                            (
                                    exception.message
                                        ?: "Unknown error"
                                    )
            )
        } finally {
            connection
                ?.disconnect()
        }
    }

    private fun String.encode():
            String {
        return URLEncoder.encode(
            this,
            Charsets.UTF_8.name()
        )
    }

    private fun errorJson(
        message: String,
        httpCode: Int = 0
    ): JSONObject {
        return JSONObject()
            .apply {
                put(
                    "success",
                    false
                )

                put(
                    "message",
                    message
                )

                put(
                    "http_code",
                    httpCode
                )
            }
    }
}
