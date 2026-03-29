package sw2.io.mediafloat.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import sw2.io.mediafloat.model.MediaArtwork
import sw2.io.mediafloat.model.MediaArtworkSource
import sw2.io.mediafloat.model.MediaCommand
import sw2.io.mediafloat.model.MediaSessionState
import sw2.io.mediafloat.model.PlaybackStatus
import sw2.io.mediafloat.model.WidgetConfig
import sw2.io.mediafloat.model.overlayAppearance

class OverlayThumbnailPolicyTest {

    @Test
    fun selectOverlayThumbnailArtwork_usesFirstHighQualityCandidateByDefault() {
        val selected = selectOverlayThumbnailArtwork(
            candidates = listOf(
                MediaArtwork.UriSource(
                    source = MediaArtworkSource.MetadataDisplayIconUri,
                    uri = "content://artwork/319x500",
                    widthPx = 319,
                    heightPx = 500
                ),
                MediaArtwork.BitmapSource(
                    source = MediaArtworkSource.MetadataArtBitmap,
                    bitmap = null,
                    widthPx = 640,
                    heightPx = 640
                )
            ),
            allowLowQualityFallback = false
        )

        assertEquals(MediaArtworkSource.MetadataArtBitmap, selected?.source)
    }

    @Test
    fun selectOverlayThumbnailArtwork_rejectsOnlyLowQualityCandidatesByDefault() {
        val selected = selectOverlayThumbnailArtwork(
            candidates = listOf(
                MediaArtwork.UriSource(
                    source = MediaArtworkSource.MetadataDisplayIconUri,
                    uri = "content://artwork/128x128",
                    widthPx = 128,
                    heightPx = 128
                ),
                MediaArtwork.UriSource(
                    source = MediaArtworkSource.MetadataArtUri,
                    uri = "content://artwork/319x500",
                    widthPx = 319,
                    heightPx = 500
                )
            ),
            allowLowQualityFallback = false
        )

        assertNull(selected)
    }

    @Test
    fun selectOverlayThumbnailArtwork_allowsLowQualityFallbackAsLastResort() {
        val selected = selectOverlayThumbnailArtwork(
            candidates = listOf(
                MediaArtwork.UriSource(
                    source = MediaArtworkSource.MetadataDisplayIconUri,
                    uri = "content://artwork/319x500",
                    widthPx = 319,
                    heightPx = 500
                ),
                MediaArtwork.BitmapSource(
                    source = MediaArtworkSource.MetadataArtBitmap,
                    bitmap = null,
                    widthPx = 128,
                    heightPx = 128
                )
            ),
            allowLowQualityFallback = true
        )

        assertEquals(MediaArtworkSource.MetadataDisplayIconUri, selected?.source)
    }

    @Test
    fun resolveOverlayThumbnailPresentation_linksThumbnailSizeToMediaBarHeight() {
        val config = WidgetConfig()
        val sizing = config.overlayAppearance().sizing
        val presentation = resolveOverlayThumbnailPresentation(
            mediaState = MediaSessionState.Active(
                sessionId = "session-preview",
                title = "Late Bloom",
                artworkCandidates = listOf(
                    MediaArtwork.UriSource(
                        source = MediaArtworkSource.MetadataArtUri,
                        uri = "content://artwork/320x320",
                        widthPx = 320,
                        heightPx = 320
                    )
                ),
                supportedActions = setOf(MediaCommand.TogglePlayPause),
                playbackStatus = PlaybackStatus.Playing
            ),
            sizing = sizing,
            allowLowQualityFallback = false
        )

        assertEquals(320, presentation?.artwork?.widthPx)
        assertEquals(sizing.containerHeightDp, presentation?.sizeDp)
        assertEquals(
            sizing.containerWidthDp + sizing.thumbnailSizeDp + sizing.itemSpacingDp,
            sizing.bodyWidthDp(hasThumbnail = true)
        )
    }
}
