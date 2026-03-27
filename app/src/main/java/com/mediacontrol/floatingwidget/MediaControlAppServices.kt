package sw2.io.mediafloat

import android.content.Context
import sw2.io.mediafloat.debug.AppDebugActions
import sw2.io.mediafloat.debug.PreferencesDebugLogRepository
import sw2.io.mediafloat.media.AndroidMediaSessionRepository
import sw2.io.mediafloat.media.MediaControllerCommandDispatcher
import sw2.io.mediafloat.preferences.AppPreferencesRepository
import sw2.io.mediafloat.preferences.AppPreferencesStore
import sw2.io.mediafloat.overlay.OverlayPositionStore
import sw2.io.mediafloat.runtime.OverlayRuntimeCoordinator
import sw2.io.mediafloat.state.AppPreferencesStateHolder
import sw2.io.mediafloat.state.DebugLogStateHolder
import sw2.io.mediafloat.state.MediaSummaryStateHolder
import sw2.io.mediafloat.state.RuntimeSummaryStateHolder
import sw2.io.mediafloat.state.WidgetConfigStateHolder
import sw2.io.mediafloat.widget.WidgetConfigStore
import sw2.io.mediafloat.widget.WidgetPreferencesRepository

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
