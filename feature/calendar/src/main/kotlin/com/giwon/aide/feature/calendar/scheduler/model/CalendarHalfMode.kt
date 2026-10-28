package com.giwon.aide.feature.calendar.scheduler.model

object CalendarHalfMode {
    const val HALF_MODE = 2
    const val FULL_MODE = 1

    const val TRANSITION_STATE_END = 0
    const val TRANSITION_STATE_START = 1

    private var transitionState: Int = 0
    private var mode = FULL_MODE

    fun isHalfMode(): Boolean {
        return mode == HALF_MODE
    }

    fun updateMode(mode: Int) {
        this.mode = mode
    }

    fun updateTransitionState(state: Int) {
        transitionState = state
    }
}
