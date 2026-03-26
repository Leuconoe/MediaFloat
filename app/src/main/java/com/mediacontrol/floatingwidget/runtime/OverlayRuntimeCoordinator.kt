package sw2.io.mediafloat.runtime

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import sw2.io.mediafloat.debug.DebugLogWriter
import sw2.io.mediafloat.debug.NoOpDebugLogWriter
import sw2.io.mediafloat.model.CapabilityState
import sw2.io.mediafloat.model.OverlayRuntimeState
import sw2.io.mediafloat.overlay.OverlayService
import sw2.io.mediafloat.permissions.NotificationListenerAccessChecker
import sw2.io.mediafloat.permissions.NotificationPermissionChecker
import sw2.io.mediafloat.permissions.OverlayPermissionChecker
import sw2.io.mediafloat.permissions.OverlayReadinessResolver
import sw2.io.mediafloat.permissions.PermissionReadinessSnapshotProvider

class OverlayRuntimeCoordinator(
    context: Context,
    private val debugLogWriter: DebugLogWriter = NoOpDebugLogWriter
) : OverlayRuntimeController {

    private val appContext = context.applicationContext
    private val overlayPermissionChecker = OverlayPermissionChecker(appContext)
    private val notificationListenerAccessChecker = NotificationListenerAccessChecker(appContext)
    private val notificationPermissionChecker = NotificationPermissionChecker(appContext)
    private val snapshotProvider = PermissionReadinessSnapshotProvider(
        overlayPermissionChecker = overlayPermissionChecker,
        notificationListenerAccessChecker = notificationListenerAccessChecker,
        notificationPermissionChecker = notificationPermissionChecker
    )
    private val readinessResolver = OverlayReadinessResolver()

    override fun capabilityState(): CapabilityState {
        return readinessResolver.resolveCapabilities(snapshotProvider.createSnapshot())
    }

    override fun readinessRuntimeState(): OverlayRuntimeState {
        return readinessResolver.resolveRuntimeState(snapshotProvider.createSnapshot())
    }

    override fun runtimeState(): OverlayRuntimeState {
        val inMemoryState = OverlayRuntimeRegistry.currentState()
        return if (inMemoryState is OverlayRuntimeState.Showing) {
            inMemoryState
        } else {
            readinessRuntimeState()
        }
    }

    override fun startOverlay(): Boolean {
        if (!capabilityState().isReadyForPersistentOverlay()) {
            OverlayRuntimeRegistry.update(readinessRuntimeState())
            debugLogWriter.warn(TAG, "Blocked overlay start because readiness failed", "runtimeState=${readinessRuntimeState()}")
            return false
        }

        return try {
            ContextCompat.startForegroundService(appContext, OverlayService.startIntent(appContext))
            debugLogWriter.info(TAG, "Requested overlay foreground service start")
            true
        } catch (error: Exception) {
            OverlayRuntimeRegistry.update(readinessRuntimeState())
            debugLogWriter.error(TAG, "Overlay foreground service start failed", error.stackTraceToString())
            Log.e(TAG, "Failed to start overlay foreground service", error)
            false
        }
    }

    override fun stopOverlay() {
        appContext.startService(OverlayService.stopIntent(appContext))
        debugLogWriter.info(TAG, "Requested overlay foreground service stop")
    }

    override fun overlaySettingsIntent(): Intent = overlayPermissionChecker.createSettingsIntent()

    override fun notificationListenerSettingsIntent(): Intent = notificationListenerAccessChecker.createSettingsIntent()

    override fun notificationSettingsIntent(): Intent = notificationPermissionChecker.createSettingsIntent()

    private companion object {
        const val TAG = "OverlayRuntime"
    }
}
