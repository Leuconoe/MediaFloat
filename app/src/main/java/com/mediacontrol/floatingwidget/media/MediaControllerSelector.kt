package sw2.io.mediafloat.media

import android.media.session.MediaController
import android.media.session.PlaybackState

internal object MediaControllerSelector {
    fun select(controllers: List<MediaController>): MediaController? {
        if (controllers.isEmpty()) return null
        return controllers
            .sortedWith(
                compareByDescending<MediaController> { it.playbackState.isActivelyPlaying() }
                    .thenByDescending { it.playbackState?.lastPositionUpdateTime ?: 0L }
            )
            .firstOrNull()
    }

    private fun PlaybackState?.isActivelyPlaying(): Boolean {
        return this?.state == PlaybackState.STATE_PLAYING || this?.state == PlaybackState.STATE_BUFFERING
    }
}
