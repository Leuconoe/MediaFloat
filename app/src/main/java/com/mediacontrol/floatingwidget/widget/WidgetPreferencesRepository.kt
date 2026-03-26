package sw2.io.mediafloat.widget

import sw2.io.mediafloat.debug.DebugLogWriter
import sw2.io.mediafloat.debug.NoOpDebugLogWriter
import sw2.io.mediafloat.model.WidgetConfig
import sw2.io.mediafloat.model.WidgetPosition
import sw2.io.mediafloat.overlay.OverlayPositionStore

data class WidgetPreferencesState(
    val config: WidgetConfig,
    val position: WidgetPosition
)

fun interface WidgetPreferencesListener {
    fun onWidgetPreferencesChanged(state: WidgetPreferencesState)
}

class WidgetPreferencesRepository(
    private val configStore: WidgetConfigStore,
    private val positionStore: OverlayPositionStore,
    private val debugLogWriter: DebugLogWriter = NoOpDebugLogWriter
) {

    private val listeners = linkedSetOf<WidgetPreferencesListener>()

    @Volatile
    private var state: WidgetPreferencesState = loadState()

    fun currentState(): WidgetPreferencesState = state

    fun addListener(listener: WidgetPreferencesListener) {
        listeners += listener
        listener.onWidgetPreferencesChanged(state)
    }

    fun removeListener(listener: WidgetPreferencesListener) {
        listeners -= listener
    }

    @Synchronized
    fun saveConfig(config: WidgetConfig) {
        configStore.save(config)
        state = state.copy(config = config)
        debugLogWriter.info(
            TAG,
            "Saved widget config",
            "buttons=${config.layout.orderedButtons.joinToString()} side=${config.layout.dragHandlePlacement.name} size=${config.sizePreset.name} width=${config.widthStyle.name} theme=${config.themePreset.name} opacity=${config.opacity} persistent=${config.persistentOverlayEnabled}"
        )
        notifyListeners()
    }

    @Synchronized
    fun savePosition(position: WidgetPosition) {
        if (state.position == position) {
            debugLogWriter.debug(TAG, "Skipped widget position save because nothing changed")
            return
        }
        positionStore.save(position)
        state = state.copy(position = position)
        debugLogWriter.debug(
            TAG,
            "Saved widget position",
            "anchor=${position.anchor.name} xOffsetDp=${position.xOffsetDp} yOffsetDp=${position.yOffsetDp}"
        )
        notifyListeners()
    }

    @Synchronized
    fun reload() {
        state = loadState()
        notifyListeners()
    }

    private fun loadState(): WidgetPreferencesState {
        return WidgetPreferencesState(
            config = configStore.load(),
            position = positionStore.load()
        )
    }

    private fun notifyListeners() {
        val snapshot = state
        listeners.forEach { it.onWidgetPreferencesChanged(snapshot) }
    }

    private companion object {
        const val TAG = "WidgetPrefsRepo"
    }
}
