package com.mediacontrol.floatingwidget.overlay

import com.mediacontrol.floatingwidget.model.MediaSessionState
import com.mediacontrol.floatingwidget.model.WidgetConfig
import com.mediacontrol.floatingwidget.model.WidgetLayout
import com.mediacontrol.floatingwidget.model.WidgetPosition

data class OverlayViewState(
    val config: WidgetConfig,
    val position: WidgetPosition,
    val mediaState: MediaSessionState
) {
    val layout: WidgetLayout
        get() = config.layout
}

interface OverlayHost {
    fun attach(viewState: OverlayViewState)

    fun update(viewState: OverlayViewState)

    fun detach()
}
