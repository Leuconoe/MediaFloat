package sw2.io.mediafloat.model

import android.graphics.Bitmap
import kotlin.math.min

enum class MediaCommand {
    Previous,
    TogglePlayPause,
    Next
}

enum class PlaybackStatus {
    Playing,
    Paused,
    Buffering,
    Stopped,
    Unknown
}

enum class MediaSessionLimitReason {
    MissingTransportControls,
    PlaybackStateUnknown,
    SessionChanging
}

enum class MediaSessionErrorReason {
    PermissionRevoked,
    PlatformFailure,
    Unknown
}

enum class MediaArtworkSource {
    MetadataDisplayIconUri,
    MetadataArtUri,
    MetadataAlbumArtUri,
    MetadataDisplayIconBitmap,
    MetadataArtBitmap,
    MetadataAlbumArtBitmap,
    NotificationLargeIcon
}

sealed interface MediaArtwork {
    val source: MediaArtworkSource
    val widthPx: Int
    val heightPx: Int

    data class UriSource(
        override val source: MediaArtworkSource,
        val uri: String,
        override val widthPx: Int,
        override val heightPx: Int
    ) : MediaArtwork

    data class BitmapSource(
        override val source: MediaArtworkSource,
        val bitmap: Bitmap?,
        override val widthPx: Int,
        override val heightPx: Int
    ) : MediaArtwork
}

const val DEFAULT_MIN_MEDIA_ARTWORK_EDGE_PX = 320

val MediaArtwork.minDimensionPx: Int
    get() = min(widthPx, heightPx)

fun MediaArtwork.meetsDefaultQualityGate(): Boolean {
    return minDimensionPx >= DEFAULT_MIN_MEDIA_ARTWORK_EDGE_PX
}

sealed interface MediaSessionState {
    data object Unavailable : MediaSessionState

    data object Discovering : MediaSessionState

    data class Active(
        val sessionId: String,
        val title: String?,
        val artworkCandidates: List<MediaArtwork> = emptyList(),
        val supportedActions: Set<MediaCommand>,
        val playbackStatus: PlaybackStatus
    ) : MediaSessionState

    data class Limited(
        val reason: MediaSessionLimitReason,
        val title: String?,
        val artworkCandidates: List<MediaArtwork> = emptyList(),
        val supportedActions: Set<MediaCommand>
    ) : MediaSessionState

    data class Error(
        val reason: MediaSessionErrorReason
    ) : MediaSessionState
}

fun MediaSessionState.supports(command: MediaCommand): Boolean {
    return when (this) {
        is MediaSessionState.Active -> command in supportedActions
        is MediaSessionState.Limited -> command in supportedActions
        MediaSessionState.Discovering,
        MediaSessionState.Unavailable,
        is MediaSessionState.Error -> false
    }
}

fun MediaSessionState.currentTitle(): String? {
    return when (this) {
        is MediaSessionState.Active -> title.orEmpty()
        is MediaSessionState.Limited -> title.orEmpty()
        MediaSessionState.Discovering,
        MediaSessionState.Unavailable,
        is MediaSessionState.Error -> null
    }
}

fun MediaSessionState.currentArtworkCandidates(): List<MediaArtwork> {
    return when (this) {
        is MediaSessionState.Active -> artworkCandidates
        is MediaSessionState.Limited -> artworkCandidates
        MediaSessionState.Discovering,
        MediaSessionState.Unavailable,
        is MediaSessionState.Error -> emptyList()
    }
}
