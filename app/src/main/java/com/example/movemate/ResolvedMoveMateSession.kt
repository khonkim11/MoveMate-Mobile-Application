package com.example.movemate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class ResolvedMoveMateSessionState(
    val loading: Boolean,
    val session: UserSession,
    val repaired: Boolean,
    val errorMessage: String
)

/**
 * Repairs older saved sessions that contain a valid email but userId = 0.
 *
 * The repaired ID is saved back to SharedPreferences, so the next app launch
 * uses the correct database user immediately.
 */
@Composable
fun rememberResolvedMoveMateSession(
    originalSession: UserSession
): ResolvedMoveMateSessionState {
    val context =
        LocalContext.current

    var resolvedSession by remember(
        originalSession.userId,
        originalSession.email
    ) {
        mutableStateOf(
            originalSession
        )
    }

    var loading by remember(
        originalSession.userId,
        originalSession.email
    ) {
        mutableStateOf(
            originalSession.userId <= 0
        )
    }

    var repaired by remember(
        originalSession.userId,
        originalSession.email
    ) {
        mutableStateOf(
            false
        )
    }

    var errorMessage by remember(
        originalSession.userId,
        originalSession.email
    ) {
        mutableStateOf(
            ""
        )
    }

    LaunchedEffect(
        originalSession.userId,
        originalSession.email
    ) {
        if (
            originalSession.userId >
            0
        ) {
            resolvedSession =
                originalSession

            loading =
                false

            repaired =
                false

            errorMessage =
                ""

            return@LaunchedEffect
        }

        val email =
            originalSession.email
                .trim()

        if (
            email.isBlank()
        ) {
            loading =
                false

            errorMessage =
                "The saved login has no account email. Please log in again."

            return@LaunchedEffect
        }

        loading =
            true

        errorMessage =
            ""

        val response =
            MoveMateIdentityService
                .resolveUserId(
                    email
                )

        if (
            response.optBoolean(
                "success",
                false
            )
        ) {
            val userId =
                response.optInt(
                    "user_id",
                    0
                )

            if (
                userId >
                0
            ) {
                val repairedSession =
                    originalSession.copy(
                        userId =
                            userId
                    )

                resolvedSession =
                    repairedSession

                saveSession(
                    context =
                        context,
                    session =
                        repairedSession
                )

                repaired =
                    true
            } else {
                errorMessage =
                    "The account was found, but its user ID is invalid."
            }
        } else {
            errorMessage =
                response.optString(
                    "message",
                    "Could not repair the saved login."
                )
        }

        loading =
            false
    }

    return ResolvedMoveMateSessionState(
        loading =
            loading,
        session =
            resolvedSession,
        repaired =
            repaired,
        errorMessage =
            errorMessage
    )
}

object MoveMateIdentityService {

    private const val BASE_URL =
        "http://10.0.2.2/movemate_api/"

    suspend fun resolveUserId(
        email: String
    ): JSONObject =
        withContext(
            Dispatchers.IO
        ) {
            val cleanEmail =
                email.trim()

            if (
                cleanEmail.isBlank()
            ) {
                return@withContext JSONObject()
                    .put(
                        "success",
                        false
                    )
                    .put(
                        "message",
                        "Email is missing."
                    )
            }

            val endpoint =
                "resolve_user.php"

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
                    12_000

                connection.readTimeout =
                    12_000

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

                val formBody =
                    "email=" +
                            URLEncoder.encode(
                                cleanEmail,
                                Charsets.UTF_8.name()
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

                val responseCode =
                    connection.responseCode

                val stream =
                    if (
                        responseCode in
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
                    responseText.startsWith(
                        "{"
                    )
                ) {
                    JSONObject(
                        responseText
                    )
                        .apply {
                            put(
                                "http_code",
                                responseCode
                            )
                        }
                } else {
                    JSONObject()
                        .put(
                            "success",
                            false
                        )
                        .put(
                            "message",
                            "The user resolver returned invalid data."
                        )
                        .put(
                            "http_code",
                            responseCode
                        )
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
                        "Cannot resolve the saved account: " +
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
}
