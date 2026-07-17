package com.example.movemate

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONObject

private val GoalPageBackgroundTop =
    Color(0xFFEFF6FF)

private val GoalPageBackgroundMiddle =
    Color(0xFFF8FAFC)

private val GoalPageBackgroundBottom =
    Color(0xFFF5F3FF)

private val GoalInk =
    Color(0xFF0F172A)

private val GoalMuted =
    Color(0xFF64748B)

private val GoalPrimary =
    Color(0xFF2563EB)

private val GoalPrimaryDark =
    Color(0xFF1E3A8A)

private val GoalPurple =
    Color(0xFF7C3AED)

private val GoalCyan =
    Color(0xFF0891B2)

private val GoalGreen =
    Color(0xFF16A34A)

@Composable
fun GoalCenterScreen(
    session: UserSession,
    onBack: () -> Unit,
    onHome: () -> Unit =
        onBack,
    onActivity: () -> Unit = {},
    onProgress: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    BackHandler(
        onBack =
            onBack
    )

    val scope =
        rememberCoroutineScope()

    val resolvedState =
        rememberResolvedMoveMateSession(
            session
        )

    val activeSession =
        resolvedState.session

    var goal by remember(
        activeSession.userId,
        activeSession.email
    ) {
        mutableStateOf(
            goalCenterBuildDefaultGoal(
                activeSession
            )
        )
    }

    var savedGoal by remember(
        activeSession.userId,
        activeSession.email
    ) {
        mutableStateOf<GoalCenterData?>(
            null
        )
    }

    var editing by remember(
        activeSession.userId
    ) {
        mutableStateOf(
            true
        )
    }

    var loading by remember(
        activeSession.userId
    ) {
        mutableStateOf(
            true
        )
    }

    var saving by remember(
        activeSession.userId
    ) {
        mutableStateOf(
            false
        )
    }

    var message by remember(
        activeSession.userId
    ) {
        mutableStateOf(
            ""
        )
    }

    var messageIsError by remember(
        activeSession.userId
    ) {
        mutableStateOf(
            false
        )
    }

    var pageVisible by remember {
        mutableStateOf(
            false
        )
    }

    LaunchedEffect(
        Unit
    ) {
        pageVisible =
            true
    }

    LaunchedEffect(
        activeSession.userId,
        resolvedState.loading
    ) {
        if (
            resolvedState.loading
        ) {
            return@LaunchedEffect
        }

        if (
            activeSession.userId <=
            0
        ) {
            loading =
                false

            message =
                resolvedState
                    .errorMessage

            messageIsError =
                true

            return@LaunchedEffect
        }

        loading =
            true

        message =
            ""

        messageIsError =
            false

        try {
            val response =
                GoalCenterApiService
                    .getUserGoal(
                        activeSession.userId
                    )

            when {
                response.optBoolean(
                    "success",
                    false
                ) &&
                        response.optBoolean(
                            "exists",
                            false
                        ) -> {
                    val databaseGoal =
                        goalCenterDataFromJson(
                            json =
                                response.optJSONObject(
                                    "goal"
                                )
                                    ?: JSONObject(),
                            session =
                                activeSession
                        )

                    goal =
                        databaseGoal

                    savedGoal =
                        databaseGoal

                    editing =
                        false
                }

                response.optBoolean(
                    "success",
                    false
                ) -> {
                    val defaultGoal =
                        goalCenterBuildDefaultGoal(
                            activeSession
                        )

                    goal =
                        defaultGoal

                    savedGoal =
                        null

                    editing =
                        true
                }

                else -> {
                    message =
                        response.optString(
                            "message",
                            "Could not load your goal."
                        )

                    messageIsError =
                        true
                }
            }
        } catch (
            exception: Exception
        ) {
            message =
                "Could not load your goal: " +
                        (
                                exception.message
                                    ?: exception
                                        .javaClass
                                        .simpleName
                                )

            messageIsError =
                true
        } finally {
            loading =
                false
        }
    }

    val targetValues =
        listOf(
            goal.dailyCaloriesTarget,
            goal.workoutCaloriesTarget,
            goal.stepsTarget,
            goal.waterTargetMl,
            goal.workoutMinutesTarget
        )

    val completedTargetCount =
        targetValues.count {
            it.toDoubleOrNull()
                ?.let {
                        number ->

                    number >
                            0.0
                }
                ?: false
        }

    val animatedTargetCount by
    animateIntAsState(
        targetValue =
            completedTargetCount,
        animationSpec =
            tween(
                durationMillis =
                    550
            )
    )

    val completionFraction =
        (
                animatedTargetCount /
                        5f
                )
            .coerceIn(
                0f,
                1f
            )

    val animatedCompletion by
    animateFloatAsState(
        targetValue =
            completionFraction,
        animationSpec =
            tween(
                durationMillis =
                    650
            )
    )

    Scaffold(
        containerColor =
            Color.Transparent,
        topBar = {
            GoalTopAppBar(
                editing =
                    editing,
                hasSavedGoal =
                    savedGoal !=
                            null,
                onBack =
                    onBack
            )
        },
        bottomBar = {
            GoalFooterMenu(
                onHome =
                    onHome,
                onActivity =
                    onActivity,
                onProgress =
                    onProgress,
                onGoal = {},
                onProfile =
                    onProfile
            )
        }
    ) {
            scaffoldPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                GoalPageBackgroundTop,
                                GoalPageBackgroundMiddle,
                                GoalPageBackgroundBottom
                            )
                    )
                )
                .padding(
                    scaffoldPadding
                )
        ) {
            GoalPageDecorations()

            AnimatedVisibility(
                visible =
                    pageVisible,
                enter =
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    420
                            )
                    ) +
                            slideInVertically(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            460
                                    ),
                                initialOffsetY = {
                                    it /
                                            8
                                }
                            ),
                exit =
                    fadeOut() +
                            slideOutVertically {
                                it /
                                        8
                            }
            ) {
                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            start =
                                18.dp,
                            top =
                                12.dp,
                            end =
                                18.dp,
                            bottom =
                                28.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        )
                ) {
                    item {
                        GoalHeroCard(
                            session =
                                activeSession,
                            goal =
                                goal,
                            savedGoal =
                                savedGoal,
                            editing =
                                editing,
                            completion =
                                animatedCompletion
                        )
                    }

                    item {
                        AnimatedVisibility(
                            visible =
                                resolvedState.loading ||
                                        loading,
                            enter =
                                fadeIn(),
                            exit =
                                fadeOut()
                        ) {
                            GoalLoadingCard(
                                text =
                                    if (
                                        resolvedState.loading
                                    ) {
                                        "Repairing saved account..."
                                    } else {
                                        "Loading saved goal..."
                                    }
                            )
                        }
                    }

                    item {
                        AnimatedVisibility(
                            visible =
                                message.isNotBlank(),
                            enter =
                                fadeIn() +
                                        slideInVertically {
                                            it /
                                                    5
                                        },
                            exit =
                                fadeOut()
                        ) {
                            GoalMessageCard(
                                message =
                                    message,
                                isError =
                                    messageIsError
                            )
                        }
                    }

                    val currentSavedGoal =
                        savedGoal

                    if (
                        currentSavedGoal !=
                        null &&
                        !editing &&
                        !loading
                    ) {
                        item {
                            SavedGoalOverviewCard(
                                goal =
                                    currentSavedGoal,
                                onEdit = {
                                    goal =
                                        currentSavedGoal

                                    editing =
                                        true

                                    message =
                                        ""

                                    messageIsError =
                                        false
                                }
                            )
                        }

                        item {
                            GoalInformationCard(
                                goal =
                                    currentSavedGoal
                            )
                        }
                    }

                    if (
                        !loading &&
                        (
                                savedGoal ==
                                        null ||
                                        editing
                                )
                    ) {
                        item {
                            GoalStepHeader(
                                step =
                                    "01",
                                title =
                                    "Choose goal type",
                                subtitle =
                                    "Select the result you want to work toward."
                            )
                        }

                        item {
                            GoalChoiceCard(
                                options =
                                    listOf(
                                        "Fat Loss",
                                        "Maintain",
                                        "Healthy Gain",
                                        "Build Muscle"
                                    ),
                                selected =
                                    goal.goalType,
                                accentColor =
                                    GoalPrimary,
                                onSelect = {
                                        selected ->

                                    goal =
                                        goal.copy(
                                            goalType =
                                                selected
                                        )
                                            .goalCenterApplySuggestedTargets(
                                                activeSession
                                            )

                                    message =
                                        ""

                                    messageIsError =
                                        false
                                }
                            )
                        }

                        item {
                            GoalStepHeader(
                                step =
                                    "02",
                                title =
                                    "Choose activity focus",
                                subtitle =
                                    "Pick the activity style used for suggested targets."
                            )
                        }

                        item {
                            GoalChoiceCard(
                                options =
                                    listOf(
                                        "Walking",
                                        "Running",
                                        "Cycling",
                                        "Strength",
                                        "Mixed"
                                    ),
                                selected =
                                    goal.activityFocus,
                                accentColor =
                                    GoalCyan,
                                onSelect = {
                                        selected ->

                                    goal =
                                        goal.copy(
                                            activityFocus =
                                                selected
                                        )
                                            .goalCenterApplySuggestedTargets(
                                                activeSession
                                            )

                                    message =
                                        ""

                                    messageIsError =
                                        false
                                }
                            )
                        }

                        item {
                            GoalStepHeader(
                                step =
                                    "03",
                                title =
                                    "Choose intensity",
                                subtitle =
                                    "Use a level that matches your current fitness."
                            )
                        }

                        item {
                            GoalChoiceCard(
                                options =
                                    listOf(
                                        "Beginner",
                                        "Normal",
                                        "Hard"
                                    ),
                                selected =
                                    goal.intensity,
                                accentColor =
                                    GoalPurple,
                                onSelect = {
                                        selected ->

                                    goal =
                                        goal.copy(
                                            intensity =
                                                selected
                                        )
                                            .goalCenterApplySuggestedTargets(
                                                activeSession
                                            )

                                    message =
                                        ""

                                    messageIsError =
                                        false
                                }
                            )
                        }

                        item {
                            GoalStepHeader(
                                step =
                                    "04",
                                title =
                                    "Choose schedule",
                                subtitle =
                                    "Decide how often you plan to work toward the goal."
                            )
                        }

                        item {
                            GoalChoiceCard(
                                options =
                                    listOf(
                                        "Daily",
                                        "3 Days/Week",
                                        "5 Days/Week",
                                        "Weekend"
                                    ),
                                selected =
                                    goal.schedule,
                                accentColor =
                                    Color(0xFFF97316),
                                onSelect = {
                                        selected ->

                                    goal =
                                        goal.copy(
                                            schedule =
                                                selected
                                        )
                                            .goalCenterApplySuggestedTargets(
                                                activeSession
                                            )

                                    message =
                                        ""

                                    messageIsError =
                                        false
                                }
                            )
                        }

                        item {
                            GoalTargetsCard(
                                goal =
                                    goal,
                                completedTargetCount =
                                    animatedTargetCount,
                                onGoalChange = {
                                        updatedGoal ->

                                    goal =
                                        updatedGoal

                                    message =
                                        ""

                                    messageIsError =
                                        false
                                },
                                onSuggested = {
                                    goal =
                                        goal
                                            .goalCenterApplySuggestedTargets(
                                                activeSession
                                            )

                                    message =
                                        "Suggested targets applied. Review them before saving."

                                    messageIsError =
                                        false
                                },
                                onReset = {
                                    goal =
                                        goalCenterBuildDefaultGoal(
                                            activeSession
                                        )

                                    message =
                                        "Goal form reset to default values."

                                    messageIsError =
                                        false
                                }
                            )
                        }

                        item {
                            GoalSaveActions(
                                hasSavedGoal =
                                    savedGoal !=
                                            null,
                                saving =
                                    saving,
                                loading =
                                    loading ||
                                            resolvedState.loading,
                                onSave = {
                                    if (
                                        activeSession.userId <=
                                        0
                                    ) {
                                        message =
                                            "Could not find your database user account."

                                        messageIsError =
                                            true

                                        return@GoalSaveActions
                                    }

                                    val cleanGoal =
                                        goal.goalCenterClean()

                                    if (
                                        !cleanGoal
                                            .goalCenterHasValidTargets()
                                    ) {
                                        message =
                                            "All target values must be greater than 0."

                                        messageIsError =
                                            true

                                        return@GoalSaveActions
                                    }

                                    saving =
                                        true

                                    message =
                                        if (
                                            savedGoal ==
                                            null
                                        ) {
                                            "Saving goal..."
                                        } else {
                                            "Updating goal..."
                                        }

                                    messageIsError =
                                        false

                                    scope.launch {
                                        try {
                                            val response =
                                                GoalCenterApiService
                                                    .saveUserGoal(
                                                        userId =
                                                            activeSession.userId,
                                                        goal =
                                                            cleanGoal
                                                    )

                                            if (
                                                response.optBoolean(
                                                    "success",
                                                    false
                                                )
                                            ) {
                                                val savedFromServer =
                                                    response
                                                        .optJSONObject(
                                                            "goal"
                                                        )
                                                        ?.let {
                                                                json ->

                                                            goalCenterDataFromJson(
                                                                json =
                                                                    json,
                                                                session =
                                                                    activeSession
                                                            )
                                                        }
                                                        ?: cleanGoal

                                                goal =
                                                    savedFromServer

                                                savedGoal =
                                                    savedFromServer

                                                GoalRefreshBus
                                                    .notifyGoalChanged()

                                                editing =
                                                    false

                                                message =
                                                    "Goal saved. Home will use ${savedFromServer.workoutCaloriesTarget} kcal as today's workout-burn target."

                                                messageIsError =
                                                    false
                                            } else {
                                                message =
                                                    response.optString(
                                                        "message",
                                                        "Could not save goal."
                                                    )

                                                messageIsError =
                                                    true
                                            }
                                        } catch (
                                            exception: Exception
                                        ) {
                                            message =
                                                "Could not save goal: " +
                                                        (
                                                                exception.message
                                                                    ?: exception
                                                                        .javaClass
                                                                        .simpleName
                                                                )

                                            messageIsError =
                                                true
                                        } finally {
                                            saving =
                                                false
                                        }
                                    }
                                },
                                onCancel = {
                                    val oldGoal =
                                        savedGoal

                                    if (
                                        oldGoal !=
                                        null
                                    ) {
                                        goal =
                                            oldGoal

                                        editing =
                                            false

                                        message =
                                            ""

                                        messageIsError =
                                            false
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalTopAppBar(
    editing: Boolean,
    hasSavedGoal: Boolean,
    onBack: () -> Unit
) {
    Surface(
        color =
            Color.White
                .copy(
                    alpha =
                        0.96f
                ),
        shadowElevation =
            5.dp
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        start =
                            16.dp,
                        top =
                            10.dp,
                        end =
                            18.dp,
                        bottom =
                            10.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            GoalBackButton(
                onClick =
                    onBack
            )

            Column(
                modifier =
                    Modifier
                        .weight(
                            1f
                        )
                        .padding(
                            start =
                                13.dp
                        )
            ) {
                Text(
                    text =
                        "Goal Center",
                    color =
                        GoalInk,
                    fontSize =
                        21.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        when {
                            editing &&
                                    hasSavedGoal ->
                                "Editing your saved goal"

                            editing ->
                                "Create a personal fitness plan"

                            else ->
                                "Your saved fitness plan"
                        },
                    color =
                        GoalMuted,
                    fontSize =
                        10.sp
                )
            }

            Text(
                text =
                    when {
                        editing &&
                                hasSavedGoal ->
                            "EDITING"

                        editing ->
                            "NEW"

                        else ->
                            "SAVED"
                    },
                color =
                    when {
                        editing ->
                            GoalPrimary

                        else ->
                            GoalGreen
                    },
                fontSize =
                    9.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                modifier =
                    Modifier
                        .clip(
                            RoundedCornerShape(
                                999.dp
                            )
                        )
                        .background(
                            if (
                                editing
                            ) {
                                GoalPrimary
                                    .copy(
                                        alpha =
                                            0.10f
                                    )
                            } else {
                                GoalGreen
                                    .copy(
                                        alpha =
                                            0.10f
                                    )
                            }
                        )
                        .padding(
                            horizontal =
                                10.dp,
                            vertical =
                                7.dp
                        )
            )
        }
    }
}

@Composable
private fun GoalBackButton(
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource
        .collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (
                pressed
            ) {
                0.90f
            } else {
                1f
            },
        animationSpec =
            spring(
                dampingRatio =
                    0.80f,
                stiffness =
                    540f
            )
    )

    Box(
        modifier =
            Modifier
                .size(
                    44.dp
                )
                .scale(
                    scale
                )
                .clip(
                    CircleShape
                )
                .background(
                    GoalPrimary
                        .copy(
                            alpha =
                                0.10f
                        )
                )
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    onClick =
                        onClick
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                "‹",
            color =
                GoalPrimaryDark,
            fontSize =
                29.sp,
            fontWeight =
                FontWeight.Bold,
            modifier =
                Modifier.padding(
                    bottom =
                        3.dp
                )
        )
    }
}

@Composable
private fun GoalHeroCard(
    session: UserSession,
    goal: GoalCenterData,
    savedGoal: GoalCenterData?,
    editing: Boolean,
    completion: Float
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                28.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.Transparent
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    7.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors =
                            listOf(
                                Color(0xFF0F172A),
                                GoalPrimaryDark,
                                GoalPurple
                            )
                    )
                )
                .padding(
                    19.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    13.dp
                )
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(
                                55.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    18.dp
                                )
                            )
                            .background(
                                Color.White
                                    .copy(
                                        alpha =
                                            0.14f
                                    )
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            "🎯",
                        fontSize =
                            28.sp
                    )
                }

                Column(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                            .padding(
                                start =
                                    12.dp
                            )
                ) {
                    Text(
                        text =
                            session.fullName
                                .ifBlank {
                                    "MoveMate User"
                                },
                        color =
                            Color.White,
                        fontSize =
                            20.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            if (
                                savedGoal ==
                                null
                            ) {
                                "Build a realistic goal you can follow."
                            } else if (
                                editing
                            ) {
                                "Update the plan and save your changes."
                            } else {
                                "${goal.goalType} • ${goal.activityFocus}"
                            },
                        color =
                            Color.White
                                .copy(
                                    alpha =
                                        0.76f
                                ),
                        fontSize =
                            11.sp
                    )
                }
            }

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                GoalHeroMetric(
                    label =
                        "GOAL",
                    value =
                        goal.goalType,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                GoalHeroMetric(
                    label =
                        "FOCUS",
                    value =
                        goal.activityFocus,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                GoalHeroMetric(
                    label =
                        "LEVEL",
                    value =
                        goal.intensity,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )
            }

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        text =
                            "Target readiness",
                        color =
                            Color.White
                                .copy(
                                    alpha =
                                        0.66f
                                ),
                        fontSize =
                            10.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    Text(
                        text =
                            "${(completion * 100f).toInt()}%",
                        color =
                            Color.White,
                        fontSize =
                            11.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                7.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    999.dp
                                )
                            )
                            .background(
                                Color.White
                                    .copy(
                                        alpha =
                                            0.14f
                                    )
                            )
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(
                                    fraction =
                                        completion
                                )
                                .height(
                                    7.dp
                                )
                                .clip(
                                    RoundedCornerShape(
                                        999.dp
                                    )
                                )
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF38BDF8),
                                            Color(0xFF60A5FA),
                                            Color(0xFFC084FC)
                                        )
                                    )
                                )
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalHeroMetric(
    label: String,
    value: String,
    modifier: Modifier
) {
    Column(
        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        15.dp
                    )
                )
                .background(
                    Color.White
                        .copy(
                            alpha =
                                0.09f
                        )
                )
                .padding(
                    horizontal =
                        8.dp,
                    vertical =
                        10.dp
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                label,
            color =
                Color.White
                    .copy(
                        alpha =
                            0.54f
                    ),
            fontSize =
                8.sp,
            fontWeight =
                FontWeight.ExtraBold
        )

        Text(
            text =
                value,
            color =
                Color.White,
            fontSize =
                11.sp,
            fontWeight =
                FontWeight.ExtraBold,
            textAlign =
                TextAlign.Center,
            maxLines =
                1,
            modifier =
                Modifier.padding(
                    top =
                        3.dp
                )
        )
    }
}

@Composable
private fun GoalLoadingCard(
    text: String
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                20.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    11.dp
                )
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(
                        23.dp
                    ),
                color =
                    GoalPrimary,
                strokeWidth =
                    3.dp
            )

            Text(
                text =
                    text,
                color =
                    GoalMuted,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GoalMessageCard(
    message: String,
    isError: Boolean
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                20.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (
                        isError
                    ) {
                        Color(0xFFFFF1F2)
                    } else {
                        Color(0xFFF0FDF4)
                    }
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    15.dp
                ),
            verticalAlignment =
                Alignment.Top
        ) {
            Text(
                text =
                    if (
                        isError
                    ) {
                        "!"
                    } else {
                        "✓"
                    },
                color =
                    if (
                        isError
                    ) {
                        Color(0xFFBE123C)
                    } else {
                        GoalGreen
                    },
                fontSize =
                    17.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    message,
                color =
                    if (
                        isError
                    ) {
                        Color(0xFF9F1239)
                    } else {
                        Color(0xFF166534)
                    },
                fontSize =
                    12.sp,
                fontWeight =
                    FontWeight.Bold,
                modifier =
                    Modifier.padding(
                        start =
                            10.dp
                    )
            )
        }
    }
}

@Composable
private fun GoalStepHeader(
    step: String,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        39.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            13.dp
                        )
                    )
                    .background(
                        GoalPrimary
                            .copy(
                                alpha =
                                    0.10f
                            )
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    step,
                color =
                    GoalPrimary,
                fontSize =
                    11.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }

        Column(
            modifier =
                Modifier.padding(
                    start =
                        11.dp
                )
        ) {
            Text(
                text =
                    title,
                color =
                    GoalInk,
                fontSize =
                    17.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    subtitle,
                color =
                    GoalMuted,
                fontSize =
                    10.sp
            )
        }
    }
}

@Composable
private fun GoalChoiceCard(
    options: List<String>,
    selected: String,
    accentColor: Color,
    onSelect: (String) -> Unit
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize(),
        shape =
            RoundedCornerShape(
                23.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    14.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    9.dp
                )
        ) {
            options
                .chunked(
                    2
                )
                .forEach {
                        row ->

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                9.dp
                            )
                    ) {
                        row.forEach {
                                option ->

                            GoalOptionButton(
                                text =
                                    option,
                                selected =
                                    option ==
                                            selected,
                                accentColor =
                                    accentColor,
                                onClick = {
                                    onSelect(
                                        option
                                    )
                                },
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )
                        }

                        if (
                            row.size <
                            2
                        ) {
                            Spacer(
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun GoalOptionButton(
    text: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource
        .collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (
                pressed
            ) {
                0.96f
            } else {
                1f
            },
        animationSpec =
            spring(
                dampingRatio =
                    0.84f,
                stiffness =
                    520f
            )
    )

    val elevation by
    animateDpAsState(
        targetValue =
            if (
                selected
            ) {
                2.dp
            } else {
                0.dp
            },
        animationSpec =
            tween(
                durationMillis =
                    180
            )
    )

    Surface(
        modifier =
            modifier
                .scale(
                    scale
                )
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    onClick =
                        onClick
                ),
        shape =
            RoundedCornerShape(
                16.dp
            ),
        color =
            if (
                selected
            ) {
                accentColor
            } else {
                accentColor
                    .copy(
                        alpha =
                            0.085f
                    )
            },
        shadowElevation =
            elevation
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min =
                            49.dp
                    )
                    .padding(
                        horizontal =
                            10.dp,
                        vertical =
                            10.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.Center
        ) {
            if (
                selected
            ) {
                Text(
                    text =
                        "✓",
                    color =
                        Color.White,
                    fontSize =
                        12.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    modifier =
                        Modifier.padding(
                            end =
                                6.dp
                        )
                )
            }

            Text(
                text =
                    text,
                color =
                    if (
                        selected
                    ) {
                        Color.White
                    } else {
                        accentColor
                    },
                fontSize =
                    12.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                textAlign =
                    TextAlign.Center
            )
        }
    }
}

@Composable
private fun GoalTargetsCard(
    goal: GoalCenterData,
    completedTargetCount: Int,
    onGoalChange:
        (
        GoalCenterData
    ) -> Unit,
    onSuggested: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                26.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    4.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    17.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    13.dp
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Text(
                        text =
                            "Daily targets",
                        color =
                            GoalInk,
                        fontSize =
                            19.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            "Set realistic values. All five targets are required.",
                        color =
                            GoalMuted,
                        fontSize =
                            11.sp
                    )
                }

                Text(
                    text =
                        "$completedTargetCount / 5",
                    color =
                        GoalPrimary,
                    fontSize =
                        12.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    modifier =
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    999.dp
                                )
                            )
                            .background(
                                GoalPrimary
                                    .copy(
                                        alpha =
                                            0.10f
                                    )
                            )
                            .padding(
                                horizontal =
                                    10.dp,
                                vertical =
                                    7.dp
                            )
                )
            }

            GoalNumberField(
                label =
                    "Daily Food / Energy Target",
                helper =
                    "Food and intake target; this is not burned calories.",
                suffix =
                    "kcal",
                value =
                    goal.dailyCaloriesTarget,
                onValueChange = {
                    onGoalChange(
                        goal.copy(
                            dailyCaloriesTarget =
                                it
                        )
                    )
                }
            )

            GoalNumberField(
                label =
                    "Daily Workout Calories Burn",
                helper =
                    "Home uses this exact value for workout progress.",
                suffix =
                    "kcal",
                value =
                    goal.workoutCaloriesTarget,
                onValueChange = {
                    onGoalChange(
                        goal.copy(
                            workoutCaloriesTarget =
                                it
                        )
                    )
                }
            )

            GoalNumberField(
                label =
                    "Steps Target",
                helper =
                    "Your total movement target for one day.",
                suffix =
                    "steps",
                value =
                    goal.stepsTarget,
                onValueChange = {
                    onGoalChange(
                        goal.copy(
                            stepsTarget =
                                it
                        )
                    )
                }
            )

            GoalNumberField(
                label =
                    "Water Target",
                helper =
                    "Daily hydration target.",
                suffix =
                    "ml",
                value =
                    goal.waterTargetMl,
                onValueChange = {
                    onGoalChange(
                        goal.copy(
                            waterTargetMl =
                                it
                        )
                    )
                }
            )

            GoalNumberField(
                label =
                    "Workout Minutes",
                helper =
                    "Planned active workout time per day.",
                suffix =
                    "min",
                value =
                    goal.workoutMinutesTarget,
                onValueChange = {
                    onGoalChange(
                        goal.copy(
                            workoutMinutesTarget =
                                it
                        )
                    )
                }
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        9.dp
                    )
            ) {
                OutlinedButton(
                    onClick =
                        onSuggested,
                    modifier = Modifier
                        .weight(
                            1f
                        )
                        .height(
                            50.dp
                        ),
                    shape =
                        RoundedCornerShape(
                            16.dp
                        )
                ) {
                    Text(
                        text =
                            "Suggested",
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick =
                        onReset,
                    modifier = Modifier
                        .weight(
                            1f
                        )
                        .height(
                            50.dp
                        ),
                    shape =
                        RoundedCornerShape(
                            16.dp
                        )
                ) {
                    Text(
                        text =
                            "Reset",
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalNumberField(
    label: String,
    helper: String,
    suffix: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                5.dp
            )
    ) {
        OutlinedTextField(
            value =
                value,
            onValueChange = {
                    input ->

                onValueChange(
                    input.filter {
                        it.isDigit()
                    }
                )
            },
            label = {
                Text(
                    text =
                        label
                )
            },
            trailingIcon = {
                Text(
                    text =
                        suffix,
                    color =
                        GoalPrimary,
                    fontSize =
                        10.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    modifier =
                        Modifier.padding(
                            end =
                                10.dp
                        )
                )
            },
            modifier =
                Modifier.fillMaxWidth(),
            singleLine =
                true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Number
                ),
            shape =
                RoundedCornerShape(
                    17.dp
                )
        )

        Text(
            text =
                helper,
            color =
                GoalMuted,
            fontSize =
                10.sp,
            modifier =
                Modifier.padding(
                    start =
                        9.dp
                )
        )
    }
}

@Composable
private fun GoalSaveActions(
    hasSavedGoal: Boolean,
    saving: Boolean,
    loading: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                9.dp
            )
    ) {
        Button(
            onClick =
                onSave,
            enabled =
                !saving &&
                        !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    58.dp
                ),
            shape =
                RoundedCornerShape(
                    19.dp
                ),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        GoalPrimary,
                    disabledContainerColor =
                        GoalPrimary
                            .copy(
                                alpha =
                                    0.45f
                            )
                )
        ) {
            if (
                saving
            ) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(
                            22.dp
                        ),
                    color =
                        Color.White,
                    strokeWidth =
                        3.dp
                )

                Text(
                    text =
                        "Saving goal...",
                    color =
                        Color.White,
                    fontWeight =
                        FontWeight.ExtraBold,
                    modifier =
                        Modifier.padding(
                            start =
                                9.dp
                        )
                )
            } else {
                Text(
                    text =
                        if (
                            hasSavedGoal
                        ) {
                            "Update Goal"
                        } else {
                            "Save Goal"
                        },
                    color =
                        Color.White,
                    fontSize =
                        16.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }

        if (
            hasSavedGoal
        ) {
            OutlinedButton(
                onClick =
                    onCancel,
                enabled =
                    !saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        50.dp
                    ),
                shape =
                    RoundedCornerShape(
                        17.dp
                    )
            ) {
                Text(
                    text =
                        "Cancel Changes",
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SavedGoalOverviewCard(
    goal: GoalCenterData,
    onEdit: () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                27.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.Transparent
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    7.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF0F172A),
                            GoalPrimaryDark,
                            GoalCyan
                        )
                    )
                )
                .padding(
                    20.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    11.dp
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Text(
                        text =
                            "Saved fitness goal",
                        color =
                            Color.White,
                        fontSize =
                            22.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            "${goal.goalType} • ${goal.activityFocus} • ${goal.intensity}",
                        color =
                            Color(0xFFE0F2FE),
                        fontSize =
                            11.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Text(
                    text =
                        "ACTIVE",
                    color =
                        Color.White,
                    fontSize =
                        9.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    modifier =
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    999.dp
                                )
                            )
                            .background(
                                GoalGreen
                            )
                            .padding(
                                horizontal =
                                    10.dp,
                                vertical =
                                    7.dp
                            )
                )
            }

            SavedGoalLine(
                label =
                    "Daily food / energy",
                value =
                    "${goal.dailyCaloriesTarget} kcal"
            )

            SavedGoalLine(
                label =
                    "Daily workout burn",
                value =
                    "${goal.workoutCaloriesTarget} kcal"
            )

            SavedGoalLine(
                label =
                    "Steps target",
                value =
                    "${goal.stepsTarget} steps"
            )

            SavedGoalLine(
                label =
                    "Water target",
                value =
                    "${goal.waterTargetMl} ml"
            )

            SavedGoalLine(
                label =
                    "Workout time",
                value =
                    "${goal.workoutMinutesTarget} min"
            )

            Text(
                text =
                    "Home uses ${goal.workoutCaloriesTarget} kcal for the workout progress bar.",
                color =
                    Color(0xFFFFE29A),
                fontSize =
                    10.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Button(
                onClick =
                    onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        51.dp
                    ),
                shape =
                    RoundedCornerShape(
                        16.dp
                    ),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color.White,
                        contentColor =
                            GoalPrimaryDark
                    )
            ) {
                Text(
                    text =
                        "Edit Goal",
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun SavedGoalLine(
    label: String,
    value: String
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Text(
            text =
                label,
            color =
                Color.White
                    .copy(
                        alpha =
                            0.70f
                    ),
            fontSize =
                11.sp,
            modifier =
                Modifier.weight(
                    1f
                )
        )

        Text(
            text =
                value,
            color =
                Color.White,
            fontSize =
                12.sp,
            fontWeight =
                FontWeight.ExtraBold,
            textAlign =
                TextAlign.End
        )
    }
}

@Composable
private fun GoalInformationCard(
    goal: GoalCenterData
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            Text(
                text =
                    "How your goal is used",
                color =
                    GoalInk,
                fontSize =
                    16.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            GoalInfoRow(
                icon =
                    "🔥",
                title =
                    "Home progress",
                text =
                    "${goal.workoutCaloriesTarget} kcal is used as today's workout-burn target."
            )

            GoalInfoRow(
                icon =
                    "🏃",
                title =
                    "Workout planning",
                text =
                    "${goal.workoutMinutesTarget} minutes and ${goal.activityFocus} guide your daily workout plan."
            )

            GoalInfoRow(
                icon =
                    "💧",
                title =
                    "Daily wellbeing",
                text =
                    "${goal.stepsTarget} steps and ${goal.waterTargetMl} ml support your movement and hydration targets."
            )
        }
    }
}

@Composable
private fun GoalInfoRow(
    icon: String,
    title: String,
    text: String
) {
    Row(
        verticalAlignment =
            Alignment.Top
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        36.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            12.dp
                        )
                    )
                    .background(
                        GoalPrimary
                            .copy(
                                alpha =
                                    0.09f
                            )
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    icon,
                fontSize =
                    17.sp
            )
        }

        Column(
            modifier =
                Modifier
                    .padding(
                        start =
                            10.dp
                    )
                    .weight(
                        1f
                    )
        ) {
            Text(
                text =
                    title,
                color =
                    GoalInk,
                fontSize =
                    12.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    text,
                color =
                    GoalMuted,
                fontSize =
                    11.sp,
                lineHeight =
                    16.sp,
                modifier =
                    Modifier.padding(
                        top =
                            2.dp
                    )
            )
        }
    }
}

@Composable
private fun GoalFooterMenu(
    onHome: () -> Unit,
    onActivity: () -> Unit,
    onProgress: () -> Unit,
    onGoal: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        color =
            Color.White,
        shadowElevation =
            14.dp,
        shape =
            RoundedCornerShape(
                topStart =
                    25.dp,
                topEnd =
                    25.dp
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        start =
                            10.dp,
                        top =
                            8.dp,
                        end =
                            10.dp,
                        bottom =
                            8.dp
                    ),
            horizontalArrangement =
                Arrangement.SpaceAround,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            GoalFooterItem(
                icon =
                    "⌂",
                label =
                    "Home",
                selected =
                    false,
                onClick =
                    onHome
            )

            GoalFooterItem(
                icon =
                    "●",
                label =
                    "Activity",
                selected =
                    false,
                onClick =
                    onActivity
            )

            GoalFooterItem(
                icon =
                    "▥",
                label =
                    "Progress",
                selected =
                    false,
                onClick =
                    onProgress
            )

            GoalFooterItem(
                icon =
                    "◎",
                label =
                    "Goal",
                selected =
                    true,
                onClick =
                    onGoal
            )

            GoalFooterItem(
                icon =
                    "◉",
                label =
                    "Profile",
                selected =
                    false,
                onClick =
                    onProfile
            )
        }
    }
}

@Composable
private fun GoalFooterItem(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource
        .collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (
                pressed
            ) {
                0.91f
            } else {
                1f
            },
        animationSpec =
            spring(
                dampingRatio =
                    0.82f,
                stiffness =
                    520f
            )
    )

    Column(
        modifier =
            Modifier
                .width(
                    67.dp
                )
                .scale(
                    scale
                )
                .clip(
                    RoundedCornerShape(
                        16.dp
                    )
                )
                .background(
                    if (
                        selected
                    ) {
                        GoalPrimary
                            .copy(
                                alpha =
                                    0.09f
                            )
                    } else {
                        Color.Transparent
                    }
                )
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    onClick =
                        onClick
                )
                .padding(
                    vertical =
                        7.dp
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                icon,
            color =
                if (
                    selected
                ) {
                    GoalPrimary
                } else {
                    Color(0xFF94A3B8)
                },
            fontSize =
                18.sp,
            fontWeight =
                FontWeight.ExtraBold
        )

        Text(
            text =
                label,
            color =
                if (
                    selected
                ) {
                    GoalPrimary
                } else {
                    Color(0xFF94A3B8)
                },
            fontSize =
                9.sp,
            fontWeight =
                if (
                    selected
                ) {
                    FontWeight.ExtraBold
                } else {
                    FontWeight.Bold
                },
            modifier =
                Modifier.padding(
                    top =
                        2.dp
                )
        )
    }
}

@Composable
private fun GoalPageDecorations() {
    Box(
        modifier =
            Modifier
                .padding(
                    top =
                        34.dp,
                    end =
                        10.dp
                )
                .size(
                    105.dp
                )
                .clip(
                    CircleShape
                )
                .background(
                    GoalPrimary
                        .copy(
                            alpha =
                                0.035f
                        )
                )
    )
}
