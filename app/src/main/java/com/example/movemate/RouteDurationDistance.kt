package com.example.movemate

/**
 * Calculates the route length from the selected workout duration.
 *
 * Formula:
 * distance in kilometres = speed in km/h × duration in hours
 *
 * Examples:
 * 6 km/h for 10 minutes = 1.00 km
 * 8 km/h for 10 minutes = 1.33 km
 * 15 km/h for 10 minutes = 2.50 km
 */
fun routeDistanceForDurationKm(
    speedKmh: Double,
    durationSeconds: Int
): Double {
    val safeSpeedKmh =
        speedKmh.coerceAtLeast(
            0.1
        )

    val safeDurationSeconds =
        durationSeconds.coerceAtLeast(
            1
        )

    return safeSpeedKmh *
            (
                    safeDurationSeconds.toDouble() /
                            3_600.0
                    )
}