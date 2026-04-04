package sw2.io.mediafloat.overlay

import android.view.Gravity
import android.widget.LinearLayout
import sw2.io.mediafloat.model.DragHandlePlacement
import sw2.io.mediafloat.model.MediaSessionState
import sw2.io.mediafloat.model.currentDisplayText
import sw2.io.mediafloat.model.WidgetOverlayAppearance

internal data class OverlayPresentationSpec(
    val metrics: OverlayLayoutMetrics,
    val rootGravity: Int,
    val mediaRowOrientation: Int,
    val mediaRowGravity: Int,
    val controlsRowWidthDp: Int,
    val controlsRowHeightDp: Int,
    val thumbnailVisible: Boolean,
    val thumbnailWidthDp: Int,
    val thumbnailHeightDp: Int,
    val thumbnailMarginEndDp: Int,
    val titleVisible: Boolean,
    val titleText: String,
    val titleWidthDp: Int,
    val titleMarginEndDp: Int,
    val titleBottomMarginDp: Int,
    val dragHandleMarginEndDp: Int
)

internal object OverlayPresentationSpecFactory {
    fun create(
        appearance: WidgetOverlayAppearance,
        mediaState: MediaSessionState,
        thumbnailEnabled: Boolean,
        dragHandlePlacement: DragHandlePlacement
    ): OverlayPresentationSpec {
        val sizing = appearance.sizing
        val titleText = mediaState.currentDisplayText()
        val titleVisible = true
        val metrics = OverlayLayoutCalculator.calculate(
            appearance = appearance,
            thumbnailEnabled = thumbnailEnabled,
            hasTitle = titleVisible
        )
        return OverlayPresentationSpec(
            metrics = metrics,
            rootGravity = if (dragHandlePlacement == DragHandlePlacement.Left) {
                Gravity.START or Gravity.CENTER_VERTICAL
            } else {
                Gravity.END or Gravity.CENTER_VERTICAL
            },
            mediaRowOrientation = LinearLayout.VERTICAL,
            mediaRowGravity = Gravity.CENTER_HORIZONTAL,
            controlsRowWidthDp = sizing.containerWidthDp,
            controlsRowHeightDp = sizing.containerHeightDp,
            thumbnailVisible = thumbnailEnabled,
            thumbnailWidthDp = sizing.thumbnailSizeDp,
            thumbnailHeightDp = sizing.thumbnailSizeDp,
            thumbnailMarginEndDp = if (thumbnailEnabled) sizing.itemSpacingDp else 0,
            titleVisible = titleVisible,
            titleText = titleText,
            titleWidthDp = sizing.titleStripWidthDp,
            titleMarginEndDp = 0,
            titleBottomMarginDp = sizing.titleStripSpacingDp,
            dragHandleMarginEndDp = if (dragHandlePlacement == DragHandlePlacement.Left) sizing.itemSpacingDp else 0
        )
    }
}
