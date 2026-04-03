package sw2.io.mediafloat.overlay

import kotlin.math.max
import sw2.io.mediafloat.model.WidgetOverlayAppearance

data class OverlayLayoutMetrics(
    val totalWidthDp: Int,
    val totalHeightDp: Int,
    val mediaColumnWidthDp: Int,
    val mediaColumnHeightDp: Int
)

internal object OverlayLayoutCalculator {
    fun calculate(
        appearance: WidgetOverlayAppearance,
        thumbnailEnabled: Boolean,
        hasTitle: Boolean
    ): OverlayLayoutMetrics {
        val sizing = appearance.sizing
        val mediaColumnWidthDp = max(sizing.containerWidthDp, sizing.titleStripWidthDp)
        val mediaColumnHeightDp = sizing.containerHeightDp + if (hasTitle) sizing.titleStripMinHeightDp + sizing.titleStripSpacingDp else 0
        val totalWidthDp = if (thumbnailEnabled) {
            sizing.thumbnailSizeDp + sizing.itemSpacingDp + mediaColumnWidthDp
        } else {
            mediaColumnWidthDp
        }
        val totalHeightDp = if (thumbnailEnabled) {
            max(sizing.thumbnailSizeDp, mediaColumnHeightDp)
        } else {
            mediaColumnHeightDp
        }
        return OverlayLayoutMetrics(
            totalWidthDp = totalWidthDp,
            totalHeightDp = totalHeightDp,
            mediaColumnWidthDp = mediaColumnWidthDp,
            mediaColumnHeightDp = mediaColumnHeightDp
        )
    }
}
