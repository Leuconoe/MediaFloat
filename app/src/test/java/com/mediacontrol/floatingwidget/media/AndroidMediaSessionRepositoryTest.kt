package sw2.io.mediafloat.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test
import sw2.io.mediafloat.model.MediaArtwork
import sw2.io.mediafloat.model.MediaArtworkSource
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
        val artworkCandidates = listOf(
            MediaArtwork.UriSource(
                source = MediaArtworkSource.MetadataDisplayIconUri,
                uri = "content://artwork/display",
                widthPx = 320,
                heightPx = 320
            )
        )
        val state = AndroidMediaSessionRepository.buildMediaSessionState(
            sessionId = "session-1",
            title = "Late Night Drive",
            artist = null,
            artworkCandidates = artworkCandidates,
            playbackStatus = null,
            supportedActions = null
        )

        assertEquals(
            MediaSessionState.Limited(
                reason = MediaSessionLimitReason.PlaybackStateUnknown,
                title = "Late Night Drive",
                artist = null,
                artworkCandidates = artworkCandidates,
                supportedActions = emptySet()
            ),
            state
        )
    }

    @Test
    fun buildMediaSessionState_publishesTitleOnActiveState() {
        val artworkCandidates = listOf(
            MediaArtwork.BitmapSource(
                source = MediaArtworkSource.MetadataArtBitmap,
                bitmap = null,
                widthPx = 640,
                heightPx = 640
            )
        )
        val state = AndroidMediaSessionRepository.buildMediaSessionState(
            sessionId = "session-2",
            title = "Neon Skyline Avenue",
            artist = "Synthwave Collective",
            artworkCandidates = artworkCandidates,
            playbackStatus = PlaybackStatus.Playing,
            supportedActions = setOf(MediaCommand.Previous, MediaCommand.TogglePlayPause, MediaCommand.Next)
        )

        assertTrue(state is MediaSessionState.Active)
        state as MediaSessionState.Active
        assertEquals("session-2", state.sessionId)
        assertEquals("Neon Skyline Avenue", state.title)
        assertEquals("Synthwave Collective", state.artist)
        assertEquals(artworkCandidates, state.artworkCandidates)
        assertEquals(PlaybackStatus.Playing, state.playbackStatus)
        assertEquals(
            setOf(MediaCommand.Previous, MediaCommand.TogglePlayPause, MediaCommand.Next),
            state.supportedActions
        )
    }

    @Test
    fun buildArtworkCandidates_keepsMetadataAndNotificationPriorityOrder() {
        val artworkCandidates = AndroidMediaSessionRepository.buildArtworkCandidates(
            metadataDisplayIconUri = MediaArtwork.UriSource(
                source = MediaArtworkSource.MetadataDisplayIconUri,
                uri = "content://artwork/display",
                widthPx = 319,
                heightPx = 500
            ),
            metadataArtUri = MediaArtwork.UriSource(
                source = MediaArtworkSource.MetadataArtUri,
                uri = "content://artwork/art",
                widthPx = 640,
                heightPx = 640
            ),
            metadataAlbumArtUri = MediaArtwork.UriSource(
                source = MediaArtworkSource.MetadataAlbumArtUri,
                uri = "content://artwork/album",
                widthPx = 320,
                heightPx = 320
            ),
            metadataDisplayIconBitmap = MediaArtwork.BitmapSource(
                source = MediaArtworkSource.MetadataDisplayIconBitmap,
                bitmap = null,
                widthPx = 640,
                heightPx = 640
            ),
            metadataArtBitmap = MediaArtwork.BitmapSource(
                source = MediaArtworkSource.MetadataArtBitmap,
                bitmap = null,
                widthPx = 320,
                heightPx = 320
            ),
            metadataAlbumArtBitmap = MediaArtwork.BitmapSource(
                source = MediaArtworkSource.MetadataAlbumArtBitmap,
                bitmap = null,
                widthPx = 128,
                heightPx = 128
            ),
            notificationLargeIcon = MediaArtwork.BitmapSource(
                source = MediaArtworkSource.NotificationLargeIcon,
                bitmap = null,
                widthPx = 640,
                heightPx = 640
            )
        )

        assertEquals(
            listOf(
                MediaArtworkSource.MetadataDisplayIconUri,
                MediaArtworkSource.MetadataArtUri,
                MediaArtworkSource.MetadataAlbumArtUri,
                MediaArtworkSource.MetadataDisplayIconBitmap,
                MediaArtworkSource.MetadataArtBitmap,
                MediaArtworkSource.MetadataAlbumArtBitmap,
                MediaArtworkSource.NotificationLargeIcon
            ),
            artworkCandidates.map { it.source }
        )
    }
}
