package sw2.io.mediafloat.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test
import sw2.io.mediafloat.model.MediaCommand
import sw2.io.mediafloat.model.MediaSessionLimitReason
import sw2.io.mediafloat.model.MediaSessionState
import sw2.io.mediafloat.model.PlaybackStatus

class AndroidMediaSessionRepositoryTest {

    @Test
    fun resolveMediaTitle_prefersDisplayTitleOverTitle() {
        val title = AndroidMediaSessionRepository.resolveMediaTitle(
            displayTitle = "Display title",
            title = "Fallback title"
        )

        assertEquals("Display title", title)
    }

    @Test
    fun resolveMediaTitle_fallsBackToTitleWhenDisplayTitleIsBlank() {
        val title = AndroidMediaSessionRepository.resolveMediaTitle(
            displayTitle = "   ",
            title = "Fallback title"
        )

        assertEquals("Fallback title", title)
    }

    @Test
    fun resolveMediaTitle_normalizesBlankMetadataToNull() {
        val title = AndroidMediaSessionRepository.resolveMediaTitle(
            displayTitle = "\n\t  ",
            title = "   "
        )

        assertNull(title)
    }

    @Test
    fun resolveMediaTitle_trimsWhitespaceAroundSelectedValue() {
        val title = AndroidMediaSessionRepository.resolveMediaTitle(
            displayTitle = null,
            title = "  Midnight Express  "
        )

        assertEquals("Midnight Express", title)
    }

    @Test
    fun buildMediaSessionState_keepsTitleWhenPlaybackStateIsMissing() {
        val state = AndroidMediaSessionRepository.buildMediaSessionState(
            sessionId = "session-1",
            title = "Late Night Drive",
            playbackStatus = null,
            supportedActions = null
        )

        assertEquals(
            MediaSessionState.Limited(
                reason = MediaSessionLimitReason.PlaybackStateUnknown,
                title = "Late Night Drive",
                supportedActions = emptySet()
            ),
            state
        )
    }

    @Test
    fun buildMediaSessionState_publishesTitleOnActiveState() {
        val state = AndroidMediaSessionRepository.buildMediaSessionState(
            sessionId = "session-2",
            title = "Neon Skyline Avenue",
            playbackStatus = PlaybackStatus.Playing,
            supportedActions = setOf(MediaCommand.Previous, MediaCommand.TogglePlayPause, MediaCommand.Next)
        )

        assertTrue(state is MediaSessionState.Active)
        state as MediaSessionState.Active
        assertEquals("session-2", state.sessionId)
        assertEquals("Neon Skyline Avenue", state.title)
        assertEquals(PlaybackStatus.Playing, state.playbackStatus)
        assertEquals(
            setOf(MediaCommand.Previous, MediaCommand.TogglePlayPause, MediaCommand.Next),
            state.supportedActions
        )
    }
}
