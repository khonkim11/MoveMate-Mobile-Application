package com.example.movemate

import org.json.JSONObject
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

data class GoalCenterData(
    val goalType: String = "Maintain",
    val activityFocus: String = "Mixed",
    val intensity: String = "Normal",
    val schedule: String = "Daily",
    val dailyCaloriesTarget: String = "2000",
    val workoutCaloriesTarget: String = "250",
    val stepsTarget: String = "7500",
    val waterTargetMl: String = "2200",
    val workoutMinutesTarget: String = "30"
)

fun goalCenterBuildDefaultGoal(session: UserSession): GoalCenterData {
    val bmi = goalCenterBmiValue(session)

    val startingGoal = when {
        bmi > 0.0 && bmi < 18.5 -> GoalCenterData(
            goalType = "Healthy Gain",
            activityFocus = "Strength",
            intensity = "Beginner",
            schedule = "3 Days/Week"
        )

        bmi >= 25.0 -> GoalCenterData(
            goalType = "Fat Loss",
            activityFocus = "Walking",
            intensity = "Normal",
            schedule = "Daily"
        )

        else -> GoalCenterData(
            goalType = "Maintain",
            activityFocus = "Mixed",
            intensity = "Normal",
            schedule = "5 Days/Week"
        )
    }

    return startingGoal.goalCenterApplySuggestedTargets(session)
}

/**
 * General editable fitness suggestions based on profile and selected options.
 * These values are not medical prescriptions.
 */
fun GoalCenterData.goalCenterApplySuggestedTargets(
    session: UserSession
): GoalCenterData {
    var dailyCalories: Int
    var workoutCalories: Int
    var steps: Int
    var waterMl: Int
    var workoutMinutes: Int

    when (goalType) {
        "Fat Loss" -> {
            dailyCalories = 1750
            workoutCalories = 320
            steps = 8500
            waterMl = 2400
            workoutMinutes = 35
        }

        "Healthy Gain" -> {
            dailyCalories = 2250
            workoutCalories = 180
            steps = 6000
            waterMl = 2200
            workoutMinutes = 30
        }

        "Build Muscle" -> {
            dailyCalories = 2450
            workoutCalories = 240
            steps = 7000
            waterMl = 2500
            workoutMinutes = 45
        }

        else -> {
            dailyCalories = 2000
            workoutCalories = 250
            steps = 7500
            waterMl = 2200
            workoutMinutes = 30
        }
    }

    when (activityFocus) {
        "Walking" -> {
            workoutCalories -= 30
            steps += 1000
            workoutMinutes += 5
        }

        "Running" -> {
            workoutCalories += 120
            steps += 1500
            waterMl += 200
            workoutMinutes += 5
        }

        "Cycling" -> {
            workoutCalories += 90
            waterMl += 200
            workoutMinutes += 10
        }

        "Strength" -> {
            dailyCalories += 100
            workoutCalories += 40
            steps -= 500
            waterMl += 200
            workoutMinutes += 10
        }
    }

    when (intensity) {
        "Beginner" -> {
            workoutCalories = (workoutCalories * 0.80).roundToInt()
            steps -= 500
            workoutMinutes -= 5
        }

        "Hard" -> {
            dailyCalories += 100
            workoutCalories = (workoutCalories * 1.25).roundToInt()
            steps += 500
            waterMl += 300
            workoutMinutes += 10
        }
    }

    when (schedule) {
        "3 Days/Week" -> {
            workoutCalories += 40
            workoutMinutes += 10
        }

        "5 Days/Week" -> {
            workoutMinutes += 5
        }

        "Weekend" -> {
            workoutCalories += 80
            steps -= 500
            workoutMinutes += 15
        }
    }

    val bmi = goalCenterBmiValue(session)

    when {
        bmi > 0.0 && bmi < 18.5 &&
                goalType in setOf("Healthy Gain", "Build Muscle") -> {
            dailyCalories += 100
        }

        bmi >= 30.0 && goalType == "Fat Loss" -> {
            dailyCalories -= 100
            steps += 500
            waterMl += 200
        }
    }

    return copy(
        dailyCaloriesTarget = dailyCalories.coerceIn(1200, 3500).toString(),
        workoutCaloriesTarget = workoutCalories.coerceIn(100, 900).toString(),
        stepsTarget = steps.coerceIn(3000, 20000).toString(),
        waterTargetMl = waterMl.coerceIn(1500, 5000).toString(),
        workoutMinutesTarget = workoutMinutes.coerceIn(15, 120).toString()
    )
}

fun GoalCenterData.goalCenterHasCustomTargets(session: UserSession): Boolean {
    val suggested = goalCenterApplySuggestedTargets(session)

    return dailyCaloriesTarget != suggested.dailyCaloriesTarget ||
            workoutCaloriesTarget != suggested.workoutCaloriesTarget ||
            stepsTarget != suggested.stepsTarget ||
            waterTargetMl != suggested.waterTargetMl ||
            workoutMinutesTarget != suggested.workoutMinutesTarget
}

fun GoalCenterData.goalCenterClean(): GoalCenterData {
    fun cleanNumber(value: String, fallback: String): String {
        val cleaned = value.filter(Char::isDigit)
        return cleaned.ifBlank { fallback }
    }

    return copy(
        goalType = goalType.ifBlank { "Maintain" },
        activityFocus = activityFocus.ifBlank { "Mixed" },
        intensity = intensity.ifBlank { "Normal" },
        schedule = schedule.ifBlank { "Daily" },
        dailyCaloriesTarget =
            cleanNumber(dailyCaloriesTarget, "2000"),
        workoutCaloriesTarget =
            cleanNumber(workoutCaloriesTarget, "250"),
        stepsTarget =
            cleanNumber(stepsTarget, "7500"),
        waterTargetMl =
            cleanNumber(waterTargetMl, "2200"),
        workoutMinutesTarget =
            cleanNumber(workoutMinutesTarget, "30")
    )
}

fun GoalCenterData.goalCenterValidationMessage(): String? {
    val dailyCalories = dailyCaloriesTarget.goalCenterToInt()
    val workoutCalories = workoutCaloriesTarget.goalCenterToInt()
    val steps = stepsTarget.goalCenterToInt()
    val water = waterTargetMl.goalCenterToInt()
    val minutes = workoutMinutesTarget.goalCenterToInt()

    if (dailyCalories !in 1200..3500) {
        return "Daily calories must be between 1,200 and 3,500 kcal."
    }

    if (workoutCalories !in 100..900) {
        return "Workout calories must be between 100 and 900 kcal."
    }

    if (steps !in 3000..20000) {
        return "Steps must be between 3,000 and 20,000."
    }

    if (water !in 1500..5000) {
        return "Water must be between 1,500 and 5,000 ml."
    }

    if (minutes !in 15..120) {
        return "Workout time must be between 15 and 120 minutes."
    }

    return null
}

fun GoalCenterData.goalCenterHasValidTargets(): Boolean {
    return goalCenterValidationMessage() == null
}

fun goalCenterDataFromJson(
    json: JSONObject,
    session: UserSession
): GoalCenterData {
    val defaultGoal = goalCenterBuildDefaultGoal(session)

    fun flexibleInt(key: String, fallback: Int): Int {
        val value = json.opt(key)

        return when (value) {
            is Number -> value.toInt()
            is String -> value.trim().toIntOrNull() ?: fallback
            else -> fallback
        }
    }

    return defaultGoal.copy(
        goalType = json
            .optString("goal_type", defaultGoal.goalType)
            .ifBlank { defaultGoal.goalType },

        activityFocus = json
            .optString("activity_focus", defaultGoal.activityFocus)
            .ifBlank { defaultGoal.activityFocus },

        intensity = json
            .optString("intensity", defaultGoal.intensity)
            .ifBlank { defaultGoal.intensity },

        schedule = json
            .optString("schedule", defaultGoal.schedule)
            .ifBlank { defaultGoal.schedule },

        dailyCaloriesTarget = flexibleInt(
            "daily_calories_target",
            defaultGoal.dailyCaloriesTarget.toIntOrNull() ?: 2000
        ).toString(),

        workoutCaloriesTarget = flexibleInt(
            "workout_calories_target",
            defaultGoal.workoutCaloriesTarget.toIntOrNull() ?: 250
        ).toString(),

        stepsTarget = flexibleInt(
            "steps_target",
            defaultGoal.stepsTarget.toIntOrNull() ?: 7500
        ).toString(),

        waterTargetMl = flexibleInt(
            "water_target_ml",
            defaultGoal.waterTargetMl.toIntOrNull() ?: 2200
        ).toString(),

        workoutMinutesTarget = flexibleInt(
            "workout_minutes_target",
            defaultGoal.workoutMinutesTarget.toIntOrNull() ?: 30
        ).toString()
    ).goalCenterClean()
}

fun goalCenterBmiValue(session: UserSession): Double {
    val weight = session.weightKg
    val height = session.heightCm

    if (weight <= 0.0 || height <= 0.0) {
        return 0.0
    }

    val heightMetres = height / 100.0
    return weight / heightMetres.pow(2.0)
}

fun goalCenterBmiText(session: UserSession): String {
    val bmi = goalCenterBmiValue(session)

    return if (bmi > 0.0) {
        String.format(Locale.US, "%.1f", bmi)
    } else {
        "N/A"
    }
}

fun goalCenterBmiStatus(session: UserSession): String {
    val bmi = goalCenterBmiValue(session)

    return when {
        bmi <= 0.0 -> "Add profile data"
        bmi < 18.5 -> "Underweight"
        bmi < 25.0 -> "Normal"
        bmi < 30.0 -> "Overweight"
        else -> "Obesity range"
    }
}

fun String.goalCenterToInt(): Int {
    return trim().toIntOrNull() ?: 0
}

fun goalCenterAdvice(
    session: UserSession,
    goal: GoalCenterData
): String {
    val profileName = session.fullName.ifBlank { "User" }

    return when (goal.goalType) {
        "Fat Loss" ->
            "$profileName, focus on daily movement, steady exercise, and realistic targets."

        "Healthy Gain" ->
            "$profileName, focus on strength training, regular meals, and gradual progress."

        "Build Muscle" ->
            "$profileName, train consistently, recover well, and increase targets gradually."

        else ->
            "$profileName, keep a balanced routine and review your progress every week."
    }
}

fun goalCenterSuggestionReason(
    session: UserSession,
    goal: GoalCenterData
): String {
    val profileText =
        if (goalCenterBmiValue(session) > 0.0) {
            "your profile and BMI"
        } else {
            "your selected options"
        }

    return "MoveMate calculated these general suggestions from $profileText, " +
            "your ${goal.goalType.lowercase()} goal, " +
            "${goal.activityFocus.lowercase()} focus, " +
            "${goal.intensity.lowercase()} intensity, and " +
            "${goal.schedule.lowercase()} schedule."
}
