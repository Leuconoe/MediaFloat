package com.mediacontrol.floatingwidget.runtime

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mediacontrol.floatingwidget.debug.DebugLogWriter
import com.mediacontrol.floatingwidget.debug.NoOpDebugLogWriter
import com.mediacontrol.floatingwidget.model.CapabilityState
import com.mediacontrol.floatingwidget.model.OverlayRuntimeState
import com.mediacontrol.floatingwidget.overlay.OverlayService
import com.mediacontrol.floatingwidget.permissions.NotificationListenerAccessChecker
import com.mediacontrol.floatingwidget.permissions.NotificationPermissionChecker
import com.mediacontrol.floatingwidget.permissions.OverlayPermissionChecker
import com.mediacontrol.floatingwidget.permissions.OverlayReadinessResolver
import com.mediacontrol.floatingwidget.permissions.PermissionReadinessSnapshotProvider

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

        ContextCompat.startForegroundService(appContext, OverlayService.startIntent(appContext))
        debugLogWriter.info(TAG, "Requested overlay foreground service start")
        return true
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
