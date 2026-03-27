package sw2.io.mediafloat.state

import sw2.io.mediafloat.model.WidgetButton
import sw2.io.mediafloat.model.WidgetConfig
import sw2.io.mediafloat.model.WidgetLayout
import sw2.io.mediafloat.model.WidgetPosition
import sw2.io.mediafloat.model.WidgetSizePreset
import sw2.io.mediafloat.model.WidgetThemePreset
import sw2.io.mediafloat.model.WidgetWidthStyle
import sw2.io.mediafloat.model.DragHandlePlacement
import sw2.io.mediafloat.widget.WidgetPreferencesListener
import sw2.io.mediafloat.widget.WidgetPreferencesRepository
import sw2.io.mediafloat.widget.WidgetPreferencesState

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

    fun setOpacity(opacity: Float) {
        repository.saveConfig(currentState().config.copy(opacity = opacity.coerceIn(0.35f, 1f)))
    }

    fun setDragHandlePlacement(placement: DragHandlePlacement) {
        repository.saveConfig(
            currentState().config.copy(
                layout = currentState().config.layout.copy(dragHandlePlacement = placement)
            )
        )
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
