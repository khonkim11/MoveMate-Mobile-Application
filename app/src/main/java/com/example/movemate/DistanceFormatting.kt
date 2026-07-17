package com.example.movemate

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * The application stores and sends distance in kilometres, but all workout UI
 * displays the value in metres.
 */
fun formatDistanceMetersForUi(
    distanceKm: Double
): String {
    val metres = (
            distanceKm
                .coerceAtLeast(0.0) *
                    1_000.0
            )
        .roundToInt()

    return NumberFormat
        .getIntegerInstance(Locale.US)
        .format(metres) + " m"
}
