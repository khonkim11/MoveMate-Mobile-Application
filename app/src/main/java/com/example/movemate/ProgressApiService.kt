package com.example.movemate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ProgressApiService {

    private const val BASE_URL =
        "http://10.0.2.2/movemate_api/"

    suspend fun getProgress(
        userId: Int,
        days: Int
    ): JSONObject =
        withContext(
            Dispatchers.IO
        ) {
            if (
                userId <=
                0
            ) {
                return@withContext errorJson(
                    message =
                        "Invalid user session.",
                    httpCode =
                        0
                )
            }

            val safeDays =
                days.coerceIn(
                    1,
                    365
                )

            val formBody =
                listOf(
                    "user_id" to
                            userId.toString(),
                    "days" to
                            safeDays.toString()
                )
                    .joinToString(
                        separator =
                            "&"
                    ) {
                            pair ->

                        pair.first.encode() +
                                "=" +
                                pair.second.encode()
                    }

            var connection:
                    HttpURLConnection? =
                null

            try {
                connection =
                    (
                            URL(
                                BASE_URL +
                                        "progress.php"
                            )
                                .openConnection()
                                    as
                                    HttpURLConnection
                            )
                        .apply {
                            requestMethod =
                                "POST"

                            connectTimeout =
                                15_000

                            readTimeout =
                                15_000

                            doInput =
                                true

                            doOutput =
                                true

                            useCaches =
                                false

                            setRequestProperty(
                                "Content-Type",
                                "application/x-www-form-urlencoded; charset=UTF-8"
                            )

                            setRequestProperty(
                                "Accept",
                                "application/json"
                            )
                        }

                connection.outputStream
                    .bufferedWriter(
                        Charsets.UTF_8
                    )
                    .use {
                            writer ->

                        writer.write(
                            formBody
                        )

                        writer.flush()
                    }

                val httpCode =
                    connection.responseCode

                val stream =
                    if (
                        httpCode in
                        200..299
                    ) {
                        connection.inputStream
                    } else {
                        connection.errorStream
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
                        .trimStart(
                            '\uFEFF'
                        )

                when {
                    body.startsWith(
                        "{"
                    ) -> {
                        JSONObject(
                            body
                        )
                            .apply {
                                if (
                                    !has(
                                        "http_code"
                                    )
                                ) {
                                    put(
                                        "http_code",
                                        httpCode
                                    )
                                }
                            }
                    }

                    body.isBlank() -> {
                        errorJson(
                            message =
                                "Progress server returned an empty response. HTTP $httpCode.",
                            httpCode =
                                httpCode
                        )
                    }

                    else -> {
                        errorJson(
                            message =
                                "Progress server returned invalid JSON. HTTP $httpCode.",
                            httpCode =
                                httpCode
                        )
                            .apply {
                                put(
                                    "response_preview",
                                    body.take(
                                        180
                                    )
                                )
                            }
                    }
                }
            } catch (
                exception: Exception
            ) {
                errorJson(
                    message =
                        "Cannot load progress: " +
                                (
                                        exception.message
                                            ?: "Unknown error"
                                        ),
                    httpCode =
                        0
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
        httpCode: Int
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
