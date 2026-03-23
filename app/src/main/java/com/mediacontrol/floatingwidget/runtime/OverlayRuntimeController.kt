package com.mediacontrol.floatingwidget.runtime

import android.content.Intent
import com.mediacontrol.floatingwidget.model.CapabilityState
import com.mediacontrol.floatingwidget.model.OverlayRuntimeState

interface OverlayRuntimeController {
    fun capabilityState(): CapabilityState

    fun readinessRuntimeState(): OverlayRuntimeState

    fun runtimeState(): OverlayRuntimeState

    fun startOverlay(): Boolean

    fun stopOverlay()

    fun overlaySettingsIntent(): Intent

    fun notificationListenerSettingsIntent(): Intent

    fun notificationSettingsIntent(): Intent
}
