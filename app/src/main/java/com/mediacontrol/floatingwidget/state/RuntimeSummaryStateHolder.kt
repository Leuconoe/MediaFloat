package com.mediacontrol.floatingwidget.state

import com.mediacontrol.floatingwidget.model.CapabilityState
import com.mediacontrol.floatingwidget.model.OverlayRuntimeState
import com.mediacontrol.floatingwidget.runtime.OverlayRuntimeController
import com.mediacontrol.floatingwidget.runtime.OverlayRuntimeListener
import com.mediacontrol.floatingwidget.runtime.OverlayRuntimeRegistry

data class RuntimeSummaryState(
    val capabilityState: CapabilityState,
    val runtimeState: OverlayRuntimeState,
    val canStartOverlay: Boolean,
    val canStopOverlay: Boolean
)

class RuntimeSummaryStateHolder(
    private val runtimeController: OverlayRuntimeController
) : ObservableStateHolder<RuntimeSummaryState>(runtimeController.toRuntimeSummaryState()) {

    private val listener = OverlayRuntimeListener {
        refresh()
    }

    init {
        OverlayRuntimeRegistry.addListener(listener)
        refresh()
    }

    fun refresh() {
        updateState(runtimeController.toRuntimeSummaryState())
    }

    fun close() {
        OverlayRuntimeRegistry.removeListener(listener)
    }
}

private fun OverlayRuntimeController.toRuntimeSummaryState(): RuntimeSummaryState {
    val capabilityState = capabilityState()
    val runtimeState = runtimeState()
    return RuntimeSummaryState(
        capabilityState = capabilityState,
        runtimeState = runtimeState,
        canStartOverlay = capabilityState.isReadyForPersistentOverlay(),
        canStopOverlay = runtimeState is OverlayRuntimeState.Showing
    )
}
