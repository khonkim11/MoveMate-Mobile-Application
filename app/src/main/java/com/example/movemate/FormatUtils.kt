package com.example.movemate

import java.util.Locale

fun Number.workoutOneDecimal(): String {
    return String.format(Locale.US, "%.1f", this.toDouble())
}