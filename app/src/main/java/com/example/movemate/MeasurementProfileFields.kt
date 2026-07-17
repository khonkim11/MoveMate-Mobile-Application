package com.example.movemate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

class MeasurementProfileInputState internal constructor(
    initialSystem: MeasurementSystem,
    initialWeightKg: Double,
    initialHeightCm: Double
) {
    var system by mutableStateOf(
        initialSystem
    )
        private set

    var metricWeightText by mutableStateOf(
        inputNumber(
            initialWeightKg,
            1
        )
    )
        private set

    var metricHeightText by mutableStateOf(
        inputNumber(
            initialHeightCm,
            1
        )
    )
        private set

    var imperialWeightText by mutableStateOf(
        inputNumber(
            kilogramsToPounds(
                initialWeightKg
            ),
            1
        )
    )
        private set

    private val initialHeight =
        centimetersToFeetAndInches(
            initialHeightCm
        )

    var feetText by mutableStateOf(
        if (initialHeight.feet > 0) {
            initialHeight.feet.toString()
        } else {
            ""
        }
    )
        private set

    var inchesText by mutableStateOf(
        if (
            initialHeight.feet > 0 ||
            initialHeight.inches > 0
        ) {
            initialHeight.inches.toString()
        } else {
            ""
        }
    )
        private set

    fun selectSystem(
        newSystem: MeasurementSystem
    ) {
        if (newSystem == system) {
            return
        }

        val currentWeight =
            weightKgOrNull()

        val currentHeight =
            heightCmOrNull()

        currentWeight?.let {
            updateWeightFields(it)
        }

        currentHeight?.let {
            updateHeightFields(it)
        }

        system = newSystem
    }

    fun updateMetricWeight(
        value: String
    ) {
        metricWeightText =
            value.decimalInputOnly()
    }

    fun updateMetricHeight(
        value: String
    ) {
        metricHeightText =
            value.decimalInputOnly()
    }

    fun updateImperialWeight(
        value: String
    ) {
        imperialWeightText =
            value.decimalInputOnly()
    }

    fun updateFeet(
        value: String
    ) {
        feetText =
            value.filter(Char::isDigit)
                .take(1)
    }

    fun updateInches(
        value: String
    ) {
        inchesText =
            value.filter(Char::isDigit)
                .take(2)
    }

    fun weightKgOrNull(): Double? {
        return if (
            system ==
            MeasurementSystem.METRIC
        ) {
            metricWeightText
                .toDoubleOrNull()
        } else {
            imperialWeightText
                .toDoubleOrNull()
                ?.let(::poundsToKilograms)
        }
    }

    fun heightCmOrNull(): Double? {
        return if (
            system ==
            MeasurementSystem.METRIC
        ) {
            metricHeightText
                .toDoubleOrNull()
        } else {
            val feet =
                feetText.toIntOrNull()
                    ?: return null

            val inches =
                inchesText.toIntOrNull()
                    ?: return null

            if (inches !in 0..11) {
                return null
            }

            feetAndInchesToCentimeters(
                feet,
                inches
            )
        }
    }

    fun updateFromMetric(
        weightKg: Double,
        heightCm: Double
    ) {
        updateWeightFields(weightKg)
        updateHeightFields(heightCm)
    }

    private fun updateWeightFields(
        kilograms: Double
    ) {
        metricWeightText =
            inputNumber(
                kilograms,
                1
            )

        imperialWeightText =
            inputNumber(
                kilogramsToPounds(
                    kilograms
                ),
                1
            )
    }

    private fun updateHeightFields(
        centimeters: Double
    ) {
        metricHeightText =
            inputNumber(
                centimeters,
                1
            )

        val converted =
            centimetersToFeetAndInches(
                centimeters
            )

        feetText =
            converted.feet.toString()

        inchesText =
            converted.inches.toString()
    }
}

@Composable
fun rememberMeasurementProfileInputState(
    userId: Int,
    initialSystem: MeasurementSystem,
    initialWeightKg: Double,
    initialHeightCm: Double
): MeasurementProfileInputState {
    return remember(
        userId,
        initialWeightKg,
        initialHeightCm
    ) {
        MeasurementProfileInputState(
            initialSystem =
                initialSystem,
            initialWeightKg =
                initialWeightKg,
            initialHeightCm =
                initialHeightCm
        )
    }
}

@Composable
fun MeasurementProfileFields(
    state: MeasurementProfileInputState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {
        if (
            state.system ==
            MeasurementSystem.METRIC
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value =
                        state.metricWeightText,
                    onValueChange =
                        state::updateMetricWeight,
                    label = {
                        Text("Weight (kg)")
                    },
                    modifier =
                        Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Decimal
                        ),
                    shape =
                        RoundedCornerShape(
                            16.dp
                        )
                )

                OutlinedTextField(
                    value =
                        state.metricHeightText,
                    onValueChange =
                        state::updateMetricHeight,
                    label = {
                        Text("Height (cm)")
                    },
                    modifier =
                        Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Decimal
                        ),
                    shape =
                        RoundedCornerShape(
                            16.dp
                        )
                )
            }
        } else {
            OutlinedTextField(
                value =
                    state.imperialWeightText,
                onValueChange =
                    state::updateImperialWeight,
                label = {
                    Text("Weight (lb)")
                },
                modifier =
                    Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Decimal
                    ),
                shape =
                    RoundedCornerShape(
                        16.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state.feetText,
                    onValueChange =
                        state::updateFeet,
                    label = {
                        Text("Height (ft)")
                    },
                    modifier =
                        Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),
                    shape =
                        RoundedCornerShape(
                            16.dp
                        )
                )

                OutlinedTextField(
                    value =
                        state.inchesText,
                    onValueChange =
                        state::updateInches,
                    label = {
                        Text("Height (in)")
                    },
                    modifier =
                        Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),
                    shape =
                        RoundedCornerShape(
                            16.dp
                        )
                )
            }
        }
    }
}

private fun inputNumber(
    value: Double,
    decimals: Int
): String {
    if (value <= 0.0) {
        return ""
    }

    val formatted = String.format(
        Locale.US,
        "%.${decimals}f",
        value
    )

    return formatted
        .trimEnd('0')
        .trimEnd('.')
}

private fun String.decimalInputOnly(): String {
    var decimalUsed = false

    return filter { character ->
        when {
            character.isDigit() -> true

            character == '.' &&
                    !decimalUsed -> {
                decimalUsed = true
                true
            }

            else -> false
        }
    }
}
