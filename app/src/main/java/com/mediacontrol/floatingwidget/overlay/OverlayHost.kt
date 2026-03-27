package sw2.io.mediafloat.overlay

import sw2.io.mediafloat.model.MediaSessionState
import sw2.io.mediafloat.model.WidgetConfig
import sw2.io.mediafloat.model.WidgetLayout
import sw2.io.mediafloat.model.WidgetPosition

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
