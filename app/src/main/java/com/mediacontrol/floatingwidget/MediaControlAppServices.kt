package com.mediacontrol.floatingwidget

import android.content.Context
import com.mediacontrol.floatingwidget.debug.AppDebugActions
import com.mediacontrol.floatingwidget.debug.PreferencesDebugLogRepository
import com.mediacontrol.floatingwidget.media.AndroidMediaSessionRepository
import com.mediacontrol.floatingwidget.media.MediaControllerCommandDispatcher
import com.mediacontrol.floatingwidget.preferences.AppPreferencesRepository
import com.mediacontrol.floatingwidget.preferences.AppPreferencesStore
import com.mediacontrol.floatingwidget.overlay.OverlayPositionStore
import com.mediacontrol.floatingwidget.runtime.OverlayRuntimeCoordinator
import com.mediacontrol.floatingwidget.state.AppPreferencesStateHolder
import com.mediacontrol.floatingwidget.state.DebugLogStateHolder
import com.mediacontrol.floatingwidget.state.MediaSummaryStateHolder
import com.mediacontrol.floatingwidget.state.RuntimeSummaryStateHolder
import com.mediacontrol.floatingwidget.state.WidgetConfigStateHolder
import com.mediacontrol.floatingwidget.widget.WidgetConfigStore
import com.mediacontrol.floatingwidget.widget.WidgetPreferencesRepository

class MediaControlAppServices private constructor(
    context: Context
) {

    val debugLogRepository = PreferencesDebugLogRepository(context)
    val appPreferencesStore = AppPreferencesStore(context)
    val appPreferencesRepository = AppPreferencesRepository(appPreferencesStore)
    val overlayPositionStore = OverlayPositionStore(context)
    val widgetConfigStore = WidgetConfigStore(context)
    val widgetPreferencesRepository = WidgetPreferencesRepository(
        configStore = widgetConfigStore,
        positionStore = overlayPositionStore,
        debugLogWriter = debugLogRepository
    )
    val mediaRepository = AndroidMediaSessionRepository(
        context = context,
        debugLogWriter = debugLogRepository
    )
    val mediaCommandDispatcher = MediaControllerCommandDispatcher(
        repository = mediaRepository,
        debugLogWriter = debugLogRepository
    )
    val runtimeCoordinator = OverlayRuntimeCoordinator(
        context = context,
        debugLogWriter = debugLogRepository
    )
    val appPreferencesStateHolder = AppPreferencesStateHolder(appPreferencesRepository)
    val widgetConfigStateHolder = WidgetConfigStateHolder(widgetPreferencesRepository)
    val runtimeSummaryStateHolder = RuntimeSummaryStateHolder(runtimeCoordinator)
    val mediaSummaryStateHolder = MediaSummaryStateHolder(mediaRepository)
    val debugLogStateHolder = DebugLogStateHolder(debugLogRepository)
    val debugActions = AppDebugActions(
        runtimeController = runtimeCoordinator,
        mediaSessionRepository = mediaRepository,
        mediaCommandDispatcher = mediaCommandDispatcher,
        debugLogRepository = debugLogRepository,
        debugLogWriter = debugLogRepository
    )

    companion object {
        @Volatile
        private var instance: MediaControlAppServices? = null

        fun from(context: Context): MediaControlAppServices {
            return instance ?: synchronized(this) {
                instance ?: MediaControlAppServices(context.applicationContext).also { instance = it }
            }
        }
    }
}
