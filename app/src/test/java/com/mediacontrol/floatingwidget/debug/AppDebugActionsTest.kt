package com.mediacontrol.floatingwidget.debug

import android.content.Intent
import com.mediacontrol.floatingwidget.media.MediaCommandDispatcher
import com.mediacontrol.floatingwidget.media.MediaSessionRepository
import com.mediacontrol.floatingwidget.media.MediaSessionStateListener
import com.mediacontrol.floatingwidget.model.CapabilityGrantState
import com.mediacontrol.floatingwidget.model.CapabilityState
import com.mediacontrol.floatingwidget.model.MediaCommand
import com.mediacontrol.floatingwidget.model.MediaSessionState
import com.mediacontrol.floatingwidget.model.NotificationPosture
import com.mediacontrol.floatingwidget.model.OverlayRuntimeState
import com.mediacontrol.floatingwidget.model.OverlayUnavailableReason
import com.mediacontrol.floatingwidget.runtime.OverlayRuntimeController
import com.mediacontrol.floatingwidget.storage.TestPreferencesStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDebugActionsTest {

    @Test
    fun dispatchPlayPause_usesSharedDispatcherPathAndBalancesConnection() {
        val repository = FakeMediaSessionRepository()
        val dispatcher = RecordingMediaCommandDispatcher()
        val actions = AppDebugActions(
            runtimeController = FakeOverlayRuntimeController(),
            mediaSessionRepository = repository,
            mediaCommandDispatcher = dispatcher,
            debugLogRepository = PreferencesDebugLogRepository(storage = TestPreferencesStorage(), retentionLimit = 200, clock = { 1L })
        )

        val dispatched = actions.dispatchPlayPause()

        assertTrue(dispatched)
        assertEquals(listOf(MediaCommand.TogglePlayPause), dispatcher.commands)
        assertEquals(1, repository.connectCalls)
        assertEquals(1, repository.disconnectCalls)
    }

    private class FakeMediaSessionRepository : MediaSessionRepository {
        var connectCalls = 0
        var disconnectCalls = 0

        override fun connect() {
            connectCalls += 1
        }

        override fun disconnect() {
            disconnectCalls += 1
        }

        override fun currentState(): MediaSessionState = MediaSessionState.Unavailable

        override fun addListener(listener: MediaSessionStateListener) = Unit

        override fun removeListener(listener: MediaSessionStateListener) = Unit
    }

    private class RecordingMediaCommandDispatcher : MediaCommandDispatcher {
        val commands = mutableListOf<MediaCommand>()

        override fun dispatch(command: MediaCommand): Boolean {
            commands += command
            return true
        }
    }

    private class FakeOverlayRuntimeController : OverlayRuntimeController {
        override fun capabilityState(): CapabilityState {
            return CapabilityState(
                overlayAccess = CapabilityGrantState.Granted,
                notificationListenerAccess = CapabilityGrantState.Granted,
                notificationPosture = NotificationPosture.Visible,
                serviceStartReadiness = CapabilityGrantState.Granted
            )
        }

        override fun readinessRuntimeState(): OverlayRuntimeState = OverlayRuntimeState.Ready

        override fun runtimeState(): OverlayRuntimeState {
            return OverlayRuntimeState.Unavailable(OverlayUnavailableReason.UnknownRuntimeFailure)
        }

        override fun startOverlay(): Boolean = true

        override fun stopOverlay() = Unit

        override fun overlaySettingsIntent(): Intent = Intent()

        override fun notificationListenerSettingsIntent(): Intent = Intent()

        override fun notificationSettingsIntent(): Intent = Intent()
    }
}
