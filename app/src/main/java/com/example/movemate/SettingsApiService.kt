package com.example.movemate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object SettingsApiService {

    private const val BASE_URL =
        "http://10.0.2.2/movemate_api/"

    suspend fun updateAccountSettings(
        userId: Int,
        fullName: String,
        gender: String,
        age: Int,
        weightKg: Double,
        heightCm: Double,
        currentPassword: String,
        newPassword: String
    ): JSONObject = withContext(Dispatchers.IO) {
        postForm(
            endpoint = "account_settings.php",
            params = mapOf(
                "user_id" to userId.toString(),
                "full_name" to fullName.trim(),
                "gender" to gender.trim(),
                "age" to age.toString(),
                "weight_kg" to weightKg.toString(),
                "height_cm" to heightCm.toString(),
                "current_password" to currentPassword,
                "new_password" to newPassword
            )
        )
    }

    private fun postForm(
        endpoint: String,
        params: Map<String, String>
    ): JSONObject {
        val urlText = BASE_URL + endpoint
        val connection =
            URL(urlText).openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.doInput = true
            connection.doOutput = true
            connection.useCaches = false

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
                .bufferedWriter(Charsets.UTF_8)
                .use { writer ->
                    writer.write(
                        params.entries.joinToString("&") {
                                (key, value) ->
                            "${key.urlEncode()}=${value.urlEncode()}"
                        }
                    )
                    writer.flush()
                }

            connection.readSettingsJson(urlText)
        } catch (exception: Exception) {
            errorJson(
                message =
                    "Cannot connect to account settings: " +
                            (exception.message
                                ?: exception.javaClass.simpleName),
                url = urlText
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun String.urlEncode(): String {
        return URLEncoder.encode(
            this,
            Charsets.UTF_8.name()
        )
    }

    private fun HttpURLConnection.readSettingsJson(
        urlText: String
    ): JSONObject {
        val httpCode = responseCode

        val stream =
            if (httpCode in 200..299) {
                inputStream
            } else {
                errorStream ?: runCatching {
                    inputStream
                }.getOrNull()
            }

        val body = stream
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            .orEmpty()
            .trim()

        if (body.isBlank()) {
            return errorJson(
                message =
                    "The settings server returned an empty response.",
                url = urlText,
                httpCode = httpCode
            )
        }

        if (!body.startsWith("{")) {
            return errorJson(
                message =
                    "The settings server returned non-JSON data. " +
                            "Check account_settings.php and Apache error.log.",
                url = urlText,
                httpCode = httpCode,
                rawResponse = body
            )
        }

        return try {
            JSONObject(body).apply {
                if (!has("http_code")) {
                    put("http_code", httpCode)
                }
            }
        } catch (exception: Exception) {
            errorJson(
                message =
                    "Invalid settings JSON: " +
                            (exception.message ?: "Unknown error"),
                url = urlText,
                httpCode = httpCode,
                rawResponse = body
            )
        }
    }

    private fun errorJson(
        message: String,
        url: String,
        httpCode: Int? = null,
        rawResponse: String? = null
    ): JSONObject {
        return JSONObject().apply {
            put("success", false)
            put("message", message)
            put("url", url)

            httpCode?.let {
                put("http_code", it)
            }

            rawResponse?.let {
                put(
                    "raw_response_start",
                    it.take(700)
                )
            }
        }
    }
}