package com.example.movemate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

object GoalRefreshBus {

    var version by
    mutableIntStateOf(
        0
    )
        private set

    fun notifyGoalChanged() {
        version +=
            1
    }
}
