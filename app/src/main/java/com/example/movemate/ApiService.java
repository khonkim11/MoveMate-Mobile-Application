package com.example.movemate;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ApiService {

    private static final String BASE_URL =
            "http://10.0.2.2/movemate_api/";

    private ApiService() {
    }

    private static JSONObject error(String message) {
        JSONObject result = new JSONObject();

        try {
            result.put("success", false);
            result.put(
                    "message",
                    message == null || message.trim().isEmpty()
                            ? "Unknown error"
                            : message
            );
        } catch (Exception ignored) {
        }

        return result;
    }

    private static JSONObject postJson(
            String endpoint,
            JSONObject body
    ) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);

            connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );
            connection.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            try (
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(
                                    connection.getOutputStream(),
                                    StandardCharsets.UTF_8
                            )
                    )
            ) {
                writer.write(body.toString());
                writer.flush();
            }

            int responseCode = connection.getResponseCode();

            InputStream stream =
                    responseCode >= 200 && responseCode < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            String responseText = readStream(stream);

            if (responseText.trim().isEmpty()) {
                return error(
                        "Empty response from PHP server. HTTP " +
                                responseCode
                );
            }

            try {
                return new JSONObject(responseText);
            } catch (Exception jsonError) {
                return error(
                        "Invalid PHP JSON response: " + responseText
                );
            }

        } catch (Exception exception) {
            return error(
                    "Cannot connect to PHP server: " +
                            exception.getClass().getSimpleName() +
                            ": " +
                            exception.getMessage()
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static JSONObject getJson(String endpoint) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            int responseCode = connection.getResponseCode();

            InputStream stream =
                    responseCode >= 200 && responseCode < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            String responseText = readStream(stream);

            if (responseText.trim().isEmpty()) {
                return error(
                        "Empty response from PHP server. HTTP " +
                                responseCode
                );
            }

            try {
                return new JSONObject(responseText);
            } catch (Exception jsonError) {
                return error(
                        "Invalid PHP JSON response: " + responseText
                );
            }

        } catch (Exception exception) {
            return error(
                    "Cannot connect to PHP server: " +
                            exception.getClass().getSimpleName() +
                            ": " +
                            exception.getMessage()
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readStream(InputStream stream)
            throws Exception {
        if (stream == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                stream,
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    public static JSONObject testConnection() {
        return getJson("test_connection.php");
    }

    // Keeps compatibility with the existing SignUpScreen call.
    public static JSONObject registerUser(
            String fullName,
            String email,
            String password,
            String gender,
            int age,
            double weightValue,
            String weightUnit,
            double weightKg,
            double heightValue,
            String heightUnit,
            double heightCm,
            int heightFeet,
            int heightInches
    ) {
        return registerUser(
                fullName,
                email,
                password,
                gender,
                age,
                weightKg,
                heightCm
        );
    }

    public static JSONObject registerUser(
            String fullName,
            String email,
            String password,
            String gender,
            int age,
            double weightKg,
            double heightCm
    ) {
        try {
            JSONObject body = new JSONObject();

            body.put("full_name", clean(fullName));
            body.put("email", clean(email).toLowerCase());
            body.put("password", password == null ? "" : password);
            body.put("gender", clean(gender));
            body.put("age", age);
            body.put("weight_kg", roundTwo(weightKg));
            body.put("height_cm", roundTwo(heightCm));

            return postJson("register.php", body);

        } catch (Exception exception) {
            return error(exception.getMessage());
        }
    }

    public static JSONObject loginUser(
            String email,
            String password
    ) {
        try {
            JSONObject body = new JSONObject();

            body.put("email", clean(email).toLowerCase());
            body.put("password", password == null ? "" : password);

            return postJson("login.php", body);

        } catch (Exception exception) {
            return error(exception.getMessage());
        }
    }

    public static JSONObject forgotPassword(
            String email,
            String newPassword
    ) {
        try {
            JSONObject body = new JSONObject();

            body.put("email", clean(email).toLowerCase());
            body.put(
                    "new_password",
                    newPassword == null ? "" : newPassword
            );

            return postJson("forgot_password.php", body);

        } catch (Exception exception) {
            return error(exception.getMessage());
        }
    }

    // Compatibility for older screen code.
    public static JSONObject updatePassword(
            String email,
            String newPassword
    ) {
        return forgotPassword(email, newPassword);
    }

    public static JSONObject saveWorkout(
            int userId,
            String activityType,
            String workoutName,
            int durationSeconds,
            double distanceKm,
            double calories
    ) {
        try {
            JSONObject body = new JSONObject();

            body.put("user_id", userId);
            body.put("activity_type", clean(activityType));
            body.put("workout_name", clean(workoutName));
            body.put("duration_seconds", durationSeconds);
            body.put("distance_km", distanceKm);
            body.put("calories", calories);

            return postJson("save_workout.php", body);

        } catch (Exception exception) {
            return error(exception.getMessage());
        }
    }

    public static JSONObject getTodaySummary(int userId) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            return postJson("today_summary.php", body);
        } catch (Exception exception) {
            return error(exception.getMessage());
        }
    }

    public static JSONObject getProgress(int userId) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            return postJson("progress.php", body);
        } catch (Exception exception) {
            return error(exception.getMessage());
        }
    }

    public static JSONObject saveGoal(
            int userId,
            String goalType,
            double targetValue
    ) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("goal_type", clean(goalType));
            body.put("target_value", targetValue);
            return postJson("save_goal.php", body);
        } catch (Exception exception) {
            return error(exception.getMessage());
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static double roundTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
