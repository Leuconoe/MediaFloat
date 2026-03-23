package com.mediacontrol.floatingwidget.state

import com.mediacontrol.floatingwidget.media.MediaSessionRepository
import com.mediacontrol.floatingwidget.media.MediaSessionStateListener
import com.mediacontrol.floatingwidget.model.MediaCommand
import com.mediacontrol.floatingwidget.model.MediaSessionState
import com.mediacontrol.floatingwidget.model.supports

data class MediaSummaryState(
    val mediaState: MediaSessionState,
    val previousEnabled: Boolean,
    val playPauseEnabled: Boolean,
    val nextEnabled: Boolean
)

class MediaSummaryStateHolder(
    private val mediaSessionRepository: MediaSessionRepository
) : ObservableStateHolder<MediaSummaryState>(mediaSessionRepository.currentState().toMediaSummaryState()) {

    private val listener = MediaSessionStateListener { mediaState ->
        updateState(mediaState.toMediaSummaryState())
    }

    private var started = false

    @Synchronized
    fun start() {
        if (started) {
            return
        }

        started = true
        mediaSessionRepository.addListener(listener)
        mediaSessionRepository.connect()
        updateState(mediaSessionRepository.currentState().toMediaSummaryState())
    }

    @Synchronized
    fun stop() {
        if (!started) {
            return
        }

        mediaSessionRepository.removeListener(listener)
        mediaSessionRepository.disconnect()
        started = false
    }
}

private fun MediaSessionState.toMediaSummaryState(): MediaSummaryState {
    return MediaSummaryState(
        mediaState = this,
        previousEnabled = supports(MediaCommand.Previous),
        playPauseEnabled = supports(MediaCommand.TogglePlayPause),
        nextEnabled = supports(MediaCommand.Next)
    )
}
