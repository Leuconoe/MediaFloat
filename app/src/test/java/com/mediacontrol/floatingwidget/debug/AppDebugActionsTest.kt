package sw2.io.mediafloat.debug

import android.content.Intent
import sw2.io.mediafloat.media.MediaCommandDispatcher
import sw2.io.mediafloat.media.MediaSessionRepository
import sw2.io.mediafloat.media.MediaSessionStateListener
import sw2.io.mediafloat.model.CapabilityGrantState
import sw2.io.mediafloat.model.CapabilityState
import sw2.io.mediafloat.model.MediaCommand
import sw2.io.mediafloat.model.MediaSessionState
import sw2.io.mediafloat.model.NotificationPosture
import sw2.io.mediafloat.model.OverlayRuntimeState
import sw2.io.mediafloat.model.OverlayUnavailableReason
import sw2.io.mediafloat.runtime.OverlayRuntimeController
import sw2.io.mediafloat.storage.TestPreferencesStorage
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

    @Test
    fun startOverlay_preparesMediaTrackingBeforeRuntimeStart() {
        val repository = FakeMediaSessionRepository()
        val runtimeController = FakeOverlayRuntimeController()
        val actions = AppDebugActions(
            runtimeController = runtimeController,
            mediaSessionRepository = repository,
            mediaCommandDispatcher = RecordingMediaCommandDispatcher(),
            debugLogRepository = PreferencesDebugLogRepository(storage = TestPreferencesStorage(), retentionLimit = 200, clock = { 1L })
        )

        val started = actions.startOverlay()

        assertTrue(started)
        assertEquals(1, repository.prepareForOverlayActivationCalls)
        assertEquals(1, runtimeController.startOverlayCalls)
    }

    @Test
    fun toggleOverlay_stopsWhenRuntimeIsShowing() {
        val runtimeController = FakeOverlayRuntimeController().apply {
            runtimeState = OverlayRuntimeState.Showing(
                position = sw2.io.mediafloat.model.WidgetPosition(),
                layout = sw2.io.mediafloat.model.WidgetLayout.Default,
                mediaState = MediaSessionState.Unavailable
            )
        }
        val actions = AppDebugActions(
            runtimeController = runtimeController,
            mediaSessionRepository = FakeMediaSessionRepository(),
            mediaCommandDispatcher = RecordingMediaCommandDispatcher(),
            debugLogRepository = PreferencesDebugLogRepository(storage = TestPreferencesStorage(), retentionLimit = 200, clock = { 1L })
        )

        val started = actions.toggleOverlay()

        assertEquals(false, started)
        assertEquals(1, runtimeController.stopOverlayCalls)
        assertEquals(0, runtimeController.startOverlayCalls)
    }

    private class FakeMediaSessionRepository : MediaSessionRepository {
        var connectCalls = 0
        var disconnectCalls = 0
        var prepareForOverlayActivationCalls = 0

        override fun connect() {
            connectCalls += 1
        }

        override fun disconnect() {
            disconnectCalls += 1
        }

        override fun prepareForOverlayActivation() {
            prepareForOverlayActivationCalls += 1
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
        var startOverlayCalls = 0
        var stopOverlayCalls = 0
        var runtimeState: OverlayRuntimeState = OverlayRuntimeState.Unavailable(OverlayUnavailableReason.UnknownRuntimeFailure)

        override fun capabilityState(): CapabilityState {
            return CapabilityState(
                overlayAccess = CapabilityGrantState.Granted,
                notificationListenerAccess = CapabilityGrantState.Granted,
                notificationPosture = NotificationPosture.Visible,
                serviceStartReadiness = CapabilityGrantState.Granted
            )
        }

        override fun readinessRuntimeState(): OverlayRuntimeState = OverlayRuntimeState.Ready

        override fun runtimeState(): OverlayRuntimeState = runtimeState

        override fun startOverlay(): Boolean {
            startOverlayCalls += 1
            return true
        }

        override fun stopOverlay() {
            stopOverlayCalls += 1
            runtimeState = OverlayRuntimeState.Unavailable(OverlayUnavailableReason.UnknownRuntimeFailure)
        }

        override fun overlaySettingsIntent(): Intent = Intent()

        override fun notificationListenerSettingsIntent(): Intent = Intent()

        override fun notificationSettingsIntent(): Intent = Intent()
    }
}
