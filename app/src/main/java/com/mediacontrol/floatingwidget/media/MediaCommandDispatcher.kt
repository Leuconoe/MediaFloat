package com.mediacontrol.floatingwidget.media

import com.mediacontrol.floatingwidget.model.MediaCommand

interface MediaCommandDispatcher {
    fun dispatch(command: MediaCommand): Boolean
}
