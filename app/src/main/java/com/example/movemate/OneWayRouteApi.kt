package com.example.movemate

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class PlannedOneWayRoute(
    val points: List<LatLng>,
    val distanceMetres: Double,
    val durationSeconds: Double,
    val travelMode: String
)

object OneWayRouteApi {

    private const val BASE_URL =
        "http://10.0.2.2/movemate_api/"

    /**
     * Returns the route directly.
     *
     * Network, PHP and parsing errors are thrown as exceptions and handled by
     * WorkoutRouteMap with try/catch/finally.
     */
    suspend fun load(
        origin: LatLng,
        targetDistanceMetres: Double,
        activityName: String
    ): PlannedOneWayRoute =
        withContext(
            Dispatchers.IO
        ) {
            val mode =
                when (
                    activityName
                        .trim()
                        .lowercase(
                            java.util.Locale.US
                        )
                ) {
                    "cycling",
                    "bicycle",
                    "bike" ->
                        "BICYCLE"

                    else ->
                        "WALK"
                }

            val parameters =
                linkedMapOf(
                    "origin_lat" to
                            origin.latitude.toString(),
                    "origin_lng" to
                            origin.longitude.toString(),
                    "target_meters" to
                            targetDistanceMetres
                                .coerceIn(
                                    300.0,
                                    15_000.0
                                )
                                .toString(),
                    "travel_mode" to
                            mode
                )

            val formBody =
                parameters.entries
                    .joinToString(
                        separator = "&"
                    ) {
                            entry ->

                        encodeFormValue(
                            entry.key
                        ) +
                                "=" +
                                encodeFormValue(
                                    entry.value
                                )
                    }

            val connection =
                URL(
                    BASE_URL +
                            "compute_one_way_route.php"
                )
                    .openConnection()
                        as HttpURLConnection

            try {
                connection.requestMethod =
                    "POST"

                connection.connectTimeout =
                    20_000

                connection.readTimeout =
                    35_000

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
                    responseStream
                        ?.bufferedReader(
                            Charsets.UTF_8
                        )
                        ?.use {
                                reader ->

                            reader.readText()
                        }
                        .orEmpty()
                        .trim()

                if (
                    responseText.isBlank()
                ) {
                    throw IllegalStateException(
                        "Route server returned an empty response. HTTP $responseCode"
                    )
                }

                if (
                    !responseText.startsWith(
                        "{"
                    )
                ) {
                    throw IllegalStateException(
                        "Route server returned invalid JSON. HTTP $responseCode"
                    )
                }

                val json =
                    JSONObject(
                        responseText
                    )

                val success =
                    json.optBoolean(
                        "success",
                        false
                    )

                if (
                    !success
                ) {
                    val serverMessage =
                        json.optString(
                            "message",
                            "Could not create a one-way route."
                        )
                            .ifBlank {
                                "Could not create a one-way route."
                            }

                    throw IllegalStateException(
                        "$serverMessage HTTP $responseCode"
                    )
                }

                val encodedPolyline =
                    json.optString(
                        "encoded_polyline",
                        ""
                    )

                if (
                    encodedPolyline.isBlank()
                ) {
                    throw IllegalStateException(
                        "The route response does not contain a polyline."
                    )
                }

                val routePoints =
                    decodeOneWayPolyline(
                        encodedPolyline
                    )

                if (
                    routePoints.size <
                    2
                ) {
                    throw IllegalStateException(
                        "The route contains fewer than two points."
                    )
                }

                val measuredDistance =
                    json.optDouble(
                        "distance_meters",
                        0.0
                    )
                        .takeIf {
                            it >
                                    0.0
                        }
                        ?: calculateRouteLengthMetres(
                            routePoints
                        )

                PlannedOneWayRoute(
                    points =
                        routePoints,
                    distanceMetres =
                        measuredDistance,
                    durationSeconds =
                        json.optDouble(
                            "duration_seconds",
                            0.0
                        ),
                    travelMode =
                        json.optString(
                            "travel_mode",
                            mode
                        )
                )
            } finally {
                connection.disconnect()
            }
        }

    private fun encodeFormValue(
        value: String
    ): String {
        return URLEncoder.encode(
            value,
            Charsets.UTF_8.name()
        )
    }
}

private data class OneWayDecodedValue(
    val value: Int,
    val nextIndex: Int
)

private fun decodeOneWayPolyline(
    encoded: String
): List<LatLng> {
    if (
        encoded.isBlank()
    ) {
        return emptyList()
    }

    val points =
        mutableListOf<LatLng>()

    var index =
        0

    var latitude =
        0

    var longitude =
        0

    while (
        index <
        encoded.length
    ) {
        val latitudeResult =
            decodeOneWayValue(
                encoded =
                    encoded,
                startIndex =
                    index
            )

        index =
            latitudeResult.nextIndex

        latitude +=
            latitudeResult.value

        if (
            index >=
            encoded.length
        ) {
            break
        }

        val longitudeResult =
            decodeOneWayValue(
                encoded =
                    encoded,
                startIndex =
                    index
            )

        index =
            longitudeResult.nextIndex

        longitude +=
            longitudeResult.value

        points.add(
            LatLng(
                latitude /
                        100_000.0,
                longitude /
                        100_000.0
            )
        )
    }

    return points
}

private fun decodeOneWayValue(
    encoded: String,
    startIndex: Int
): OneWayDecodedValue {
    var index =
        startIndex

    var result =
        0

    var shift =
        0

    var currentByte:
            Int

    do {
        if (
            index >=
            encoded.length
        ) {
            throw IllegalArgumentException(
                "Invalid encoded route polyline."
            )
        }

        currentByte =
            encoded[index]
                .code -
                    63

        index +=
            1

        result =
            result or
                    (
                            (
                                    currentByte and
                                            0x1F
                                    )
                                    shl
                                    shift
                            )

        shift +=
            5
    } while (
        currentByte >=
        0x20
    )

    val decodedValue =
        if (
            (
                    result and
                            1
                    ) !=
            0
        ) {
            (
                    result shr
                            1
                    ).inv()
        } else {
            result shr
                    1
        }

    return OneWayDecodedValue(
        value =
            decodedValue,
        nextIndex =
            index
    )
}

private fun calculateRouteLengthMetres(
    points: List<LatLng>
): Double {
    var totalMetres =
        0.0

    for (
    index in
    0 until
            points.lastIndex
    ) {
        val results =
            FloatArray(
                1
            )

        android.location.Location
            .distanceBetween(
                points[index].latitude,
                points[index].longitude,
                points[index + 1].latitude,
                points[index + 1].longitude,
                results
            )

        totalMetres +=
            results[0]
                .toDouble()
    }

    return totalMetres
}