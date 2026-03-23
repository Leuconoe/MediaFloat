package com.mediacontrol.floatingwidget.state

import com.mediacontrol.floatingwidget.model.WidgetButton
import com.mediacontrol.floatingwidget.model.WidgetConfig
import com.mediacontrol.floatingwidget.model.WidgetLayout
import com.mediacontrol.floatingwidget.model.WidgetPosition
import com.mediacontrol.floatingwidget.model.WidgetSizePreset
import com.mediacontrol.floatingwidget.model.WidgetThemePreset
import com.mediacontrol.floatingwidget.model.WidgetWidthStyle
import com.mediacontrol.floatingwidget.widget.WidgetPreferencesListener
import com.mediacontrol.floatingwidget.widget.WidgetPreferencesRepository
import com.mediacontrol.floatingwidget.widget.WidgetPreferencesState

data class WidgetConfigScreenState(
    val config: WidgetConfig,
    val position: WidgetPosition
)

class WidgetConfigStateHolder(
    private val repository: WidgetPreferencesRepository
) : ObservableStateHolder<WidgetConfigScreenState>(repository.currentState().toWidgetConfigScreenState()) {

    private val listener = WidgetPreferencesListener { preferencesState ->
        updateState(preferencesState.toWidgetConfigScreenState())
    }

    init {
        repository.addListener(listener)
    }

    fun setVisibleButtons(buttons: Set<WidgetButton>) {
        repository.saveConfig(
            currentState().config.copy(
                layout = WidgetLayout(visibleButtons = buttons)
            )
        )
    }

    fun setSizePreset(sizePreset: WidgetSizePreset) {
        repository.saveConfig(currentState().config.copy(sizePreset = sizePreset))
    }

    fun setWidthStyle(widthStyle: WidgetWidthStyle) {
        repository.saveConfig(currentState().config.copy(widthStyle = widthStyle))
    }

    fun setThemePreset(themePreset: WidgetThemePreset) {
        repository.saveConfig(currentState().config.copy(themePreset = themePreset))
    }

    fun setPersistentOverlayEnabled(enabled: Boolean) {
        repository.saveConfig(currentState().config.copy(persistentOverlayEnabled = enabled))
    }

    fun savePosition(position: WidgetPosition) {
        repository.savePosition(position)
    }

    fun close() {
        repository.removeListener(listener)
    }
}

private fun WidgetPreferencesState.toWidgetConfigScreenState(): WidgetConfigScreenState {
    return WidgetConfigScreenState(
        config = config,
        position = position
    )
}
