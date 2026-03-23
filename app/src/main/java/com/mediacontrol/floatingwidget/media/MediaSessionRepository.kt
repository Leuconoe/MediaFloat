package com.mediacontrol.floatingwidget.media

import com.mediacontrol.floatingwidget.model.MediaSessionState

fun interface MediaSessionStateListener {
    fun onMediaSessionStateChanged(state: MediaSessionState)
}

interface MediaSessionRepository {
    fun connect()

    fun disconnect()

    fun currentState(): MediaSessionState

    fun addListener(listener: MediaSessionStateListener)

    fun removeListener(listener: MediaSessionStateListener)
}
