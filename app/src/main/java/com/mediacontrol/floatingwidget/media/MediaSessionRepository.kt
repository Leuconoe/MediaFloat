package com.mediacontrol.floatingwidget.media

import com.mediacontrol.floatingwidget.model.MediaSessionState

interface MediaSessionRepository {
    fun currentState(): MediaSessionState
}
