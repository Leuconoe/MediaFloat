package sw2.io.mediafloat.media

import android.media.session.PlaybackState
import android.util.Log
import sw2.io.mediafloat.debug.DebugLogWriter
import sw2.io.mediafloat.debug.NoOpDebugLogWriter
import sw2.io.mediafloat.model.MediaCommand

interface MediaCommandDispatcher {
    fun dispatch(command: MediaCommand): Boolean
}

class MediaControllerCommandDispatcher(
    private val repository: AndroidMediaSessionRepository,
    private val debugLogWriter: DebugLogWriter = NoOpDebugLogWriter
) : MediaCommandDispatcher {

    override fun dispatch(command: MediaCommand): Boolean {
        var controller = repository.currentController()
        var controls = controller?.transportControls

        if (controller == null || controls == null) {
            repository.refresh(reason = "dispatch_${command.name.lowercase()}")
            controller = repository.currentController()
            controls = controller?.transportControls
        }

        if (controller == null || controls == null) {
            Log.w(TAG, "No active media controller for $command")
            debugLogWriter.warn(TAG, "No active media controller for command", "command=${command.name}")
            return false
        }

        if (command !in repository.currentState().supportedActions()) {
            repository.refresh(reason = "dispatch_support_check_${command.name.lowercase()}")
            if (command !in repository.currentState().supportedActions()) {
                Log.d(TAG, "Ignoring unsupported media command $command")
                debugLogWriter.debug(
                    TAG,
                    "Ignoring unsupported media command",
                    "command=${command.name} state=${repository.currentState()}"
                )
                return false
            }
        }

        when (command) {
            MediaCommand.Previous -> controls.skipToPrevious()
            MediaCommand.Next -> controls.skipToNext()
            MediaCommand.TogglePlayPause -> {
                val playbackState = controller.playbackState?.state ?: PlaybackState.STATE_NONE
                if (playbackState == PlaybackState.STATE_PLAYING || playbackState == PlaybackState.STATE_BUFFERING) {
                    controls.pause()
                } else {
                    controls.play()
                }
            }
        }

        Log.d(TAG, "Dispatched media command $command to ${controller.packageName}")
        debugLogWriter.info(
            TAG,
            "Dispatched media command",
            "command=${command.name} package=${controller.packageName}"
        )
        return true
    }

    private fun sw2.io.mediafloat.model.MediaSessionState.supportedActions(): Set<MediaCommand> {
        return when (this) {
            is sw2.io.mediafloat.model.MediaSessionState.Active -> supportedActions
            is sw2.io.mediafloat.model.MediaSessionState.Limited -> supportedActions
            sw2.io.mediafloat.model.MediaSessionState.Discovering,
            sw2.io.mediafloat.model.MediaSessionState.Unavailable,
            is sw2.io.mediafloat.model.MediaSessionState.Error -> emptySet()
        }
    }

    private companion object {
        const val TAG = "MediaDispatcher"
    }
}
