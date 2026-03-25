package com.mediacontrol.floatingwidget.widget

import android.content.Context
import com.mediacontrol.floatingwidget.model.WidgetButton
import com.mediacontrol.floatingwidget.model.WidgetConfig
import com.mediacontrol.floatingwidget.model.DragHandlePlacement
import com.mediacontrol.floatingwidget.model.WidgetLayout
import com.mediacontrol.floatingwidget.model.WidgetSizePreset
import com.mediacontrol.floatingwidget.model.WidgetThemePreset
import com.mediacontrol.floatingwidget.model.WidgetWidthStyle
import com.mediacontrol.floatingwidget.storage.PreferencesStorage
import com.mediacontrol.floatingwidget.storage.SharedPreferencesStorage
import kotlin.math.roundToInt

class WidgetConfigStore(
    private val storage: PreferencesStorage
) {

    constructor(context: Context) : this(
        SharedPreferencesStorage(
            context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        )
    )

    fun load(): WidgetConfig {
        val rawButtons = storage.getString(KEY_VISIBLE_BUTTONS, defaultButtonValue())
        val visibleButtons = WidgetConfigStorageFormat.decodeVisibleButtons(rawButtons)
        val dragHandlePlacement = DragHandlePlacement.entries.firstOrNull {
            it.name == storage.getString(KEY_DRAG_HANDLE_PLACEMENT, WidgetLayout.Default.dragHandlePlacement.name)
        } ?: WidgetLayout.Default.dragHandlePlacement
        val layout = runCatching {
            WidgetLayout(
                visibleButtons = visibleButtons,
                dragHandlePlacement = dragHandlePlacement
            )
        }
            .getOrElse { WidgetLayout.Default }
        val sizePreset = WidgetSizePreset.entries.firstOrNull {
            it.name == storage.getString(KEY_SIZE_PRESET, WidgetConfig().sizePreset.name)
        } ?: WidgetConfig().sizePreset
        val widthStyle = WidgetWidthStyle.entries.firstOrNull {
            it.name == storage.getString(KEY_WIDTH_STYLE, WidgetConfig().widthStyle.name)
        } ?: WidgetConfig().widthStyle
        val themePreset = WidgetThemePreset.entries.firstOrNull {
            it.name == storage.getString(KEY_THEME_PRESET, WidgetConfig().themePreset.name)
        } ?: WidgetConfig().themePreset
        val opacity = storage.getInt(KEY_OPACITY_PERCENT, (WidgetConfig().opacity * 100).roundToInt()) / 100f

        return WidgetConfig(
            layout = layout,
            sizePreset = sizePreset,
            widthStyle = widthStyle,
            themePreset = themePreset,
            opacity = opacity.coerceIn(0.35f, 1f),
            persistentOverlayEnabled = storage.getBoolean(
                KEY_PERSISTENT_OVERLAY_ENABLED,
                WidgetConfig().persistentOverlayEnabled
            )
        )
    }

    fun save(config: WidgetConfig) {
        storage.edit {
            putString(KEY_VISIBLE_BUTTONS, WidgetConfigStorageFormat.encodeVisibleButtons(config.layout.orderedButtons))
            putString(KEY_DRAG_HANDLE_PLACEMENT, config.layout.dragHandlePlacement.name)
            putString(KEY_SIZE_PRESET, config.sizePreset.name)
            putString(KEY_WIDTH_STYLE, config.widthStyle.name)
            putString(KEY_THEME_PRESET, config.themePreset.name)
            putInt(KEY_OPACITY_PERCENT, (config.opacity.coerceIn(0.35f, 1f) * 100).roundToInt())
            putBoolean(KEY_PERSISTENT_OVERLAY_ENABLED, config.persistentOverlayEnabled)
        }
    }

    private fun defaultButtonValue(): String {
        return WidgetConfigStorageFormat.encodeVisibleButtons(WidgetLayout.Default.orderedButtons)
    }

    private companion object {
        const val PREFERENCES_NAME = "widget_config"
        const val KEY_VISIBLE_BUTTONS = "visible_buttons"
        const val KEY_DRAG_HANDLE_PLACEMENT = "drag_handle_placement"
        const val KEY_SIZE_PRESET = "size_preset"
        const val KEY_WIDTH_STYLE = "width_style"
        const val KEY_THEME_PRESET = "theme_preset"
        const val KEY_OPACITY_PERCENT = "opacity_percent"
        const val KEY_PERSISTENT_OVERLAY_ENABLED = "persistent_overlay_enabled"
    }
}

object WidgetConfigStorageFormat {
    private const val BUTTON_SEPARATOR = ","

    fun encodeVisibleButtons(buttons: List<WidgetButton>): String {
        return buttons.joinToString(separator = BUTTON_SEPARATOR) { it.name }
    }

    fun decodeVisibleButtons(raw: String?): Set<WidgetButton> {
        if (raw.isNullOrBlank()) {
            return WidgetLayout.Default.visibleButtons
        }

        val buttons = raw.split(BUTTON_SEPARATOR)
            .mapNotNull { token -> WidgetButton.entries.firstOrNull { it.name == token } }
            .toSet()

        return if (buttons in WidgetLayout.supportedButtonSets) {
            buttons
        } else {
            WidgetLayout.Default.visibleButtons
        }
    }
}
