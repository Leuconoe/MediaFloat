package com.mediacontrol.floatingwidget.runtime

import com.mediacontrol.floatingwidget.model.OverlayRuntimeState
import com.mediacontrol.floatingwidget.model.OverlayUnavailableReason

fun interface OverlayRuntimeListener {
    fun onRuntimeStateChanged(state: OverlayRuntimeState)
}

object OverlayRuntimeRegistry {
    private val listeners = linkedSetOf<OverlayRuntimeListener>()

    @Volatile
    private var runtimeState: OverlayRuntimeState = OverlayRuntimeState.Unavailable(
        OverlayUnavailableReason.UnknownRuntimeFailure
    )

    fun currentState(): OverlayRuntimeState = runtimeState

    fun update(state: OverlayRuntimeState) {
        runtimeState = state
        listeners.forEach { it.onRuntimeStateChanged(state) }
    }

    fun addListener(listener: OverlayRuntimeListener) {
        listeners += listener
    }

    fun removeListener(listener: OverlayRuntimeListener) {
        listeners -= listener
    }
}
