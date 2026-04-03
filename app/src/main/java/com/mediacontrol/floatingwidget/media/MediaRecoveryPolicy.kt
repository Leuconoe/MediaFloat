package sw2.io.mediafloat.media

import android.media.session.PlaybackState
import sw2.io.mediafloat.model.MediaCommand
import sw2.io.mediafloat.model.MediaSessionLimitReason
import sw2.io.mediafloat.model.MediaSessionState

internal object MediaRecoveryPolicy {
    fun shouldRecoverAfterPlaybackStateChange(state: PlaybackState?): Boolean {
        if (state == null) return true
        return when (state.state) {
            PlaybackState.STATE_NONE,
            PlaybackState.STATE_STOPPED,
            PlaybackState.STATE_ERROR -> true
            else -> state.actions.toSupportedCommands().isEmpty()
        }
    }

    fun shouldContinueRecovery(state: MediaSessionState): Boolean {
        return when (state) {
            is MediaSessionState.Active -> false
            is MediaSessionState.Limited -> state.reason == MediaSessionLimitReason.PlaybackStateUnknown ||
                state.reason == MediaSessionLimitReason.MissingTransportControls
            MediaSessionState.Discovering,
            MediaSessionState.Unavailable -> true
            is MediaSessionState.Error -> false
        }
    }

    private fun Long.toSupportedCommands(): Set<MediaCommand> {
        val commands = linkedSetOf<MediaCommand>()
        if (this and PlaybackState.ACTION_SKIP_TO_PREVIOUS != 0L) commands += MediaCommand.Previous
        if (this and PlaybackState.ACTION_PLAY != 0L || this and PlaybackState.ACTION_PAUSE != 0L || this and PlaybackState.ACTION_PLAY_PAUSE != 0L) {
            commands += MediaCommand.TogglePlayPause
        }
        if (this and PlaybackState.ACTION_SKIP_TO_NEXT != 0L) commands += MediaCommand.Next
        return commands
    }
}
