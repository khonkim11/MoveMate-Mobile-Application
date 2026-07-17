package com.example.movemate

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import java.util.Locale
import kotlin.math.abs

enum class MoveMateScreen {
    START,
    SIGNUP,
    LOGIN,
    FORGOT_PASSWORD,
    HOME,
    PROFILE,
    PROFILE_DETAILS,
    NOTIFICATIONS,
    SETTINGS,
    ACCESSIBILITY,
    ACTIVITY,
    LEVEL_SELECT,
    WORKOUT_SESSION,
    SUMMARY,
    PROGRESS,
    GOAL,
    INTRO,
    HEALTH,
    HISTORY
}

@Composable
fun MoveMateApp(
    context: Context
) {
    MaterialTheme {
        var session by remember {
            mutableStateOf(
                loadSession(
                    context
                )
            )
        }

        var screen by remember {
            mutableStateOf(
                if (
                    session ==
                    null
                ) {
                    MoveMateScreen.START
                } else {
                    MoveMateScreen.HOME
                }
            )
        }

        var selectedActivity by remember {
            mutableStateOf<WorkoutActivityOption?>(
                null
            )
        }

        var selectedLevel by remember {
            mutableStateOf<WorkoutLevel?>(
                null
            )
        }

        /*
         * Non-null only when Home is reopening an existing active workout.
         *
         * A new activity always clears this ID.
         */
        var selectedActiveWorkoutId by remember {
            mutableStateOf<String?>(
                null
            )
        }

        var showLogoutDialog by remember {
            mutableStateOf(
                false
            )
        }

        /*
         * Increment this after saved data changes so Home reloads its totals.
         */
        var homeRefreshToken by remember {
            mutableStateOf(
                0
            )
        }

        val screenHistory =
            remember {
                mutableStateListOf<MoveMateScreen>()
            }

        LaunchedEffect(
            session?.userId
        ) {
            MoveMateNotificationManager
                .createChannels(
                    context
                )

            val currentUserId =
                session
                    ?.userId
                    ?.takeIf {
                        it >
                                0
                    }
                    ?: return@LaunchedEffect

            val notificationSettings =
                loadMoveMateNotificationSettings(
                    context,
                    currentUserId
                )

            MoveMateNotificationManager
                .scheduleAll(
                    context,
                    currentUserId,
                    notificationSettings
                )
        }

        fun navigateTo(
            nextScreen: MoveMateScreen
        ) {
            if (
                screen !=
                nextScreen
            ) {
                screenHistory.add(
                    screen
                )

                screen =
                    nextScreen
            }
        }

        /*
         * Sets all workout launch data before changing the destination.
         *
         * Keeping this in one function prevents the session screen from
         * receiving a null activity or null level during navigation.
         */
        fun launchNewWorkout(
            activity: WorkoutActivityOption,
            level: WorkoutLevel
        ) {
            selectedActiveWorkoutId =
                null

            selectedActivity =
                activity

            selectedLevel =
                level

            if (
                screen !=
                MoveMateScreen.WORKOUT_SESSION
            ) {
                screenHistory.add(
                    screen
                )

                screen =
                    MoveMateScreen.WORKOUT_SESSION
            }
        }

        /*
         * Used by the workout screen's Home/back action.
         *
         * It deliberately does not pause, reset or remove the active workout.
         */
        fun goHomeKeepingWorkoutRunning() {
            homeRefreshToken +=
                1

            screenHistory.clear()

            screen =
                MoveMateScreen.HOME
        }

        fun goToStartAfterLogout() {
            /*
             * Remove active sessions before clearing the login.
             */
            session?.let {
                    currentSession ->

                ActiveWorkoutManager
                    .clearUser(
                        currentSession.userId
                    )
            }

            clearSession(
                context
            )

            session =
                null

            selectedActivity =
                null

            selectedLevel =
                null

            selectedActiveWorkoutId =
                null

            showLogoutDialog =
                false

            homeRefreshToken =
                0

            screenHistory.clear()

            screen =
                MoveMateScreen.START
        }

        fun goToHomeAfterAuth(
            userSession: UserSession
        ) {
            saveSession(
                context,
                userSession
            )

            session =
                userSession

            selectedActivity =
                null

            selectedLevel =
                null

            selectedActiveWorkoutId =
                null

            showLogoutDialog =
                false

            homeRefreshToken +=
                1

            screenHistory.clear()

            screen =
                MoveMateScreen.HOME
        }

        fun goBackOneStep() {
            if (
                showLogoutDialog
            ) {
                showLogoutDialog =
                    false

                return
            }

            if (
                screenHistory.isNotEmpty()
            ) {
                screen =
                    screenHistory.removeAt(
                        screenHistory.lastIndex
                    )

                return
            }

            screen =
                when (
                    screen
                ) {
                    MoveMateScreen.START ->
                        MoveMateScreen.START

                    MoveMateScreen.HOME ->
                        MoveMateScreen.HOME

                    else ->
                        if (
                            session ==
                            null
                        ) {
                            MoveMateScreen.START
                        } else {
                            MoveMateScreen.HOME
                        }
                }
        }

        BackHandler(
            enabled =
                true
        ) {
            when {
                showLogoutDialog -> {
                    showLogoutDialog =
                        false
                }

                screen ==
                        MoveMateScreen.HOME -> {
                    showLogoutDialog =
                        true
                }

                screen ==
                        MoveMateScreen.WORKOUT_SESSION -> {
                    /*
                     * Back from an active workout returns to Home while the
                     * manager session continues running.
                     */
                    goHomeKeepingWorkoutRunning()
                }

                else -> {
                    goBackOneStep()
                }
            }
        }

        val measurementSystem =
            rememberMeasurementSystem(
                session?.userId
                    ?: 0
            )

        val accessibilitySettings =
            rememberMoveMateAccessibilitySettings(
                session?.userId
                    ?: 0
            )

        MoveMateAccessibilityProvider(
            settings =
                accessibilitySettings
        ) {
            CompositionLocalProvider(
                LocalMeasurementSystem provides
                        measurementSystem
            ) {
                Surface(
                    modifier =
                        Modifier.fillMaxSize(),
                    color =
                        Color(0xFFF6F8FC)
                ) {
                    when (
                        screen
                    ) {
                        MoveMateScreen.INTRO,
                        MoveMateScreen.START -> {
                            StartScreen(
                                onSignUp = {
                                    navigateTo(
                                        MoveMateScreen.SIGNUP
                                    )
                                },
                                onLogin = {
                                    navigateTo(
                                        MoveMateScreen.LOGIN
                                    )
                                }
                            )
                        }

                        MoveMateScreen.SIGNUP -> {
                            SignUpScreen(
                                onBack = {
                                    goBackOneStep()
                                },
                                onRegistered = {
                                        user ->

                                    goToHomeAfterAuth(
                                        user
                                    )
                                }
                            )
                        }

                        MoveMateScreen.LOGIN -> {
                            LoginScreen(
                                onBack = {
                                    goBackOneStep()
                                },
                                onLoginSuccess = {
                                        user ->

                                    goToHomeAfterAuth(
                                        user
                                    )
                                },
                                onForgotPassword = {
                                    navigateTo(
                                        MoveMateScreen.FORGOT_PASSWORD
                                    )
                                },
                                onSignUp = {
                                    navigateTo(
                                        MoveMateScreen.SIGNUP
                                    )
                                }
                            )
                        }

                        MoveMateScreen.FORGOT_PASSWORD -> {
                            ForgotPasswordScreen(
                                onBack = {
                                    goBackOneStep()
                                },
                                onPasswordUpdated = {
                                    screenHistory.clear()

                                    screen =
                                        MoveMateScreen.LOGIN
                                }
                            )
                        }

                        MoveMateScreen.HOME -> {
                            val currentSession =
                                session

                            if (
                                currentSession ==
                                null
                            ) {
                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.START
                            } else {
                                HomeScreen(
                                    session =
                                        currentSession,

                                    /*
                                     * Home is the authenticated root.
                                     */
                                    onBack = {
                                        showLogoutDialog =
                                            true
                                    },

                                    /*
                                     * Start a completely new workout.
                                     */
                                    onActivity = {
                                        selectedActiveWorkoutId =
                                            null

                                        selectedActivity =
                                            null

                                        selectedLevel =
                                            null

                                        navigateTo(
                                            MoveMateScreen.ACTIVITY
                                        )
                                    },


                                    /*
                                     * Start a Home-only one-minute sample.
                                     *
                                     * It uses the shared WorkoutSessionScreen.
                                     */
                                    onStartSampleWorkout = {
                                            activity,
                                            sampleLevel ->

                                        launchNewWorkout(
                                            activity =
                                                activity,
                                            level =
                                                sampleLevel
                                        )
                                    },

                                    /*
                                     * Continue opens the exact active manager
                                     * entry, not the all-activities page.
                                     */
                                    onOpenActiveWorkout = {
                                            activeWorkout:
                                            ActiveWorkoutState ->

                                        val activity =
                                            findActivityForActiveWorkout(
                                                activeWorkout
                                            )

                                        val level =
                                            activity
                                                ?.let {
                                                        selected ->

                                                    findLevelForActiveWorkout(
                                                        activity =
                                                            selected,
                                                        activeWorkout =
                                                            activeWorkout
                                                    )
                                                }

                                        if (
                                            activity !=
                                            null &&
                                            level !=
                                            null
                                        ) {
                                            selectedActiveWorkoutId =
                                                activeWorkout.id

                                            selectedActivity =
                                                activity

                                            selectedLevel =
                                                level

                                            navigateTo(
                                                MoveMateScreen.WORKOUT_SESSION
                                            )
                                        } else {
                                            /*
                                             * This should occur only if the
                                             * activity definitions were
                                             * removed or renamed completely.
                                             */
                                            selectedActiveWorkoutId =
                                                null

                                            selectedActivity =
                                                null

                                            selectedLevel =
                                                null

                                            navigateTo(
                                                MoveMateScreen.ACTIVITY
                                            )
                                        }
                                    },

                                    onSummary = {
                                        navigateTo(
                                            MoveMateScreen.SUMMARY
                                        )
                                    },

                                    onProgress = {
                                        navigateTo(
                                            MoveMateScreen.PROGRESS
                                        )
                                    },

                                    onGoal = {
                                        navigateTo(
                                            MoveMateScreen.GOAL
                                        )
                                    },

                                    onHistory = {
                                        navigateTo(
                                            MoveMateScreen.HISTORY
                                        )
                                    },

                                    onHealth = {
                                        navigateTo(
                                            MoveMateScreen.HEALTH
                                        )
                                    },

                                    onProfile = {
                                        navigateTo(
                                            MoveMateScreen.PROFILE
                                        )
                                    },

                                    onLogout = {
                                        showLogoutDialog =
                                            true
                                    },

                                    refreshToken =
                                        homeRefreshToken
                                )
                            }
                        }

                        MoveMateScreen.PROFILE -> {
                            val currentSession =
                                session

                            if (
                                currentSession ==
                                null
                            ) {
                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.START
                            } else {
                                ProfileMenuScreen(
                                    session =
                                        currentSession,

                                    onBack = {
                                        goBackOneStep()
                                    },

                                    onGoals = {
                                        navigateTo(
                                            MoveMateScreen.GOAL
                                        )
                                    },

                                    onHealth = {
                                        navigateTo(
                                            MoveMateScreen.HEALTH
                                        )
                                    },

                                    onProfileDetails = {
                                        navigateTo(
                                            MoveMateScreen.PROFILE_DETAILS
                                        )
                                    },

                                    onNotification = {
                                        navigateTo(
                                            MoveMateScreen.NOTIFICATIONS
                                        )
                                    },

                                    onSettings = {
                                        navigateTo(
                                            MoveMateScreen.SETTINGS
                                        )
                                    },

                                    onAccessibilitySettings = {
                                        navigateTo(
                                            MoveMateScreen.ACCESSIBILITY
                                        )
                                    },

                                    onLogout = {
                                        showLogoutDialog =
                                            true
                                    },

                                    onHome = {
                                        screenHistory.clear()

                                        screen =
                                            MoveMateScreen.HOME
                                    },

                                    onActivity = {
                                        navigateTo(
                                            MoveMateScreen.ACTIVITY
                                        )
                                    },

                                    onSummary = {
                                        navigateTo(
                                            MoveMateScreen.SUMMARY
                                        )
                                    },

                                    onProgress = {
                                        navigateTo(
                                            MoveMateScreen.PROGRESS
                                        )
                                    }
                                )
                            }
                        }

                        MoveMateScreen.PROFILE_DETAILS -> {
                            val currentSession =
                                session

                            if (
                                currentSession ==
                                null
                            ) {
                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.START
                            } else {
                                ProfileDetailsScreen(
                                    session =
                                        currentSession,

                                    onBack = {
                                        goBackOneStep()
                                    },

                                    onHome = {
                                        screenHistory.clear()

                                        screen =
                                            MoveMateScreen.HOME
                                    }
                                )
                            }
                        }

                        MoveMateScreen.NOTIFICATIONS -> {
                            val currentSession =
                                session

                            if (
                                currentSession ==
                                null
                            ) {
                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.START
                            } else {
                                NotificationScreen(
                                    session =
                                        currentSession,

                                    onBack = {
                                        goBackOneStep()
                                    }
                                )
                            }
                        }

                        MoveMateScreen.ACCESSIBILITY -> {
                            val currentSession =
                                session

                            if (
                                currentSession ==
                                null
                            ) {
                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.START
                            } else {
                                AccessibilitySettingsScreen(
                                    session =
                                        currentSession,

                                    onBack = {
                                        goBackOneStep()
                                    },

                                    onStartWorkout = {
                                        selectedActiveWorkoutId =
                                            null

                                        selectedActivity =
                                            null

                                        selectedLevel =
                                            null

                                        navigateTo(
                                            MoveMateScreen.ACTIVITY
                                        )
                                    },

                                    onOpenHistory = {
                                        navigateTo(
                                            MoveMateScreen.HISTORY
                                        )
                                    }
                                )
                            }
                        }

                        MoveMateScreen.SETTINGS -> {
                            val currentSession =
                                session

                            if (
                                currentSession ==
                                null
                            ) {
                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.START
                            } else {
                                SettingsScreen(
                                    session =
                                        currentSession,

                                    onBack = {
                                        goBackOneStep()
                                    },

                                    onSessionUpdated = {
                                            updatedSession ->

                                        saveSession(
                                            context,
                                            updatedSession
                                        )

                                        session =
                                            updatedSession

                                        homeRefreshToken +=
                                            1
                                    },

                                    onLogout = {
                                        showLogoutDialog =
                                            true
                                    }
                                )
                            }
                        }

                        MoveMateScreen.HEALTH -> {
                            val currentSession = session

                            if (
                                currentSession == null
                            ) {
                                screenHistory.clear()
                                screen = MoveMateScreen.START
                            } else {
                                HealthSectionScreen(
                                    session = currentSession,

                                    onBack = {
                                        goBackOneStep()
                                    },

                                    onHome = {
                                        screenHistory.clear()
                                        screen = MoveMateScreen.HOME
                                    },

                                    onActivity = {
                                        navigateTo(
                                            MoveMateScreen.ACTIVITY
                                        )
                                    },

                                    onSummary = {
                                        navigateTo(
                                            MoveMateScreen.SUMMARY
                                        )
                                    },

                                    onProgress = {
                                        navigateTo(
                                            MoveMateScreen.PROGRESS
                                        )
                                    },

                                    onProfile = {
                                        navigateTo(
                                            MoveMateScreen.PROFILE
                                        )
                                    }
                                )
                            }
                        }

                        MoveMateScreen.ACTIVITY -> {
                            val currentSession =
                                session

                            if (
                                currentSession ==
                                null
                            ) {
                                selectedActiveWorkoutId =
                                    null

                                selectedActivity =
                                    null

                                selectedLevel =
                                    null

                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.START
                            } else {
                                ActivityScreen(
                                    userId =
                                        currentSession.userId,

                                    onBack = {
                                        goBackOneStep()
                                    },

                                    onHome = {
                                        screenHistory.clear()

                                        screen =
                                            MoveMateScreen.HOME
                                    },

                                    onSummary = {
                                        navigateTo(
                                            MoveMateScreen.SUMMARY
                                        )
                                    },

                                    onProgress = {
                                        navigateTo(
                                            MoveMateScreen.PROGRESS
                                        )
                                    },

                                    onGoal = {
                                        navigateTo(
                                            MoveMateScreen.GOAL
                                        )
                                    },

                                    onNotifications = {
                                        navigateTo(
                                            MoveMateScreen.NOTIFICATIONS
                                        )
                                    },

                                    onProfile = {
                                        navigateTo(
                                            MoveMateScreen.PROFILE
                                        )
                                    },

                                    onSelect = {
                                            activity ->

                                        /*
                                         * Normal workout:
                                         * Activity -> Easy / Medium / Hard.
                                         */
                                        selectedActiveWorkoutId =
                                            null

                                        selectedActivity =
                                            activity

                                        selectedLevel =
                                            null

                                        navigateTo(
                                            MoveMateScreen.LEVEL_SELECT
                                        )
                                    }
                                )
                            }
                        }

                        MoveMateScreen.LEVEL_SELECT -> {
                            val activity =
                                selectedActivity

                            if (
                                activity ==
                                null
                            ) {
                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.ACTIVITY
                            } else {
                                WorkoutLevelScreen(
                                    activityOption =
                                        activity,

                                    onStart = {
                                            selectedActivityOption:
                                            WorkoutActivityOption,
                                            selectedWorkoutLevel:
                                            WorkoutLevel ->

                                        launchNewWorkout(
                                            activity =
                                                selectedActivityOption,
                                            level =
                                                selectedWorkoutLevel
                                        )
                                    },

                                    onBack = {
                                        goBackOneStep()
                                    },

                                    onHome = {
                                        selectedActivity =
                                            null

                                        selectedLevel =
                                            null

                                        selectedActiveWorkoutId =
                                            null

                                        screenHistory.clear()

                                        screen =
                                            MoveMateScreen.HOME
                                    },

                                    onActivities = {
                                        selectedActivity =
                                            null

                                        selectedLevel =
                                            null

                                        selectedActiveWorkoutId =
                                            null

                                        screenHistory.clear()

                                        screen =
                                            MoveMateScreen.ACTIVITY
                                    },

                                    onProgress = {
                                        selectedActivity =
                                            null

                                        selectedLevel =
                                            null

                                        selectedActiveWorkoutId =
                                            null

                                        screenHistory.clear()

                                        screen =
                                            MoveMateScreen.PROGRESS
                                    },

                                    onProfile = {
                                        selectedActivity =
                                            null

                                        selectedLevel =
                                            null

                                        selectedActiveWorkoutId =
                                            null

                                        screenHistory.clear()

                                        screen =
                                            MoveMateScreen.PROFILE
                                    }
                                )
                            }
                        }


                        MoveMateScreen.WORKOUT_SESSION -> {
                            val currentSession =
                                session

                            val currentActivity =
                                selectedActivity

                            val currentLevel =
                                selectedLevel

                            if (
                                currentSession ==
                                null
                            ) {
                                selectedActivity =
                                    null

                                selectedLevel =
                                    null

                                selectedActiveWorkoutId =
                                    null

                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.START
                            } else if (
                                currentActivity ==
                                null ||
                                currentLevel ==
                                null
                            ) {
                                selectedActivity =
                                    null

                                selectedLevel =
                                    null

                                selectedActiveWorkoutId =
                                    null

                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.ACTIVITY
                            } else {
                                /*
                                 * One shared workout screen for every activity.
                                 *
                                 * Running, Walking and Cycling display
                                 * WorkoutRouteMap because tracksRoute is true.
                                 *
                                 * Weightlifting, Yoga, HIIT and Swimming
                                 * display ActivityMovementAnimation.
                                 */
                                WorkoutSessionScreen(
                                    session =
                                        currentSession,

                                    activity =
                                        currentActivity,

                                    level =
                                        currentLevel,

                                    existingWorkoutId =
                                        selectedActiveWorkoutId,

                                    onBack = {
                                        goHomeKeepingWorkoutRunning()
                                    },

                                    onViewTodaySummary = {
                                        selectedActiveWorkoutId =
                                            null

                                        selectedActivity =
                                            null

                                        selectedLevel =
                                            null

                                        homeRefreshToken +=
                                            1

                                        screenHistory.clear()

                                        screenHistory.add(
                                            MoveMateScreen.HOME
                                        )

                                        screen =
                                            MoveMateScreen.SUMMARY
                                    }
                                )
                            }
                        }

                        MoveMateScreen.SUMMARY -> {
                            val currentSession =
                                session

                            if (
                                currentSession ==
                                null
                            ) {
                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.START
                            } else {
                                WorkoutTodaySummaryScreen(
                                    session =
                                        currentSession,

                                    onBack = {
                                        goBackOneStep()
                                    },

                                    /*
                                     * The top Home icon behaves like the old
                                     * Back action and returns to the previous
                                     * page.
                                     */
                                    onTopHome = {
                                        goBackOneStep()
                                    },

                                    /*
                                     * Footer Home always opens the Home root.
                                     */
                                    onHome = {
                                        screenHistory.clear()

                                        screen =
                                            MoveMateScreen.HOME
                                    },

                                    onStartAnotherWorkout = {
                                        selectedActiveWorkoutId =
                                            null

                                        selectedActivity =
                                            null

                                        selectedLevel =
                                            null

                                        screenHistory.clear()

                                        screen =
                                            MoveMateScreen.ACTIVITY
                                    },

                                    onProgress = {
                                        screenHistory.clear()

                                        screen =
                                            MoveMateScreen.PROGRESS
                                    },

                                    onProfile = {
                                        screenHistory.clear()

                                        screen =
                                            MoveMateScreen.PROFILE
                                    }
                                )
                            }
                        }

                        MoveMateScreen.PROGRESS -> {
                            val currentSession = session

                            if (currentSession == null) {
                                screen = MoveMateScreen.START
                            } else {
                                ProgressScreen(
                                    userId = currentSession.userId,
                                    onBack = { goBackOneStep() },
                                    onHome = {
                                        screenHistory.clear()
                                        screen = MoveMateScreen.HOME
                                    },
                                    onActivity = {
                                        screenHistory.clear()
                                        screen = MoveMateScreen.ACTIVITY
                                    },
                                    onSummary = {
                                        screenHistory.clear()
                                        screen = MoveMateScreen.SUMMARY
                                    },
                                    onGoal = {
                                        screenHistory.clear()
                                        screen = MoveMateScreen.GOAL
                                    },
                                    onProfile = {
                                        screenHistory.clear()
                                        screen = MoveMateScreen.PROFILE
                                    }
                                )
                            }
                        }

                        MoveMateScreen.GOAL -> {
                            val currentSession =
                                session

                            if (
                                currentSession ==
                                null
                            ) {
                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.START
                            } else {
                                GoalCenterScreen(
                                    session =
                                        currentSession,

                                    onBack = {
                                        homeRefreshToken +=
                                            1

                                        goBackOneStep()
                                    }
                                )
                            }
                        }

                        MoveMateScreen.HISTORY -> {
                            val currentSession =
                                session

                            if (
                                currentSession ==
                                null
                            ) {
                                screenHistory.clear()

                                screen =
                                    MoveMateScreen.START
                            } else {
                                WorkoutHistoryScreen(
                                    session =
                                        currentSession,

                                    onBack = {
                                        goBackOneStep()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (
            showLogoutDialog
        ) {
            LogoutWarningDialog(
                onConfirmLogout = {
                    goToStartAfterLogout()
                },

                onCancelLogout = {
                    showLogoutDialog =
                        false
                }
            )
        }
    }
}

/**
 * Active workouts were originally created from workoutActivityOptions.
 * Match the saved manager name to the current option definition.
 */
private fun findActivityForActiveWorkout(
    activeWorkout: ActiveWorkoutState
): WorkoutActivityOption? {
    val activeName =
        normalizeMoveMateName(
            activeWorkout.activityName
        )

    return workoutActivityOptions
        .firstOrNull {
                option ->

            normalizeMoveMateName(
                option.name
            ) ==
                    activeName ||
                    normalizeMoveMateName(
                        option.shortName
                    ) ==
                    activeName
        }
}

/**
 * Supports level labels that were renamed between older and newer builds:
 *
 * Basic / Beginner / Easy
 * Intermediate / Normal / Medium
 * Advanced / Hard
 */
private fun findLevelForActiveWorkout(
    activity: WorkoutActivityOption,
    activeWorkout: ActiveWorkoutState
): WorkoutLevel? {
    val activeLevelName =
        normalizeMoveMateName(
            activeWorkout.levelName
        )


    /*
     * Sample levels are generated dynamically and are not stored in
     * activity.levels. Recreate the exact one-minute level when Home opens it.
     */
    if (
        activeLevelName ==
        "sample1min" ||
        activeLevelName ==
        "1minutesample"
    ) {
        return oneMinuteSampleLevel(
            activity
        )
    }

    return activity.levels
        .firstOrNull {
                level ->

            normalizeMoveMateName(
                level.name
            ) ==
                    activeLevelName
        }
        ?: activity.levels
            .firstOrNull {
                    level ->

                moveMateLevelGroup(
                    level.name
                ) ==
                        moveMateLevelGroup(
                            activeWorkout.levelName
                        )
            }
        ?: activity.levels
            .minByOrNull {
                    level ->

                abs(
                    level.targetMinutes -
                            (
                                    activeWorkout.targetSeconds /
                                            60
                                    )
                )
            }
}

private fun normalizeMoveMateName(
    value: String
): String {
    return value
        .trim()
        .lowercase(
            Locale.US
        )
        .replace(
            " ",
            ""
        )
        .replace(
            "-",
            ""
        )
        .replace(
            "_",
            ""
        )
}

private fun moveMateLevelGroup(
    value: String
): Int {
    return when (
        normalizeMoveMateName(
            value
        )
    ) {
        "easy",
        "basic",
        "beginner" ->
            1

        "medium",
        "normal",
        "intermediate" ->
            2

        "hard",
        "advanced" ->
            3

        else ->
            0
    }
}
