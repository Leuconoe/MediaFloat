package sw2.io.mediafloat.overlay

import sw2.io.mediafloat.model.MediaArtwork
import sw2.io.mediafloat.model.MediaSessionState
import sw2.io.mediafloat.model.WidgetOverlaySizing
import sw2.io.mediafloat.model.currentArtworkCandidates
import sw2.io.mediafloat.model.meetsDefaultQualityGate

data class OverlayThumbnailPresentation(
    val artwork: MediaArtwork,
    val sizeDp: Int,
    val cornerRadiusDp: Int
)

fun resolveOverlayThumbnailPresentation(
    mediaState: MediaSessionState,
    sizing: WidgetOverlaySizing,
    allowLowQualityFallback: Boolean
): OverlayThumbnailPresentation? {
    val artwork = selectOverlayThumbnailArtwork(
        candidates = mediaState.currentArtworkCandidates(),
        allowLowQualityFallback = allowLowQualityFallback
    ) ?: return null

    return OverlayThumbnailPresentation(
        artwork = artwork,
        sizeDp = sizing.thumbnailSizeDp,
        cornerRadiusDp = sizing.thumbnailCornerRadiusDp
    )
}

fun selectOverlayThumbnailArtwork(
    candidates: List<MediaArtwork>,
    allowLowQualityFallback: Boolean
): MediaArtwork? {
    return candidates.firstOrNull(MediaArtwork::meetsDefaultQualityGate)
        ?: candidates.firstOrNull().takeIf { allowLowQualityFallback }
}

fun WidgetOverlaySizing.bodyWidthDp(hasThumbnail: Boolean): Int {
    return containerWidthDp + if (hasThumbnail) thumbnailSizeDp + itemSpacingDp else 0
}
