package com.mediacontrol.floatingwidget.widget

import com.mediacontrol.floatingwidget.debug.DebugLogWriter
import com.mediacontrol.floatingwidget.debug.NoOpDebugLogWriter
import com.mediacontrol.floatingwidget.model.WidgetConfig
import com.mediacontrol.floatingwidget.model.WidgetPosition
import com.mediacontrol.floatingwidget.overlay.OverlayPositionStore

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
            "buttons=${config.layout.orderedButtons.joinToString()} size=${config.sizePreset.name} width=${config.widthStyle.name} theme=${config.themePreset.name} persistent=${config.persistentOverlayEnabled}"
        )
        notifyListeners()
    }

    @Synchronized
    fun savePosition(position: WidgetPosition) {
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
