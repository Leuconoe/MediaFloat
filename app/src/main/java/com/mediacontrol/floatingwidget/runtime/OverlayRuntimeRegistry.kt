package sw2.io.mediafloat.runtime

import sw2.io.mediafloat.model.OverlayRuntimeState
import sw2.io.mediafloat.model.OverlayUnavailableReason

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
