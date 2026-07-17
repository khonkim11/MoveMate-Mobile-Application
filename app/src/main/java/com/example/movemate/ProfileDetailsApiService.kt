package com.example.movemate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ProfileDetailsApiService {

    private const val BASE_URL =
        "http://10.0.2.2/movemate_api/"

    suspend fun loadProfileDetails(
        session: UserSession
    ): ProfileDetailsResult =
        withContext(
            Dispatchers.IO
        ) {
            if (
                session.userId <=
                0
            ) {
                return@withContext ProfileDetailsResult(
                    success =
                        false,
                    message =
                        "Invalid user session. Please log in again.",
                    data =
                        null
                )
            }

            val response =
                postForm(
                    endpoint =
                        "profile_details.php",
                    params =
                        mapOf(
                            "user_id" to
                                    session.userId
                                        .toString(),
                            "limit" to
                                    "100"
                        )
                )

            parseProfileDetailsResult(
                response =
                    response,
                fallbackSession =
                    session
            )
        }

    private fun postForm(
        endpoint: String,
        params: Map<String, String>
    ): JSONObject {
        val urlText =
            BASE_URL +
                    endpoint

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
                "POST"

            connection.connectTimeout =
                15_000

            connection.readTimeout =
                25_000

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
                .use {
                        writer ->

                    writer.write(
                        params.entries
                            .joinToString(
                                "&"
                            ) {
                                    entry ->

                                entry.key
                                    .profileUrlEncode() +
                                        "=" +
                                        entry.value
                                            .profileUrlEncode()
                            }
                    )

                    writer.flush()
                }

            connection.readProfileJson(
                urlText
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
                    "Cannot connect to profile_details.php: " +
                            (
                                    exception.message
                                        ?: exception
                                            .javaClass
                                            .simpleName
                                    )
                )
                .put(
                    "url",
                    urlText
                )
        } finally {
            connection
                ?.disconnect()
        }
    }

    private fun String.profileUrlEncode():
            String {
        return URLEncoder.encode(
            this,
            Charsets.UTF_8.name()
        )
    }

    private fun HttpURLConnection.readProfileJson(
        urlText: String
    ): JSONObject {
        val httpCode =
            responseCode

        val stream =
            if (
                httpCode in
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
                .removePrefix(
                    "\uFEFF"
                )

        if (
            body.isBlank()
        ) {
            return JSONObject()
                .put(
                    "success",
                    false
                )
                .put(
                    "message",
                    "The profile server returned an empty response. HTTP $httpCode"
                )
                .put(
                    "http_code",
                    httpCode
                )
                .put(
                    "url",
                    urlText
                )
        }

        if (
            !body.startsWith(
                "{"
            )
        ) {
            return JSONObject()
                .put(
                    "success",
                    false
                )
                .put(
                    "message",
                    "The profile server returned non-JSON data. " +
                            "Check profile_details.php and Apache."
                )
                .put(
                    "http_code",
                    httpCode
                )
                .put(
                    "response_preview",
                    body.take(
                        500
                    )
                )
                .put(
                    "url",
                    urlText
                )
        }

        return try {
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
                    "Invalid profile-details JSON: " +
                            (
                                    exception.message
                                        ?: "Unknown JSON error"
                                    )
                )
                .put(
                    "http_code",
                    httpCode
                )
                .put(
                    "response_preview",
                    body.take(
                        500
                    )
                )
                .put(
                    "url",
                    urlText
                )
        }
    }
}