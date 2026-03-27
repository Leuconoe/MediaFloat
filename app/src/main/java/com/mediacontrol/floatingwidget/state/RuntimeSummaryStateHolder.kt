package sw2.io.mediafloat.state

import sw2.io.mediafloat.model.CapabilityState
import sw2.io.mediafloat.model.OverlayRuntimeState
import sw2.io.mediafloat.runtime.OverlayRuntimeController
import sw2.io.mediafloat.runtime.OverlayRuntimeListener
import sw2.io.mediafloat.runtime.OverlayRuntimeRegistry

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
