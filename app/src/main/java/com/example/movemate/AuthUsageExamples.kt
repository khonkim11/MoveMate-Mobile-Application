package com.example.movemate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

suspend fun registerMoveMateUser(
    fullName: String,
    email: String,
    password: String,
    gender: String,
    age: Int,
    weightKg: Double,
    heightCm: Double
): JSONObject = withContext(Dispatchers.IO) {
    ApiService.registerUser(
        fullName,
        email,
        password,
        gender,
        age,
        weightKg,
        heightCm
    )
}

suspend fun loginMoveMateUser(
    email: String,
    password: String
): JSONObject = withContext(Dispatchers.IO) {
    ApiService.loginUser(email, password)
}

suspend fun resetMoveMatePassword(
    email: String,
    newPassword: String
): JSONObject = withContext(Dispatchers.IO) {
    ApiService.forgotPassword(email, newPassword)
}
