package sw2.io.mediafloat.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaModelsTest {

    @Test
    fun currentTitle_returnsBlankTextForActiveSessionWithoutMetadata() {
        val state = MediaSessionState.Active(
            sessionId = "session-1",
            title = null,
            supportedActions = setOf(MediaCommand.TogglePlayPause),
            playbackStatus = PlaybackStatus.Playing
        )

        assertEquals("", state.currentTitle())
    }

    @Test
    fun currentTitle_keepsNullForUnavailableState() {
        assertNull(MediaSessionState.Unavailable.currentTitle())
    }
}
