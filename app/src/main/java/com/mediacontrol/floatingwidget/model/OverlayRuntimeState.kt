package com.mediacontrol.floatingwidget.model

sealed interface OverlayRuntimeState {
    data class Unavailable(
        val reason: OverlayUnavailableReason
    ) : OverlayRuntimeState

    data object Ready : OverlayRuntimeState

    data class Showing(
        val position: WidgetPosition,
        val layout: WidgetLayout,
        val mediaState: MediaSessionState
    ) : OverlayRuntimeState

    data class Suspended(
        val reason: OverlayUnavailableReason
    ) : OverlayRuntimeState
}
