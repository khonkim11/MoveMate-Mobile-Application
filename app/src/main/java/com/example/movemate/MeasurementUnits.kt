package com.example.movemate

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt

enum class MeasurementSystem {
    METRIC,
    IMPERIAL
}

data class FeetAndInches(
    val feet: Int,
    val inches: Int
)

private const val FITNESS_SETTINGS_PREFS =
    "movemate_fitness_settings"

private fun measurementUnitKey(
    userId: Int
): String {
    return "user_${userId}_metric_units"
}

val LocalMeasurementSystem =
    staticCompositionLocalOf {
        MeasurementSystem.METRIC
    }

fun loadMeasurementSystem(
    context: Context,
    userId: Int
): MeasurementSystem {
    if (userId <= 0) {
        return MeasurementSystem.METRIC
    }

    val metric = context
        .getSharedPreferences(
            FITNESS_SETTINGS_PREFS,
            Context.MODE_PRIVATE
        )
        .getBoolean(
            measurementUnitKey(userId),
            true
        )

    return if (metric) {
        MeasurementSystem.METRIC
    } else {
        MeasurementSystem.IMPERIAL
    }
}

fun saveMeasurementSystem(
    context: Context,
    userId: Int,
    system: MeasurementSystem
) {
    if (userId <= 0) {
        return
    }

    context
        .getSharedPreferences(
            FITNESS_SETTINGS_PREFS,
            Context.MODE_PRIVATE
        )
        .edit()
        .putBoolean(
            measurementUnitKey(userId),
            system == MeasurementSystem.METRIC
        )
        .apply()
}

@Composable
fun rememberMeasurementSystem(
    userId: Int
): MeasurementSystem {
    val context = LocalContext.current

    var system by remember(
        context,
        userId
    ) {
        mutableStateOf(
            loadMeasurementSystem(
                context,
                userId
            )
        )
    }

    DisposableEffect(
        context,
        userId
    ) {
        val preferences =
            context.getSharedPreferences(
                FITNESS_SETTINGS_PREFS,
                Context.MODE_PRIVATE
            )

        val listener =
            SharedPreferences
                .OnSharedPreferenceChangeListener {
                        _,
                        changedKey ->

                    if (
                        changedKey ==
                        measurementUnitKey(userId)
                    ) {
                        system =
                            loadMeasurementSystem(
                                context,
                                userId
                            )
                    }
                }

        preferences
            .registerOnSharedPreferenceChangeListener(
                listener
            )

        onDispose {
            preferences
                .unregisterOnSharedPreferenceChangeListener(
                    listener
                )
        }
    }

    return system
}

fun kilogramsToPounds(
    kilograms: Double
): Double {
    return kilograms * 2.2046226218
}

fun poundsToKilograms(
    pounds: Double
): Double {
    return pounds / 2.2046226218
}

fun centimetersToFeetAndInches(
    centimeters: Double
): FeetAndInches {
    if (centimeters <= 0.0) {
        return FeetAndInches(
            feet = 0,
            inches = 0
        )
    }

    val totalInches =
        centimeters / 2.54

    var feet =
        floor(totalInches / 12.0)
            .toInt()

    var inches =
        (totalInches - feet * 12)
            .roundToInt()

    if (inches >= 12) {
        feet += 1
        inches = 0
    }

    return FeetAndInches(
        feet = feet,
        inches = inches
    )
}

fun feetAndInchesToCentimeters(
    feet: Int,
    inches: Int
): Double {
    return (
            feet.coerceAtLeast(0) * 12 +
                    inches.coerceAtLeast(0)
            ) * 2.54
}

fun kilometersToMiles(
    kilometers: Double
): Double {
    return kilometers * 0.6213711922
}

fun milesToKilometers(
    miles: Double
): Double {
    return miles / 0.6213711922
}

fun kilometersPerHourToMilesPerHour(
    kilometersPerHour: Double
): Double {
    return kilometersPerHour *
            0.6213711922
}

fun millilitersToFluidOunces(
    milliliters: Double
): Double {
    return milliliters /
            29.5735295625
}

fun formatWeight(
    kilograms: Double,
    system: MeasurementSystem,
    decimals: Int = 1
): String {
    if (kilograms <= 0.0) {
        return "—"
    }

    return if (
        system == MeasurementSystem.METRIC
    ) {
        "${formatUnitNumber(kilograms, decimals)} kg"
    } else {
        "${
            formatUnitNumber(
                kilogramsToPounds(kilograms),
                decimals
            )
        } lb"
    }
}

fun formatHeight(
    centimeters: Double,
    system: MeasurementSystem
): String {
    if (centimeters <= 0.0) {
        return "—"
    }

    return if (
        system == MeasurementSystem.METRIC
    ) {
        "${
            formatUnitNumber(
                centimeters,
                1
            )
        } cm"
    } else {
        val height =
            centimetersToFeetAndInches(
                centimeters
            )

        "${height.feet} ft ${height.inches} in"
    }
}

fun formatDistance(
    kilometers: Double,
    system: MeasurementSystem,
    decimals: Int = 1
): String {
    return if (
        system == MeasurementSystem.METRIC
    ) {
        "${
            formatUnitNumber(
                kilometers,
                decimals
            )
        } km"
    } else {
        "${
            formatUnitNumber(
                kilometersToMiles(
                    kilometers
                ),
                decimals
            )
        } mi"
    }
}

fun formatDistanceValue(
    kilometers: Double,
    system: MeasurementSystem,
    decimals: Int = 1
): String {
    return if (
        system == MeasurementSystem.METRIC
    ) {
        formatUnitNumber(
            kilometers,
            decimals
        )
    } else {
        formatUnitNumber(
            kilometersToMiles(
                kilometers
            ),
            decimals
        )
    }
}

fun distanceUnitLabel(
    system: MeasurementSystem
): String {
    return if (
        system == MeasurementSystem.METRIC
    ) {
        "km"
    } else {
        "mi"
    }
}

fun formatSpeed(
    kilometersPerHour: Double,
    system: MeasurementSystem,
    decimals: Int = 1
): String {
    return if (
        system == MeasurementSystem.METRIC
    ) {
        "${
            formatUnitNumber(
                kilometersPerHour,
                decimals
            )
        } km/h"
    } else {
        "${
            formatUnitNumber(
                kilometersPerHourToMilesPerHour(
                    kilometersPerHour
                ),
                decimals
            )
        } mph"
    }
}

fun formatPace(
    minutesPerKilometer: Double,
    system: MeasurementSystem
): String {
    if (minutesPerKilometer <= 0.0) {
        return "—"
    }

    val displayedMinutes =
        if (
            system ==
            MeasurementSystem.METRIC
        ) {
            minutesPerKilometer
        } else {
            minutesPerKilometer *
                    1.609344
        }

    var wholeMinutes =
        floor(displayedMinutes)
            .toInt()

    var seconds =
        (
                (
                        displayedMinutes -
                                wholeMinutes
                        ) * 60.0
                ).roundToInt()

    if (seconds >= 60) {
        wholeMinutes += 1
        seconds = 0
    }

    val unit =
        if (
            system ==
            MeasurementSystem.METRIC
        ) {
            "/km"
        } else {
            "/mi"
        }

    return String.format(
        Locale.US,
        "%d:%02d %s",
        wholeMinutes,
        seconds,
        unit
    )
}

fun formatWater(
    milliliters: Double,
    system: MeasurementSystem
): String {
    return if (
        system == MeasurementSystem.METRIC
    ) {
        if (milliliters >= 1000.0) {
            "${
                formatUnitNumber(
                    milliliters / 1000.0,
                    1
                )
            } L"
        } else {
            "${
                formatUnitNumber(
                    milliliters,
                    0
                )
            } ml"
        }
    } else {
        "${
            formatUnitNumber(
                millilitersToFluidOunces(
                    milliliters
                ),
                1
            )
        } fl oz"
    }
}

fun formatUnitNumber(
    value: Double,
    decimals: Int
): String {
    return String.format(
        Locale.US,
        "%.${decimals}f",
        value
    )
}
