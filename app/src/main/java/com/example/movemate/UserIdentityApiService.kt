package com.example.movemate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object UserIdentityApiService {

    private const val BASE_URL =
        "http://10.0.2.2/movemate_api/"

    suspend fun resolveUserId(
        email: String
    ): JSONObject =
        withContext(Dispatchers.IO) {
            val cleanEmail = email.trim()

            if (cleanEmail.isBlank()) {
                return@withContext errorJson(
                    "Email is missing."
                )
            }

            var connection: HttpURLConnection? = null

            try {
                connection =
                    URL(BASE_URL + "resolve_user.php")
                        .openConnection()
                            as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 12_000
                connection.readTimeout = 12_000
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

                val body =
                    "email=" +
                            URLEncoder.encode(
                                cleanEmail,
                                Charsets.UTF_8.name()
                            )

                connection.outputStream
                    .bufferedWriter(Charsets.UTF_8)
                    .use { writer ->
                        writer.write(body)
                        writer.flush()
                    }

                val code = connection.responseCode
                val stream =
                    if (code in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                val responseText =
                    stream
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.use { it.readText() }
                        .orEmpty()
                        .trim()

                if (responseText.startsWith("{")) {
                    JSONObject(responseText).apply {
                        if (!has("http_code")) {
                            put("http_code", code)
                        }
                    }
                } else {
                    errorJson(
                        "User resolver returned invalid data.",
                        code
                    )
                }
            } catch (exception: Exception) {
                errorJson(
                    "Cannot resolve user ID: " +
                            (exception.message ?: "Unknown error")
                )
            } finally {
                connection?.disconnect()
            }
        }

    private fun errorJson(
        message: String,
        httpCode: Int = 0
    ): JSONObject =
        JSONObject().apply {
            put("success", false)
            put("message", message)
            put("http_code", httpCode)
        }
}