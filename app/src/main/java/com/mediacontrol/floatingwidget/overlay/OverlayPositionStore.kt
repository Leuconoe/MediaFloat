package com.mediacontrol.floatingwidget.overlay

import android.content.Context
import com.mediacontrol.floatingwidget.model.WidgetAnchor
import com.mediacontrol.floatingwidget.model.WidgetPosition
import com.mediacontrol.floatingwidget.storage.PreferencesStorage
import com.mediacontrol.floatingwidget.storage.SharedPreferencesStorage
import kotlin.math.roundToInt

class OverlayPositionStore(
    private val preferences: PreferencesStorage,
    private val density: Float
) {

    constructor(context: Context) : this(
        preferences = SharedPreferencesStorage(
            context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        ),
        density = context.resources.displayMetrics.density
    )

    fun load(): WidgetPosition {
        val anchorName = preferences.getString(KEY_ANCHOR, WidgetPosition().anchor.name)
        val anchor = WidgetAnchor.entries.firstOrNull { it.name == anchorName } ?: WidgetPosition().anchor

        return WidgetPosition(
            anchor = anchor,
            xOffsetDp = preferences.getInt(KEY_X_OFFSET_DP, WidgetPosition().xOffsetDp),
            yOffsetDp = preferences.getInt(KEY_Y_OFFSET_DP, WidgetPosition().yOffsetDp)
        )
    }

    fun save(position: WidgetPosition) {
        preferences.edit {
            putString(KEY_ANCHOR, position.anchor.name)
            putInt(KEY_X_OFFSET_DP, position.xOffsetDp)
            putInt(KEY_Y_OFFSET_DP, position.yOffsetDp)
        }
    }

    fun dpToPx(dp: Int): Int = (dp * density).roundToInt()

    fun pxToDp(px: Int): Int = (px / density).roundToInt()

    private companion object {
        const val PREFERENCES_NAME = "overlay_position"
        const val KEY_ANCHOR = "anchor"
        const val KEY_X_OFFSET_DP = "x_offset_dp"
        const val KEY_Y_OFFSET_DP = "y_offset_dp"
    }
}
