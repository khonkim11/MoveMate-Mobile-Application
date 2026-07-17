package com.example.movemate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object WorkoutApiService {

    /*
     * Android Emulator -> XAMPP Apache:
     * C:\xampp\htdocs\movemate_api\
     */
    private const val BASE_URL =
        "http://10.0.2.2/movemate_api/"

    suspend fun saveWorkout(
        userId: Int,
        activityType: String,
        workoutName: String,
        durationSeconds: Int,
        distanceKm: Double,
        calories: Double,
        intensity: String,
        notes: String,
        levelName: String = intensity,
        targetSeconds: Int = 0,
        routeJson: String = ""
    ): JSONObject {
        return post(
            endpoint =
                "save_workout.php",
            params = mapOf(
                "user_id" to
                        userId.toString(),
                "activity_type" to
                        activityType,
                "workout_name" to
                        workoutName,
                "level_name" to
                        levelName,
                "duration_seconds" to
                        durationSeconds.toString(),
                "target_seconds" to
                        targetSeconds.toString(),
                "distance_km" to
                        distanceKm.toString(),
                "calories" to
                        calories.toString(),
                "intensity" to
                        intensity,
                "notes" to
                        notes,
                "route_json" to
                        routeJson
            )
        )
    }

    suspend fun getTodaySummary(
        userId: Int
    ): JSONObject {
        return post(
            endpoint =
                "today_summary.php",
            params = mapOf(
                "user_id" to
                        userId.toString()
            )
        )
    }


    /*
     * Used by ProgressScreen for the 7-day and 30-day filters.
     */
    suspend fun getProgress(
        userId: Int,
        days: Int
    ): JSONObject {
        val safeDays =
            days.coerceIn(
                1,
                365
            )

        return post(
            endpoint =
                "progress.php",
            params = mapOf(
                "user_id" to
                        userId.toString(),
                "days" to
                        safeDays.toString()
            )
        )
    }

    /*
     * Compatibility for older screens that request the default 7-day view.
     */
    suspend fun getProgress(
        userId: Int
    ): JSONObject {
        return getProgress(
            userId =
                userId,
            days =
                7
        )
    }

    private suspend fun post(
        endpoint: String,
        params: Map<String, String>
    ): JSONObject =
        withContext(
            Dispatchers.IO
        ) {
            val urlText =
                BASE_URL +
                        endpoint

            var connection:
                    HttpURLConnection? =
                null

            try {
                connection =
                    URL(
                        urlText
                    )
                        .openConnection()
                            as
                            HttpURLConnection

                connection.requestMethod =
                    "POST"

                connection.connectTimeout =
                    15_000

                connection.readTimeout =
                    15_000

                connection.doInput =
                    true

                connection.doOutput =
                    true

                connection.useCaches =
                    false

                connection.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8"
                )

                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                )

                connection.setRequestProperty(
                    "Connection",
                    "close"
                )

                connection.outputStream
                    .bufferedWriter(
                        Charsets.UTF_8
                    )
                    .use { writer ->
                        writer.write(
                            params
                                .toFormBody()
                        )

                        writer.flush()
                    }

                connection
                    .readJsonResponse(
                        urlText
                    )
            } catch (
                exception: Exception
            ) {
                val detail =
                    buildString {
                        append(
                            exception
                                .javaClass
                                .simpleName
                        )

                        exception.message
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?.let {
                                append(": ")
                                append(it)
                            }
                    }

                errorJson(
                    message =
                        "Cannot connect to $endpoint ($detail). " +
                                "Start Apache and confirm the PHP file exists.",
                    url =
                        urlText
                )
            } finally {
                connection
                    ?.disconnect()
            }
        }

    private fun Map<String, String>
            .toFormBody(): String {
        return entries
            .joinToString(
                "&"
            ) {
                    (key, value) ->

                "${key.urlEncode()}=" +
                        value.urlEncode()
            }
    }

    private fun String
            .urlEncode(): String {
        return URLEncoder.encode(
            this,
            Charsets.UTF_8.name()
        )
    }

    private fun HttpURLConnection
            .readJsonResponse(
        urlText: String
    ): JSONObject {
        val code =
            responseCode

        val contentTypeValue =
            contentType
                .orEmpty()

        val stream =
            if (
                code in
                200..299
            ) {
                inputStream
            } else {
                errorStream
                    ?: runCatching {
                        inputStream
                    }
                        .getOrNull()
            }

        val body =
            stream
                ?.bufferedReader(
                    Charsets.UTF_8
                )
                ?.use {
                    it.readText()
                }
                .orEmpty()
                .trim()

        if (body.isBlank()) {
            return errorJson(
                message =
                    "The workout server returned an empty response.",
                url =
                    urlText,
                httpCode =
                    code,
                contentType =
                    contentTypeValue
            )
        }

        if (!body.startsWith("{")) {
            return errorJson(
                message =
                    "The workout server returned non-JSON data. " +
                            "Check PHP and the Apache error log.",
                url =
                    urlText,
                httpCode =
                    code,
                contentType =
                    contentTypeValue,
                rawResponse =
                    body
            )
        }

        return try {
            JSONObject(
                body
            ).apply {
                if (!has("http_code")) {
                    put(
                        "http_code",
                        code
                    )
                }
            }
        } catch (
            exception: Exception
        ) {
            errorJson(
                message =
                    "Invalid JSON from the workout server: " +
                            (
                                    exception.message
                                        ?: "Unknown error"
                                    ),
                url =
                    urlText,
                httpCode =
                    code,
                contentType =
                    contentTypeValue,
                rawResponse =
                    body
            )
        }
    }

    private fun errorJson(
        message: String,
        url: String,
        httpCode: Int? = null,
        contentType: String? = null,
        rawResponse: String? = null
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
                    "url",
                    url
                )

                httpCode?.let {
                    put(
                        "http_code",
                        it
                    )
                }

                contentType?.let {
                    put(
                        "content_type",
                        it
                    )
                }

                rawResponse?.let {
                    put(
                        "raw_response_start",
                        it.take(
                            700
                        )
                    )
                }
            }
    }
}
