package com.mediacontrol.floatingwidget.media

import android.media.session.PlaybackState
import android.util.Log
import com.mediacontrol.floatingwidget.debug.DebugLogWriter
import com.mediacontrol.floatingwidget.debug.NoOpDebugLogWriter
import com.mediacontrol.floatingwidget.model.MediaCommand

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

    private fun com.mediacontrol.floatingwidget.model.MediaSessionState.supportedActions(): Set<MediaCommand> {
        return when (this) {
            is com.mediacontrol.floatingwidget.model.MediaSessionState.Active -> supportedActions
            is com.mediacontrol.floatingwidget.model.MediaSessionState.Limited -> supportedActions
            com.mediacontrol.floatingwidget.model.MediaSessionState.Discovering,
            com.mediacontrol.floatingwidget.model.MediaSessionState.Unavailable,
            is com.mediacontrol.floatingwidget.model.MediaSessionState.Error -> emptySet()
        }
    }

    private companion object {
        const val TAG = "MediaDispatcher"
    }
}
