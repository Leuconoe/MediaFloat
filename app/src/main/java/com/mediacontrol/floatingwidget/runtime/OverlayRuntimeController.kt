package sw2.io.mediafloat.runtime

import android.content.Intent
import sw2.io.mediafloat.model.CapabilityState
import sw2.io.mediafloat.model.OverlayRuntimeState

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
