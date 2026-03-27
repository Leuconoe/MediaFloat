package sw2.io.mediafloat.media

import sw2.io.mediafloat.model.MediaSessionState

fun interface MediaSessionStateListener {
    fun onMediaSessionStateChanged(state: MediaSessionState)
}

interface MediaSessionRepository {
    fun connect()

    fun disconnect()

    fun prepareForOverlayActivation() = Unit

    fun currentState(): MediaSessionState

    fun addListener(listener: MediaSessionStateListener)

    fun removeListener(listener: MediaSessionStateListener)
}
