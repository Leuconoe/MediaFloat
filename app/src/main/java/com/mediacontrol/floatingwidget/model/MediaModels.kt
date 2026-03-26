package sw2.io.mediafloat.model

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

sealed interface MediaSessionState {
    data object Unavailable : MediaSessionState

    data object Discovering : MediaSessionState

    data class Active(
        val sessionId: String,
        val supportedActions: Set<MediaCommand>,
        val playbackStatus: PlaybackStatus
    ) : MediaSessionState

    data class Limited(
        val reason: MediaSessionLimitReason,
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
