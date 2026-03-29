package sw2.io.mediafloat.debug

import sw2.io.mediafloat.media.MediaCommandDispatcher
import sw2.io.mediafloat.media.MediaSessionRepository
import sw2.io.mediafloat.model.MediaCommand
import sw2.io.mediafloat.runtime.OverlayRuntimeController

interface DebugActions {
    fun startOverlay(): Boolean

    fun stopOverlay()

    fun toggleOverlay(): Boolean

    fun dispatchPrevious(): Boolean

    fun dispatchPlayPause(): Boolean

    fun dispatchNext(): Boolean

    fun clearLogs()

    fun addMarker(message: String, details: String? = null)
}

class AppDebugActions(
    private val runtimeController: OverlayRuntimeController,
    private val mediaSessionRepository: MediaSessionRepository,
    private val mediaCommandDispatcher: MediaCommandDispatcher,
    private val debugLogRepository: DebugLogRepository,
    private val debugLogWriter: DebugLogWriter = debugLogRepository as? DebugLogWriter ?: NoOpDebugLogWriter
) : DebugActions {

    override fun startOverlay(): Boolean {
        mediaSessionRepository.prepareForOverlayActivation()
        val started = runtimeController.startOverlay()
        debugLogWriter.info(TAG, "In-app start overlay action", "started=$started")
        return started
    }

    override fun stopOverlay() {
        runtimeController.stopOverlay()
        debugLogWriter.info(TAG, "In-app stop overlay action")
    }

    override fun toggleOverlay(): Boolean {
        return if (runtimeController.runtimeState() is sw2.io.mediafloat.model.OverlayRuntimeState.Showing) {
            stopOverlay()
            false
        } else {
            startOverlay()
        }
    }

    override fun dispatchPrevious(): Boolean {
        return dispatchMediaCommand(MediaCommand.Previous)
    }

    override fun dispatchPlayPause(): Boolean {
        return dispatchMediaCommand(MediaCommand.TogglePlayPause)
    }

    override fun dispatchNext(): Boolean {
        return dispatchMediaCommand(MediaCommand.Next)
    }

    override fun clearLogs() {
        debugLogRepository.clear()
        debugLogWriter.info(TAG, "Cleared debug logs")
    }

    override fun addMarker(message: String, details: String?) {
        debugLogWriter.info(TAG, message, details)
    }

    private fun dispatchMediaCommand(command: MediaCommand): Boolean {
        mediaSessionRepository.connect()
        return try {
            val dispatched = mediaCommandDispatcher.dispatch(command)
            debugLogWriter.info(TAG, "In-app media debug action", "command=${command.name} dispatched=$dispatched")
            dispatched
        } finally {
            mediaSessionRepository.disconnect()
        }
    }

    private companion object {
        const val TAG = "AppDebugActions"
    }
}
