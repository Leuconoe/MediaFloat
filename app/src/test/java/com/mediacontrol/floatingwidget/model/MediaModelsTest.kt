package sw2.io.mediafloat.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaModelsTest {

    @Test
    fun currentTitle_returnsBlankTextForActiveSessionWithoutMetadata() {
        val state = MediaSessionState.Active(
            sessionId = "session-1",
            title = null,
            artist = null,
            supportedActions = setOf(MediaCommand.TogglePlayPause),
            playbackStatus = PlaybackStatus.Playing
        )

        assertEquals("", state.currentTitle())
    }

    @Test
    fun currentTitle_keepsNullForUnavailableState() {
        assertNull(MediaSessionState.Unavailable.currentTitle())
    }

    @Test
    fun meetsDefaultQualityGate_requiresMinimumArtworkEdge() {
        val tooSmall = MediaArtwork.UriSource(
            source = MediaArtworkSource.MetadataArtUri,
            uri = "content://artwork/small",
            widthPx = 319,
            heightPx = 500
        )
        val accepted = MediaArtwork.UriSource(
            source = MediaArtworkSource.MetadataArtUri,
            uri = "content://artwork/accepted",
            widthPx = 320,
            heightPx = 320
        )

        assertFalse(tooSmall.meetsDefaultQualityGate())
        assertTrue(accepted.meetsDefaultQualityGate())
    }

    @Test
    fun currentArtworkCandidates_returnsCandidatesForActiveAndLimitedSessions() {
        val artworkCandidates = listOf(
            MediaArtwork.BitmapSource(
                source = MediaArtworkSource.MetadataArtBitmap,
                bitmap = null,
                widthPx = 640,
                heightPx = 640
            )
        )

        assertEquals(
            artworkCandidates,
            MediaSessionState.Active(
                sessionId = "session-art",
                title = "Aurora",
                artist = null,
                artworkCandidates = artworkCandidates,
                supportedActions = setOf(MediaCommand.TogglePlayPause),
                playbackStatus = PlaybackStatus.Playing
            ).currentArtworkCandidates()
        )
        assertEquals(
            artworkCandidates,
            MediaSessionState.Limited(
                reason = MediaSessionLimitReason.MissingTransportControls,
                title = "Aurora",
                artist = null,
                artworkCandidates = artworkCandidates,
                supportedActions = emptySet()
            ).currentArtworkCandidates()
        )
        assertEquals(
            artworkCandidates,
            MediaSessionState.Limited(
                reason = MediaSessionLimitReason.MissingTransportControls,
                title = "Aurora",
                artist = null,
                artworkCandidates = artworkCandidates,
                supportedActions = emptySet()
            ).currentArtworkCandidates()
        )
        assertTrue(MediaSessionState.Unavailable.currentArtworkCandidates().isEmpty())
    }
}
