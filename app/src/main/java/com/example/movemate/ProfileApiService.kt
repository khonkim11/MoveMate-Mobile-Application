package com.example.movemate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ProfileApiService {

    private const val BASE_URL = "http://10.0.2.2/movemate_api/"

    suspend fun getProfile(userId: Int): UserSession? {
        return withContext(Dispatchers.IO) {
            try {
                val json = postForm(
                    urlString = BASE_URL + "get_profile.php",
                    params = mapOf(
                        "user_id" to userId.toString()
                    )
                )

                if (!json.optBoolean("success", false)) {
                    return@withContext null
                }

                val user = json.optJSONObject("user") ?: return@withContext null

                UserSession(
                    userId = user.optInt("id", userId),
                    fullName = user.optString("full_name", ""),
                    email = user.optString("email", ""),
                    gender = user.optString("gender", ""),
                    age = user.optInt("age", 0),
                    weightKg = user.optDouble("weight_kg", 0.0),
                    heightCm = user.optDouble("height_cm", 0.0)
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun postForm(
        urlString: String,
        params: Map<String, String>
    ): JSONObject {
        val postData = params.entries.joinToString("&") { entry ->
            "${URLEncoder.encode(entry.key, "UTF-8")}=${
                URLEncoder.encode(entry.value, "UTF-8")
            }"
        }

        val connection = URL(urlString).openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.setRequestProperty(
            "Content-Type",
            "application/x-www-form-urlencoded; charset=UTF-8"
        )
        connection.setRequestProperty("Accept", "application/json")

        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(postData)
            writer.flush()
        }

        val responseCode = connection.responseCode

        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }

        val responseText = BufferedReader(
            InputStreamReader(stream, Charsets.UTF_8)
        ).use { reader ->
            reader.readText()
        }

        connection.disconnect()

        val cleanResponse = responseText.trim()

        return if (cleanResponse.startsWith("{")) {
            JSONObject(cleanResponse)
        } else {
            JSONObject()
                .put("success", false)
                .put("message", "Server returned non-JSON response.")
        }
    }
}