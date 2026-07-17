package com.example.movemate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object WorkoutHistoryApiService {

    private const val BASE_URL =
        "http://10.0.2.2/movemate_api/"

    suspend fun loadHistory(
        userId: Int,
        limit: Int =
            1_000,
        offset: Int =
            0
    ): JSONObject =
        withContext(
            Dispatchers.IO
        ) {
            require(
                userId >
                        0
            ) {
                "Invalid user ID."
            }

            postJson(
                endpoint =
                    "workout_history.php",
                body =
                    JSONObject()
                        .put(
                            "user_id",
                            userId
                        )
                        .put(
                            "limit",
                            limit.coerceIn(
                                1,
                                2_000
                            )
                        )
                        .put(
                            "offset",
                            offset.coerceAtLeast(
                                0
                            )
                        )
            )
        }

    private fun postJson(
        endpoint: String,
        body: JSONObject
    ): JSONObject {
        var connection:
                HttpURLConnection? =
            null

        try {
            connection =
                (
                        URL(
                            BASE_URL +
                                    endpoint
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
                            25_000

                        doInput =
                            true

                        doOutput =
                            true

                        useCaches =
                            false

                        setRequestProperty(
                            "Content-Type",
                            "application/json; charset=UTF-8"
                        )

                        setRequestProperty(
                            "Accept",
                            "application/json"
                        )
                    }

            OutputStreamWriter(
                connection.outputStream,
                StandardCharsets.UTF_8
            )
                .use {
                        writer ->

                    writer.write(
                        body.toString()
                    )

                    writer.flush()
                }

            val responseCode =
                connection.responseCode

            val responseStream =
                if (
                    responseCode in
                    200..299
                ) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val responseText =
                readText(
                    responseStream
                )
                    .trim()
                    .removePrefix(
                        "\uFEFF"
                    )

            if (
                responseText.isBlank()
            ) {
                return JSONObject()
                    .put(
                        "success",
                        false
                    )
                    .put(
                        "message",
                        "Empty history response. HTTP $responseCode"
                    )
                    .put(
                        "http_code",
                        responseCode
                    )
            }

            return try {
                JSONObject(
                    responseText
                )
                    .put(
                        "http_code",
                        responseCode
                    )
            } catch (
                exception: Exception
            ) {
                JSONObject()
                    .put(
                        "success",
                        false
                    )
                    .put(
                        "message",
                        "The server returned invalid JSON. HTTP $responseCode"
                    )
                    .put(
                        "response_preview",
                        responseText.take(
                            220
                        )
                    )
                    .put(
                        "http_code",
                        responseCode
                    )
            }
        } finally {
            connection
                ?.disconnect()
        }
    }

    private fun readText(
        stream: InputStream?
    ): String {
        if (
            stream ==
            null
        ) {
            return ""
        }

        return BufferedReader(
            InputStreamReader(
                stream,
                StandardCharsets.UTF_8
            )
        )
            .use {
                    reader ->

                buildString {
                    while (
                        true
                    ) {
                        val line =
                            reader.readLine()
                                ?: break

                        append(
                            line
                        )
                    }
                }
            }
    }
}